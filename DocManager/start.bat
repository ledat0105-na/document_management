@echo off
echo Starting DocManager...
echo Please wait while the application starts...
timeout /t 2 /nobreak >nul
start "" "http://localhost:8080"
java -jar document_management-0.0.1-SNAPSHOT.jar --spring.config.location=application.properties
pause
