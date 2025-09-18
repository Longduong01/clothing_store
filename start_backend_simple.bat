@echo off
echo Starting Spring Boot application...
echo.

REM Check if Java is available
java -version
if %errorlevel% neq 0 (
    echo Java is not installed or not in PATH
    pause
    exit /b 1
)

echo.
echo Java version check passed
echo.

REM Start Spring Boot application using the main class
echo Starting Spring Boot application...
echo.

java -cp "target\classes" com.example.demo_store.DemoStoreApplication

pause
