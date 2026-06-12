@echo off
cd /d "%~dp0"

echo ========================================
echo     Camplus - Desktop Version
echo ========================================
echo.

echo [0/4] Checking Java environment...
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java not found. Please install JDK 17 or higher.
    pause
    exit /b 1
)
echo Java environment OK
echo.

echo [1/4] Checking .NET environment...
dotnet --version >nul 2>&1
if errorlevel 1 (
    echo Error: .NET SDK not found. Please install .NET 8 SDK.
    pause
    exit /b 1
)
echo .NET environment OK
echo.

echo [2/4] Compiling Spring Boot project...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo Error: Maven build failed
    pause
    exit /b 1
)
echo Spring Boot project compiled successfully
echo.

echo [3/4] Compiling desktop application...
cd Camplus.Desktop
dotnet build -c Release
if errorlevel 1 (
    echo Error: Desktop app build failed
    pause
    exit /b 1
)
echo Desktop application compiled successfully
echo.

echo ========================================
echo   Starting desktop application...
echo ========================================
echo.

cd /d "%~dp0Camplus.Desktop"
"bin\Release\net8.0-windows\Camplus.exe"

echo.
echo Application exited.
echo.
pause