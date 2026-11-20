@echo off
rem Render free service 15 min inactivity ke baad so jata hai.
rem Yeh script har 10 min me health ping karke use jaga ke rakhta hai.
set URL_FILE=%~dp0..\cloud-url.txt
if not exist "%URL_FILE%" (
  echo cloud-url.txt nahi mila. Pehle cloud deploy karo aur us URL ko
  echo %URL_FILE% me likho.
  pause
  exit /b 1
)
set /p CLOUD_URL=<"%URL_FILE%"
echo Keep-alive chal raha hai: %CLOUD_URL%  (window band MAT karna)
:loop
curl -s --max-time 40 "%CLOUD_URL%/api/health" >nul
timeout /t 600 >nul
goto loop
