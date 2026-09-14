# =========================================================================
# Coding Battle Platform - PowerShell One-Click Launcher
# =========================================================================

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$MavenCmd = Join-Path $ScriptDir "tools\apache-maven-3.9.16\bin\mvn.cmd"

Write-Host "[INFO] Starting Coding Battle Platform..." -ForegroundColor Cyan

if (-not (Test-Path $MavenCmd)) {
    Write-Host "[ERROR] Maven executable not found at $MavenCmd" -ForegroundColor Red
    exit 1
}

Write-Host "[INFO] Running: mvn javafx:run" -ForegroundColor Green
& $MavenCmd javafx:run
