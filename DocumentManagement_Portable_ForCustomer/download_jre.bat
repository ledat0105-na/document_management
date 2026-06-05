@echo off
echo ========================================
echo    TAI JAVA RUNTIME PORTABLE
echo ========================================
echo.
echo Dang tai Java Runtime Environment...
echo.

if not exist "jre" (
    echo Tai JRE tu Adoptium...
    powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.12%2B7/OpenJDK17U-jre_x64_windows_hotspot_17.0.12_7.zip' -OutFile 'jre.zip'}"
    
    if exist "jre.zip" (
        echo Giai nen JRE...
        powershell -Command "Expand-Archive -Path 'jre.zip' -DestinationPath '.' -Force"
        for /d %%i in (jdk-*) do ren "%%i" "jre"
        del jre.zip
        echo JRE da duoc tai thanh cong!
    ) else (
        echo Loi: Khong the tai JRE tu internet
        echo Vui long tai thu cong tu: https://adoptium.net/
        pause
        exit /b 1
    )
) else (
    echo JRE da ton tai!
)

echo.
echo ========================================
echo    HOAN THANH!
echo ========================================
echo.
echo Java Runtime da san sang su dung
echo.
pause
