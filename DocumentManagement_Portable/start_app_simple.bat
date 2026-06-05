@echo off
title Document Management System
color 0A
echo.
echo ========================================
echo    DOCUMENT MANAGEMENT SYSTEM
echo    PHIEN BAN PORTABLE
echo ========================================
echo.
echo CHI CAN 2 BUOC:
echo 1. Chay file nay
echo 2. Mo trinh duyet: http://localhost:8080
echo.
echo ========================================
echo    DANG KHOI DONG...
echo ========================================
echo.

REM Kiem tra Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo ========================================
    echo    CAN CAI DAT JAVA
    echo ========================================
    echo.
    echo May tinh chua co Java!
    echo Vui long cai dat Java tu: https://adoptium.net/
    echo.
    echo Hoac tai Java 17 tu: https://adoptium.net/temurin/releases/
    echo.
    echo Sau khi cai dat Java, chay lai file nay.
    echo.
    pause
    exit /b 1
)

echo Java da duoc cai dat!
echo Dang khoi dong ung dung...
echo.
echo Vui long cho trong khi ung dung khoi dong...
echo.
echo Sau khi khoi dong, ban se thay:
echo "Started DocumentManagementApplication"
echo.
echo Khi do, mo trinh duyet va truy cap:
echo http://localhost:8080
echo.
echo ========================================
echo.

REM Chay ung dung
java -jar document_management-0.0.1-SNAPSHOT.jar

echo.
echo Ung dung da dung!
pause
