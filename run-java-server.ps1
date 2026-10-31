# =========================================================================
# Coding Battle Platform - Pure Java Web & REST API Server Launcher
# Runs 100% in Java without Node.js on port 3000
# =========================================================================

Write-Host "[INFO] Starting Coding Battle Pure Java Web & REST API Server..." -ForegroundColor Cyan

$CurrentDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$MavenCmd = Join-Path $CurrentDir "tools\apache-maven-3.9.16\bin\mvn.cmd"

if (-not (Test-Path $MavenCmd)) {
    Write-Host "[ERROR] Maven not found at $MavenCmd" -ForegroundColor Red
    exit 1
}

Write-Host "[INFO] Launching JavaHttpServer on http://localhost:3000/ ..." -ForegroundColor Green
& $MavenCmd exec:java -Dexec.mainClass="com.codingbattle.server.JavaHttpServer"
