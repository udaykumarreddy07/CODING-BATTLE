@echo off
REM =========================================================================
REM Coding Battle Platform - Pure Java Web & REST API Server Launcher
REM Runs 100% in Java without Node.js on port 3000
REM =========================================================================

echo [INFO] Starting Coding Battle Pure Java Web & REST API Server...
set "CURRENT_DIR=%~dp0"
set "MAVEN_CMD=%CURRENT_DIR%tools\apache-maven-3.9.16\bin\mvn.cmd"

if not exist "%MAVEN_CMD%" (
    echo [ERROR] Maven not found at %MAVEN_CMD%
    pause
    exit /b 1
)

echo [INFO] Launching JavaHttpServer on http://localhost:3000/ ...
call "%MAVEN_CMD%" exec:java -Dexec.mainClass="com.codingbattle.server.JavaHttpServer"
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Server exited with error code %ERRORLEVEL%.
    pause
)
