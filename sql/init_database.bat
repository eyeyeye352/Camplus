@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

cd /d "%~dp0"

echo ========================================
echo      Camplus - 一键建库脚本
echo ========================================
echo.

set "mysql_cmd=mysql"

where mysql >nul 2>&1
if errorlevel 1 (
    echo 警告: mysql 命令未找到，尝试使用完整路径...
    set "mysql_cmd=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    if not exist "!mysql_cmd!" (
        set "mysql_cmd=C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe"
        if not exist "!mysql_cmd!" (
            echo 错误: 未找到 MySQL 客户端，请确保 MySQL 已安装并添加到 PATH
            pause
            exit /b 1
        )
    )
)

set "db_username="
set "db_password="

set /p "db_username=请输入MySQL用户名 (默认: root): "
if not defined db_username set "db_username=root"

set "psCommand=powershell -Command "$pword = read-host '请输入MySQL密码' -AsSecureString ; ^
    $plainPassword = [System.Net.NetworkCredential]::new('', $pword).Password ; ^
    Write-Output $plainPassword""
for /f "usebackq delims=" %%p in (`%psCommand%`) do set "db_password=%%p"

echo.
echo ========================================
echo 开始初始化数据库...
echo ========================================
echo.

echo [1/3] 删除并创建数据库 camplus_db...
"%mysql_cmd%" -u%db_username% -p%db_password% -e "DROP DATABASE IF EXISTS camplus_db; CREATE DATABASE camplus_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
if errorlevel 1 (
    echo 错误: 创建数据库失败，请检查用户名和密码是否正确
    pause
    exit /b 1
)
echo 数据库创建成功
echo.

echo [2/3] 执行SQL文件...

set "sql_count=0"
set "sql_total=0"
set "sql_skipped=0"

for /f "delims=" %%f in ('dir /b /on *.sql 2^>nul') do (
    set /a sql_total+=1
)

echo 发现 %sql_total% 个SQL文件，按文件名排序执行:
echo.

for /f "delims=" %%f in ('dir /b /on *.sql 2^>nul') do (
    set "file_size=0"
    for %%s in ("%%f") do set "file_size=%%~zs"
    
    if !file_size! equ 0 (
        set /a sql_skipped+=1
        echo [跳过] %%f (文件为空)
    ) else (
        set /a sql_count+=1
        echo [%sql_count%/%sql_total%] 正在执行: %%f
        "%mysql_cmd%" -u%db_username% -p%db_password% camplus_db < "%%f"
        if errorlevel 1 (
            echo 警告: 执行 %%f 时出现错误
        )
    )
    echo.
)

echo.
echo [3/3] 数据库初始化完成！
echo.
echo ========================================
echo      建库完成！
echo ========================================
echo 数据库: camplus_db
echo 用户名: %db_username%
echo 执行SQL文件数: %sql_count%
if %sql_skipped% gtr 0 (
    echo 跳过空文件数: %sql_skipped%
)
echo.
pause