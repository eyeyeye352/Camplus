@echo off
chcp 65001
cd /d "%~dp0"

echo ========================================
echo      Camplus - 一键建库脚本
echo ========================================
echo.

echo 当前目录: %cd%
echo.

set "mysql_path=D:\MySQL\MySQL Server 8.0\bin"
set "mysql_cmd=%mysql_path%\mysql.exe"

if not exist "%mysql_cmd%" (
    set "mysql_path=D:\MySQL\MySQL Server 8.0\bin"
    set "mysql_cmd=%mysql_path%\mysql.exe"
)

if not exist "%mysql_cmd%" (
    echo 错误: 未找到 MySQL 客户端
    echo 请确保 MySQL 已安装
    echo 按任意键退出...
    pause
    exit /b 1
)

echo MySQL客户端: %mysql_cmd%
echo.

set "user="
set /p "user=请输入MySQL用户名: "
if "%user%"=="" set "user=root"

set "pass="
set /p "pass=请输入MySQL密码: "

echo.
echo ========================================
echo 开始初始化数据库...
echo ========================================
echo.

echo 步骤1: 删除并创建数据库 camplus_db...
"%mysql_cmd%" -u%user% -p%pass% -e "DROP DATABASE IF EXISTS camplus_db; CREATE DATABASE camplus_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
if errorlevel 1 (
    echo.
    echo 错误: 创建数据库失败！
    echo 请检查用户名和密码是否正确
    pause
    exit /b 1
)
echo 成功创建数据库
echo.

echo 步骤2: 执行SQL文件...

for %%f in (*.sql) do (
    echo.
    echo 正在执行: %%f
    
    echo USE camplus_db; > temp_sql.sql
    type "%%f" >> temp_sql.sql
    
    "%mysql_cmd%" -u%user% -p%pass% < temp_sql.sql
    
    if errorlevel 1 (
        echo 警告: %%f 执行可能有问题
    ) else (
        echo %%f 执行成功
    )
    
    del temp_sql.sql
)

echo.
echo ========================================
echo 建库完成！
echo ========================================
echo.
echo 数据库: camplus_db
echo 用户名: %user%
echo.
echo 按任意键退出...
pause