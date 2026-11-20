@echo off
setlocal enabledelayedexpansion
title Smart Campus - Start All
set TOOLS=C:\Users\ABU TALHA\tools
set APP=%~dp0..
set JAVA_HOME=%TOOLS%\jdk-21.0.13.1+2
set PATH=%JAVA_HOME%\bin;%PATH%

echo ============================================================
echo   SMART CAMPUS - ek click me sab kuch start
echo ============================================================
echo.

echo [1/3] Database check (port 3307) ...
netstat -ano | findstr ":3307" | findstr "LISTENING" >nul
if errorlevel 1 (
  echo       MariaDB start ho raha hai...
  start "MariaDB 3307" /min "%TOOLS%\mariadb-11.8.10-winx64\bin\mariadbd.exe" --defaults-file="%TOOLS%\mariadb.ini"
  timeout /t 6 >nul
) else (
  echo       Already running.
)

echo [2/3] Backend check (port 8080) ...
netstat -ano | findstr ":8080" | findstr "LISTENING" >nul
if errorlevel 1 (
  echo       Backend start ho raha hai...
  start "Smart Campus Backend" /min cmd /c ""%JAVA_HOME%\bin\java.exe" -jar "%APP%\backend\target\smart-campus-backend-1.0.0.jar" > "%APP%\backend\backend.log" 2>&1"
  timeout /t 10 >nul
) else (
  echo       Already running.
)
curl -s --max-time 5 http://localhost:8080/api/health
echo.
echo.

echo [3/3] Public tunnel start ho raha hai (naya window khulega)...
start "Smart Campus Tunnel" cmd /c ""%~dp0start-public.bat""
timeout /t 8 >nul
if exist "%APP%\tunnel-url.txt" (
  echo.
  echo ============================================================
  echo   PUBLIC URL: %APP%\tunnel-url.txt me saved hai:
  type "%APP%\tunnel-url.txt"
  echo   Isi URL ko app ke Server URL field me daalo.
  echo ============================================================
)
echo.
echo Sab start ho gaya. Tunnel wali window band MAT karna.
pause
