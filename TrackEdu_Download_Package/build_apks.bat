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
    if exist "app\build\outputs\apk\debug\app-debug.apk" (
        if not exist "apk" mkdir "apk"
        if not exist "TrackEdu_Download_Package\apk" mkdir "TrackEdu_Download_Package\apk"
        copy /Y "app\build\outputs\apk\debug\app-debug.apk" "apk\TrackEdu-v1.0-debug.apk" >nul
        copy /Y "app\build\outputs\apk\debug\app-debug.apk" "apk\app-debug.apk" >nul
        copy /Y "app\build\outputs\apk\debug\app-debug.apk" "TrackEdu_Download_Package\apk\TrackEdu-v1.0-debug.apk" >nul
        copy /Y "app\build\outputs\apk\debug\app-debug.apk" "TrackEdu_Download_Package\apk\app-debug.apk" >nul
        echo [SUCCESS] Debug APK copied to apk\ and TrackEdu_Download_Package\apk\
    )
) else if "%choice%"=="2" (
    echo Building Signed Release APK...
    call gradlew.bat assembleRelease
    if exist "app\build\outputs\apk\release\app-release.apk" (
        if not exist "apk" mkdir "apk"
        if not exist "TrackEdu_Download_Package\apk" mkdir "TrackEdu_Download_Package\apk"
        copy /Y "app\build\outputs\apk\release\app-release.apk" "apk\TrackEdu-v1.0-release.apk" >nul
        copy /Y "app\build\outputs\apk\release\app-release.apk" "apk\app-release.apk" >nul
        copy /Y "app\build\outputs\apk\release\app-release.apk" "TrackEdu_Download_Package\apk\TrackEdu-v1.0-release.apk" >nul
        copy /Y "app\build\outputs\apk\release\app-release.apk" "TrackEdu_Download_Package\apk\app-release.apk" >nul
        echo [SUCCESS] Signed Release APK copied to apk\ and TrackEdu_Download_Package\apk\
    )
) else if "%choice%"=="3" (
    echo Building Both Debug and Signed Release APKs...
    call gradlew.bat assembleDebug assembleRelease
    if not exist "apk" mkdir "apk"
    if not exist "TrackEdu_Download_Package\apk" mkdir "TrackEdu_Download_Package\apk"
    if exist "app\build\outputs\apk\debug\app-debug.apk" (
        copy /Y "app\build\outputs\apk\debug\app-debug.apk" "apk\TrackEdu-v1.0-debug.apk" >nul
        copy /Y "app\build\outputs\apk\debug\app-debug.apk" "apk\app-debug.apk" >nul
        copy /Y "app\build\outputs\apk\debug\app-debug.apk" "TrackEdu_Download_Package\apk\TrackEdu-v1.0-debug.apk" >nul
        copy /Y "app\build\outputs\apk\debug\app-debug.apk" "TrackEdu_Download_Package\apk\app-debug.apk" >nul
    )
    if exist "app\build\outputs\apk\release\app-release.apk" (
        copy /Y "app\build\outputs\apk\release\app-release.apk" "apk\TrackEdu-v1.0-release.apk" >nul
        copy /Y "app\build\outputs\apk\release\app-release.apk" "apk\app-release.apk" >nul
        copy /Y "app\build\outputs\apk\release\app-release.apk" "TrackEdu_Download_Package\apk\TrackEdu-v1.0-release.apk" >nul
        copy /Y "app\build\outputs\apk\release\app-release.apk" "TrackEdu_Download_Package\apk\app-release.apk" >nul
    )
    echo [SUCCESS] All APKs built and copied to apk\ and TrackEdu_Download_Package\apk\
) else if "%choice%"=="4" (
    echo Starting Preview Web Server on http://localhost:3000 ...
    python -m http.server 3000
) else (
    echo Invalid choice.
)

pause
