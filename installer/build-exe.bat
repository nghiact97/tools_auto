@echo off
echo ============================================
echo   JavaRPA Tool - Build and Package Script
echo ============================================
echo.

REM Step 1: Build fat JAR
echo [1/3] Building fat JAR...
call gradlew.bat shadowJar
if errorlevel 1 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)
echo Build successful!
echo.

REM Step 2: Package with jpackage
echo [2/3] Creating installer with jpackage...
jpackage ^
    --type exe ^
    --name "JavaRPA" ^
    --input build\libs ^
    --main-jar javarpa-1.0.jar ^
    --main-class com.javarpa.App ^
    --dest installer\output ^
    --icon src\main\resources\images\icon.ico ^
    --win-menu ^
    --win-shortcut ^
    --win-dir-chooser ^
    --app-version 1.0.0 ^
    --vendor "JavaRPA" ^
    --description "RPA Desktop Automation Tool" ^
    --copyright "2024 JavaRPA" ^
    --java-options "--add-opens java.base/java.lang=ALL-UNNAMED" ^
    --java-options "--add-opens java.desktop/java.awt=ALL-UNNAMED"

if errorlevel 1 (
    echo ERROR: jpackage failed! Make sure WiX Toolset is installed.
    echo Download: https://wixtoolset.org/releases/
    pause
    exit /b 1
)

echo.
echo [3/3] Done!
echo Installer created in: installer\output\
pause
