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

REM Set classpath
set CLASSPATH=target\classes
for %%i in (target\lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%i

REM Start Spring Boot application
echo Starting Spring Boot application...
echo Classpath: %CLASSPATH%
echo.

java -cp "%CLASSPATH%" com.example.demo_store.DemoStoreApplication

pause
