@echo off
start "MariaDB 3307" /min "C:\Users\ABU TALHA\tools\mariadb-11.8.10-winx64\bin\mariadbd.exe" --defaults-file="C:\Users\ABU TALHA\tools\mariadb.ini"
echo MariaDB starting on port 3307 (minimized window)...
timeout /t 3 >nul
"C:\Users\ABU TALHA\tools\mariadb-11.8.10-winx64\bin\mariadb.exe" -uroot -h127.0.0.1 -P3307 -e "SELECT VERSION();"
