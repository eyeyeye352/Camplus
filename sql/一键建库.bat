@echo off
chcp 65001
cd /d "%~dp0"

echo ========================================
echo      Camplus - 一键建库脚本
echo ========================================
echo.

echo 当前目录: %cd%
echo.

set "mysql_path=C:\Program Files\MySQL\MySQL Server 8.0\bin"
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

echo 步骤1: 执行建库脚本 init.sql...
"%mysql_cmd%" -u%user% -p%pass% < init.sql
if errorlevel 1 (
    echo.
    echo 错误: 建库失败！
    echo 请检查用户名和密码是否正确
    pause
    exit /b 1
)
echo init.sql 执行成功
echo.

echo 步骤2: 执行数据脚本 data.sql...
"%mysql_cmd%" -u%user% -p%pass% < data.sql
if errorlevel 1 (
    echo 警告: data.sql 执行可能有问题
) else (
    echo data.sql 执行成功
)
echo.

echo.
echo ========================================
echo 数据库创建完成！
echo ========================================
echo.
echo 数据库: camplus_db
echo 用户名: %user%
echo.

echo ========================================
echo 步骤3: 数据导入
echo ========================================
echo.

REM 回到项目根目录
cd /d "%~dp0\.."

REM 检查 jar 是否存在
set "jar_path=target\Camplus.jar"

echo 正在编译打包项目...
call mvn clean package -q -DskipTests
if errorlevel 1 (
    echo.
    echo 错误: 编译打包失败！
    echo 请手动执行 mvn clean package -DskipTests 后重新运行此脚本
    echo.
    echo 按任意键退出...
    pause
    exit /b 1
)
echo 编译打包完成

REM 检查 RawData 目录是否有有效文件
set "has_file=0"
if exist "RawData" (
    for %%f in (RawData\*) do (
        if not "%%~xf"==".gitkeep" if not "%%~xf"==".bat" set "has_file=1"
    )
)

if "%has_file%"=="0" (
    echo RawData 目录中没有有效文件，跳过数据导入
    echo.
    echo 建库完成！按任意键退出...
    pause
    exit /b 0
)

echo 找到 RawData 文件，开始数据导入...
echo 这将解析文件、加载向量化模型并导入数据，可能需要几分钟。
echo.

java -jar "%jar_path%" --import-only --spring.profiles.active=import-only --db-user "%user%" --db-pass "%pass%"

if errorlevel 1 (
    echo.
    echo ========================================
    echo 警告: 数据导入过程中出现错误
    echo ========================================
    echo 可能原因：
    echo   1. BGE-M3 模型文件未找到
    echo   2. 数据库连接失败
    echo   3. 文件解析失败
    echo.
    echo 你也可以启动 Camplus 应用，系统会再次尝试自动导入
) else (
    echo.
    echo ========================================
    echo 建库 + 数据导入全部完成！
    echo ========================================
)

echo.
echo 按任意键退出...
pause