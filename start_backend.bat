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

REM Check if Maven is available
mvn -version
if %errorlevel% neq 0 (
    echo Maven is not installed or not in PATH
    pause
    exit /b 1
)

echo.
echo Maven version check passed
echo.

REM Start Spring Boot application
echo Starting Spring Boot application...
mvn spring-boot:run

pause
