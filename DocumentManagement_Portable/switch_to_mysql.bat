@echo off
echo ========================================
echo    CHUYEN DOI SANG MYSQL DATABASE
echo ========================================
echo.
echo Dang chuyen doi cau hinh sang MySQL database...
echo Can cai dat MySQL Server truoc
echo.
copy application_mysql.properties application.properties
echo.
echo Da chuyen doi thanh cong!
echo Hay kiem tra thong tin database trong application.properties
echo.
echo Thong tin database hien tai:
echo - Username: root
echo - Password: lambieng
echo - Database: document_management
echo.
echo Neu can thay doi, sua file application.properties
echo.
pause
