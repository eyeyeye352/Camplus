@echo off
cd /d "%~dp0"

echo ========================================
echo      Camplus - Database Setup
echo ========================================
echo.

echo Current directory: %cd%
echo.

set "mysql_path=C:\Program Files\MySQL\MySQL Server 8.0\bin"
set "mysql_cmd=%mysql_path%\mysql.exe"

if not exist "%mysql_cmd%" (
    set "mysql_path=D:\MySQL\MySQL Server 8.0\bin"
    set "mysql_cmd=%mysql_path%\mysql.exe"
)

if not exist "%mysql_cmd%" (
    echo ERROR: MySQL client not found
    echo Please make sure MySQL is installed
    echo Press any key to exit...
    pause
    exit /b 1
)

echo MySQL client: %mysql_cmd%
echo.

set "user="
set /p "user=Enter MySQL username: "
if "%user%"=="" set "user=root"

set "pass="
set /p "pass=Enter MySQL password: "

echo.
echo ========================================
echo Initializing database...
echo ========================================
echo.

echo Step 1: Executing init.sql...
"%mysql_cmd%" -u%user% -p%pass% < init.sql
if errorlevel 1 (
    echo.
    echo ERROR: Database creation failed!
    echo Please check username and password
    pause
    exit /b 1
)
echo init.sql executed successfully
echo.

echo Step 2: Executing data.sql...
"%mysql_cmd%" -u%user% -p%pass% < data.sql
if errorlevel 1 (
    echo WARNING: data.sql may have issues
) else (
    echo data.sql executed successfully
)
echo.

echo.
echo ========================================
echo Database created!
echo ========================================
echo.
echo Database: camplus_db
echo Username: %user%
echo.

echo ========================================
echo Step 3: Data Import
echo ========================================
echo.

cd /d "%~dp0\.."

set "jar_path=target\Camplus.jar"

echo Compiling and packaging project...
call mvn clean package -q -DskipTests
if errorlevel 1 (
    echo.
    echo ERROR: Compilation failed!
    echo Please run "mvn clean package -DskipTests" manually and retry
    echo.
    echo Press any key to exit...
    pause
    exit /b 1
)
echo Compilation completed

set "has_file=0"
if exist "RawData" (
    for %%f in (RawData\*) do (
        if not "%%~xf"==".gitkeep" if not "%%~xf"==".bat" set "has_file=1"
    )
)

if "%has_file%"=="0" (
    echo No valid files in RawData directory, skipping import
    echo.
    echo Done! Press any key to exit...
    pause
    exit /b 0
)

echo Found RawData files, starting import...
echo This will parse files, load vector model and import data. May take a few minutes.
echo.

java -jar "%jar_path%" --import-only --spring.profiles.active=import-only --db-user "%user%" --db-pass "%pass%"

if errorlevel 1 (
    echo.
    echo ========================================
    echo WARNING: Data import completed with errors
    echo ========================================
    echo Possible causes:
    echo   1. BGE-M3 model files not found
    echo   2. Database connection failed
    echo   3. File parsing failed
    echo.
    echo You can also start Camplus app, the system will retry auto-import
) else (
    echo.
    echo ========================================
    echo Database + import completed!
    echo ========================================
)

echo.
echo Press any key to exit...
pause
