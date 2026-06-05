@echo off
title DocManager - Starting...
echo.
echo ========================================
echo    DocManager - Document Management
echo ========================================
echo.
echo Starting server...
echo Please wait while the application loads...
echo.
echo Once started, your browser will open automatically.
echo If not, please go to: http://localhost:8080
echo.
echo To stop the server, close this window or press Ctrl+C
echo.

REM Wait 2 seconds then open browser
timeout /t 2 /nobreak >nul
start "" "http://localhost:8080"

REM Start the application
java -jar document_management-0.0.1-SNAPSHOT.jar --spring.config.location=application.properties

pause
