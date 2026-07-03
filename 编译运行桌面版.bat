@echo off
cd /d "%~dp0"

echo ========================================
echo     Camplus - Desktop Version
echo ========================================
echo.

echo [0/3] Checking Java environment...
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java not found. Please install JDK 17 or higher.
    pause
    exit /b 1
)
echo Java environment OK
echo.

echo [1/3] Checking .NET environment...
dotnet --version >nul 2>&1
if errorlevel 1 (
    echo Error: .NET SDK not found. Please install .NET 8 SDK.
    pause
    exit /b 1
)
echo .NET environment OK
echo.

echo [2/3] Compiling project...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo.
    echo ========================================
    echo           BUILD FAILED!
    echo ========================================
    echo Please check the error messages above.
    pause
    exit /b 1
)
echo Backend compiled successfully
echo.

cd Camplus.Desktop
dotnet build -c Release
if errorlevel 1 (
    echo.
    echo ========================================
    echo           DESKTOP BUILD FAILED!
    echo ========================================
    echo Please check the error messages above.
    pause
    exit /b 1
)
echo Desktop application compiled successfully
echo.

echo ========================================
echo   Starting Camplus...
echo ========================================
echo.
echo Note: A dialog will appear asking for MySQL credentials.
echo       Backend logs will show in a separate command window.
echo.

cd /d "%~dp0Camplus.Desktop"
"bin\Release\net8.0-windows\Camplus.exe"

echo.
echo Application exited.
echo.
pause