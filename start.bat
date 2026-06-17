@echo off
cd /d "%~dp0"
echo Starting Order Returns System on http://127.0.0.1:8080
echo Ensure MongoDB is running on localhost:27017
echo.
mvn spring-boot:run
