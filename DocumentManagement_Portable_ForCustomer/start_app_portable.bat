@echo off
title Document Management System
echo ========================================
echo    DOCUMENT MANAGEMENT SYSTEM
echo    (Portable Version)
echo ========================================
echo.

REM Kiem tra JRE
if not exist "jre\bin\java.exe" (
    echo Java Runtime chua duoc tai!
    echo Dang tai Java Runtime...
    call download_jre.bat
    if not exist "jre\bin\java.exe" (
        echo Loi: Khong the tai Java Runtime
        echo Vui long tai thu cong tu: https://adoptium.net/
        pause
        exit /b 1
    )
)

echo Dang khoi dong ung dung...
echo.
echo Vui long cho trong khi ung dung khoi dong...
echo.
echo Sau khi khoi dong, ban co the truy cap tai:
echo http://localhost:8080
echo.
echo Nhan Ctrl+C de dung ung dung
echo.

REM Chay ung dung voi JRE portable
jre\bin\java.exe -jar document_management-0.0.1-SNAPSHOT.jar

echo.
echo Ung dung da dung!
pause
