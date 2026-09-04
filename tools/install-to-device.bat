@echo off
setlocal
cd /d "%~dp0.."
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0install-to-device.ps1"
if errorlevel 1 (
    echo.
    echo インストールに失敗しました。
    pause
    exit /b 1
)
echo.
pause
