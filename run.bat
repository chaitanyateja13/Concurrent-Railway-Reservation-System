@echo off
echo Compiling Railway Reservation System...
echo.

REM Prefer javac on PATH; fall back to common install paths
set "JAVAC_PATH="
where javac >nul 2>&1
if %errorlevel% equ 0 (
    set "JAVAC_PATH=javac"
) else if exist "C:\Program Files\Java\jdk1.8.0_461\bin\javac.exe" (
    set "JAVAC_PATH=C:\Program Files\Java\jdk1.8.0_461\bin\javac.exe"
) else if exist "C:\Program Files (x86)\Java\jdk1.8.0_461\bin\javac.exe" (
    set "JAVAC_PATH=C:\Program Files (x86)\Java\jdk1.8.0_461\bin\javac.exe"
) else (
    echo ERROR: javac not found. Please install a JDK and add its bin to PATH.
    echo Download JDK from: https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

REM Find MySQL Connector/J jar in project root (mysql-connector-java-*.jar)
set "MYSQL_JAR="
for %%f in (mysql-connector-java*.jar) do (
    if not defined MYSQL_JAR set "MYSQL_JAR=%%~f"
)

if not defined MYSQL_JAR (
    for %%f in (lib\mysql-connector-java*.jar) do (
        if not defined MYSQL_JAR set "MYSQL_JAR=%%~f"
    )
)

if not defined MYSQL_JAR (
    echo WARNING: MySQL Connector/J jar not found in project root or in lib\ folder.
    echo If your code uses MySQL, download the connector (e.g. mysql-connector-java-8.0.33.jar)
    echo and place it in the project root or in the lib\ folder, or add it to the classpath manually.
    echo.
    echo Continuing without connector on classpath. This will fail at runtime if DB is used.
)

REM Create output directory
if not exist "out" mkdir out

REM Compile the application. If a connector jar exists, include it on the compilation classpath
if defined MYSQL_JAR (
    echo Compiling with: %JAVAC_PATH%  (including %MYSQL_JAR%)
    %JAVAC_PATH% -cp "%MYSQL_JAR%" -d out -sourcepath src src\com\rbs\App.java
) else (
REM Find MySQL Connector/J jar in project root or in lib\ folder (accepts mysql-connector*.jar)
set "MYSQL_JAR="
for %%f in (mysql-connector*.jar) do (
     if not defined MYSQL_JAR set "MYSQL_JAR=%%~f"
)
if not defined MYSQL_JAR (
     for %%f in (lib\mysql-connector*.jar) do (
          if not defined MYSQL_JAR set "MYSQL_JAR=%%~f"
     )
)

echo.
echo Compilation successful! Starting application...
echo.

REM Run the application. Include connector jar at runtime if found
if defined MYSQL_JAR (
    java -cp "out;%MYSQL_JAR%" com.rbs.App
) else (
    java -cp "out" com.rbs.App
)

echo.
echo Application closed.
pause
