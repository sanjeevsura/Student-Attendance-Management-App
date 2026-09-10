@echo off
echo ========================================================
echo        TrackEdu - Android APK Build Pipeline
echo ========================================================
echo.
echo Select build target:
echo [1] Debug APK   (assembleDebug)
echo [2] Release APK (assembleRelease)
echo [3] Build Both  (Debug + Release)
echo [4] Run Live Web Preview Server (Port 3000)
echo.
set /p choice="Enter your choice (1-4): "

if "%choice%"=="1" (
    echo Building Debug APK...
    call gradlew.bat assembleDebug
    echo Debug APK generated at: app\build\outputs\apk\debug\app-debug.apk
) else if "%choice%"=="2" (
    echo Building Release APK...
    call gradlew.bat assembleRelease
    echo Release APK generated at: app\build\outputs\apk\release\app-release.apk
) else if "%choice%"=="3" (
    echo Building Debug and Release APKs...
    call gradlew.bat assembleDebug assembleRelease
    echo Debug APK:   app\build\outputs\apk\debug\app-debug.apk
    echo Release APK: app\build\outputs\apk\release\app-release.apk
) else if "%choice%"=="4" (
    echo Starting Preview Web Server on http://localhost:3000 ...
    python -m http.server 3000
) else (
    echo Invalid choice.
)

pause
