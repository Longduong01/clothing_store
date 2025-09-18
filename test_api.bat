@echo off
echo Testing API endpoints...
echo.

echo Testing connection...
curl -X GET http://localhost:8080/api/test/connection
echo.
echo.

echo Testing users API...
curl -X GET http://localhost:8080/api/users
echo.
echo.

echo Testing sizes API...
curl -X GET http://localhost:8080/api/sizes
echo.
echo.

echo Testing colors API...
curl -X GET http://localhost:8080/api/colors
echo.
echo.

echo Testing sizes with error details...
curl -X GET http://localhost:8080/api/test/sizes
echo.
echo.

pause
