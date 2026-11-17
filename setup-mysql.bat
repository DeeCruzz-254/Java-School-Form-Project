@echo off
REM ============================================================
REM  One-shot MySQL setup for the Student Information app
REM  Run this from the folder that contains StudentDAO.java
REM ============================================================
set "SRC=%USERPROFILE%\Downloads\mysql-8.0.46-winx64\mysql-8.0.46-winx64"
set "MYSQL=C:\mysql"

echo [1/5] Copying MySQL to %MYSQL% ...
if not exist "%MYSQL%\bin\mysqld.exe" (
    if not exist "%SRC%\bin\mysqld.exe" (
        echo ERROR: MySQL folder not found at "%SRC%"
        echo Edit the SRC line in this file to point to your extracted folder.
        pause & exit /b 1
    )
    xcopy "%SRC%" "%MYSQL%" /E /I /Q /Y >nul
)

echo [2/5] Initializing data folder ...
if not exist "%MYSQL%\data" (
    "%MYSQL%\bin\mysqld.exe" --initialize-insecure --console
    if errorlevel 1 (
        echo ERROR: initialization failed. Install the Microsoft Visual C++ Redistributable x64 and retry.
        pause & exit /b 1
    )
)

echo [3/5] Starting MySQL server in a minimized window ...
"%MYSQL%\bin\mysqladmin.exe" -u root ping >nul 2>&1
if errorlevel 1 start "MySQL Server - do not close" /MIN "%MYSQL%\bin\mysqld.exe" --console

for /L %%i in (1,1,30) do (
    "%MYSQL%\bin\mysqladmin.exe" -u root ping >nul 2>&1 && goto ready
    timeout /t 2 /nobreak >nul
)
echo ERROR: server did not start in time.
pause & exit /b 1

:ready
echo [4/5] Creating database, table and user ...
> "%TEMP%\init.sql" echo CREATE DATABASE IF NOT EXISTS student_db;
>> "%TEMP%\init.sql" echo CREATE TABLE IF NOT EXISTS student_db.students (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(40) NOT NULL, admission_no VARCHAR(15) NOT NULL UNIQUE, course VARCHAR(60) NOT NULL, gender VARCHAR(10) NOT NULL, registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
>> "%TEMP%\init.sql" echo CREATE USER IF NOT EXISTS 'student'@'localhost' IDENTIFIED BY 'StudentPass123';
>> "%TEMP%\init.sql" echo GRANT ALL PRIVILEGES ON student_db.* TO 'student'@'localhost';
>> "%TEMP%\init.sql" echo FLUSH PRIVILEGES;
"%MYSQL%\bin\mysql.exe" -u root < "%TEMP%\init.sql"
if errorlevel 1 (
    echo ERROR: could not run the SQL setup.
    pause & exit /b 1
)

echo [5/5] Downloading the JDBC driver ...
if not exist "%~dp0mysql-connector-j-8.4.0.jar" (
    curl -fL -o "%~dp0mysql-connector-j-8.4.0.jar" https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar
)

echo.
echo ===== DONE =====
echo Now run:
echo   javac *.java
echo   java -cp ".;mysql-connector-j-8.4.0.jar" Main
pause
