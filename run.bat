@echo off
REM =========================================================================
REM Coding Battle Platform - One-Click Launcher
REM Uses local Maven and system Java
REM =========================================================================

echo [INFO] Starting Coding Battle Platform...
set "CURRENT_DIR=%~dp0"
set "MAVEN_CMD=%CURRENT_DIR%tools\apache-maven-3.9.16\bin\mvn.cmd"

if not exist "%MAVEN_CMD%" (
    echo [ERROR] Maven not found at %MAVEN_CMD%
    pause
    exit /b 1
)

echo [INFO] Launching JavaFX Application...
call "%MAVEN_CMD%" javafx:run
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Application exited with error code %ERRORLEVEL%.
    pause
)
