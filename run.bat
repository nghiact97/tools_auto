@echo off
title JavaRPA Tool
setlocal

:: ── Tự elevate lên Admin nếu chưa có quyền ──────────────────────────
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo Dang yeu cau quyen Admin...
    powershell -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
    exit /b
)
:: ─────────────────────────────────────────────────────────────────────

set APP_HOME=%~dp0
set LIB=%APP_HOME%build\install\JavaRPA\lib

set FX_MODS=%LIB%\javafx-graphics-11-win.jar;%LIB%\javafx-base-11-win.jar;%LIB%\javafx-controls-11-win.jar;%LIB%\javafx-fxml-11-win.jar;%LIB%\javafx-swing-11-win.jar;%LIB%\javafx-graphics-11.jar;%LIB%\javafx-base-11.jar;%LIB%\javafx-controls-11.jar

set CP=%LIB%\JavaRPA-1.0.0.jar;%LIB%\jnativehook-2.2.2.jar;%LIB%\tess4j-4.5.5.jar;%LIB%\gson-2.11.0.jar;%LIB%\slf4j-simple-2.0.13.jar;%LIB%\slf4j-api-2.0.13.jar;%LIB%\ghost4j-1.0.1.jar;%LIB%\lept4j-1.13.3.jar;%LIB%\jna-5.8.0.jar;%LIB%\jai-imageio-core-1.4.0.jar;%LIB%\pdfbox-2.0.24.jar;%LIB%\fontbox-2.0.24.jar;%LIB%\commons-logging-1.2.jar;%LIB%\commons-io-2.11.0.jar;%LIB%\itext-2.1.7.jar;%LIB%\log4j-1.2.17.jar;%LIB%\javafx-swing-11-win.jar

java ^
  --module-path "%FX_MODS%" ^
  --add-modules javafx.controls,javafx.fxml,javafx.swing ^
  --add-opens java.base/java.lang=ALL-UNNAMED ^
  --add-opens java.desktop/java.awt=ALL-UNNAMED ^
  -cp "%CP%" ^
  com.javarpa.App

if errorlevel 1 (
    echo.
    echo [ERROR] App crash - xem log phia tren
    pause
)
endlocal
