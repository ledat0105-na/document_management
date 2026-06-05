@echo off
title Document Management System - MySQL
color 0A
echo.
echo ========================================
echo    DOCUMENT MANAGEMENT SYSTEM
echo    CHAY VOI MYSQL DATABASE
echo ========================================
echo.
echo HUONG DAN NHANH:
echo 1. Dam bao MySQL Server dang chay
echo 2. Dam bao database 'document_management' da tao
echo 3. Nhan Enter de chay ung dung
echo.
echo ========================================
echo    THONG TIN DATABASE
echo ========================================
echo Username: root
echo Password: lambieng
echo Database: document_management
echo Host: localhost:3306
echo.
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
    echo Vui long cai dat Java 17 tu: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

echo Java da duoc cai dat!
echo.

REM Chuyen sang MySQL
echo Dang chuyen doi cau hinh sang MySQL...
copy application_mysql.properties application.properties >nul
echo Da chuyen doi thanh cong!
echo.

echo ========================================
echo    DANG KHOI DONG UNG DUNG...
echo ========================================
echo.
echo Vui long cho trong khi ung dung khoi dong...
echo Sau khi khoi dong, ban se thay:
echo "Started DocumentManagementApplication"
echo.
echo Khi do, mo trinh duyet va truy cap:
echo http://localhost:8080
echo.
echo Tai khoan admin mac dinh:
echo Email: admin@example.com
echo Password: admin123
echo.
echo ========================================
echo.

REM Chay ung dung
java -jar document_management-0.0.1-SNAPSHOT.jar

echo.
echo Ung dung da dung!
pause
