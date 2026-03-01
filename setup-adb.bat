@echo off
setlocal

:: ============================================================
:: setup-adb.bat — ADB Device Owner provisioning
:: Use for devices with non-standard setup wizards (e.g. Walpad 11G)
:: that do not support QR-based provisioning.
::
:: Pre-conditions (must be done on the device BEFORE running this):
::   1. Factory-reset the device
::   2. Do NOT add any Google or other accounts
::   3. Enable USB Debugging (Settings > Developer options)
::   4. Connect device via USB and trust this computer
:: ============================================================

set APK=app\release\app-release.apk
set PACKAGE=com.bitopi.mdm
set ADMIN_RECEIVER=com.bitopi.mdm/.DeviceAdminReceiver

echo.
echo ============================================================
echo  Bitopi MDM - ADB Device Owner Setup
echo ============================================================
echo.

:: Check ADB is available
where adb >nul 2>&1
if errorlevel 1 (
    echo ERROR: 'adb' not found in PATH.
    echo Please install Android SDK Platform-Tools and add to PATH.
    pause
    exit /b 1
)

:: Check device connected
echo Checking for connected device...
for /f "tokens=1" %%d in ('adb devices ^| findstr /v "List" ^| findstr "device"') do (
    set DEVICE=%%d
)
if not defined DEVICE (
    echo ERROR: No device connected or USB Debugging not authorised.
    echo Connect device, enable USB Debugging, and trust this computer.
    pause
    exit /b 1
)
echo Device found: %DEVICE%
echo.

:: Check APK exists
if not exist "%APK%" (
    echo ERROR: APK not found at %APK%
    echo Build the release APK first: gradlew assembleRelease
    pause
    exit /b 1
)

:: Install APK
echo Installing APK...
adb install -r "%APK%"
if errorlevel 1 (
    echo ERROR: APK installation failed.
    pause
    exit /b 1
)
echo APK installed successfully.
echo.

:: Set Device Owner
echo Setting Device Owner...
adb shell dpm set-device-owner %ADMIN_RECEIVER%
if errorlevel 1 (
    echo.
    echo ERROR: Failed to set Device Owner.
    echo Common causes:
    echo   - A Google or other account is already added to the device
    echo   - The device already has a Device Owner
    echo   - The app was not freshly installed on a factory-reset device
    echo.
    echo Solution: Factory reset the device, do NOT add any accounts,
    echo enable USB Debugging, then run this script again.
    pause
    exit /b 1
)
echo.
echo ============================================================
echo  SUCCESS: Device Owner set to %PACKAGE%
echo ============================================================
echo.

:: Verify
echo Verifying...
adb shell dpm list-owners
echo.

pause
endlocal
