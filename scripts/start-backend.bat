@echo off
cd /d "%~dp0..\backend"
set JAVA_HOME=C:\Users\ABU TALHA\tools\jdk-21.0.13.1+2
set PATH=%JAVA_HOME%\bin;%PATH%
start "Smart Campus Backend" java -jar target\smart-campus-backend-1.0.0.jar
echo Backend starting on http://localhost:8080 ...
timeout /t 6 >nul
curl -s http://localhost:8080/api/health
