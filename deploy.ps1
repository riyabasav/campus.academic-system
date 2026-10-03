# Zero-Touch Deployment & Application Launch Script for Windows
$ErrorActionPreference = "Stop"

# Configure Playwright alternate CDN mirror in case default azureedge.net CDN is blocked
$env:PLAYWRIGHT_DOWNLOAD_HOST = "https://cdn.playwright.dev"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Campus Event Management System - Automated Build & Deploy" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Determine Maven Command (Prefer Maven Wrapper if present, else fallback to installed mvn)
$mvnCmd = "mvn"
if (Test-Path ".\mvnw.cmd") {
    $mvnCmd = ".\mvnw.cmd"
}

Write-Host "[*] Compiling, running tests, and packaging application using $mvnCmd..." -ForegroundColor Yellow
& $mvnCmd clean package

if ($LASTEXITCODE -ne 0) {
    Write-Host "[!] Build or automated Playwright tests failed! Halting deployment." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "[+] Build and package completed successfully." -ForegroundColor Green

# 2. Locate generated runnable JAR file in target/
$jarFile = Get-ChildItem -Path "target" -Filter "*.jar" | Where-Object { $_.Name -notlike "*sources.jar" -and $_.Name -notlike "*javadoc.jar" } | Select-Object -First 1

if (-not $jarFile) {
    Write-Host "[!] Could not locate packaged JAR file in target directory." -ForegroundColor Red
    exit 1
}

Write-Host "[+] Found application JAR: $($jarFile.FullName)" -ForegroundColor Green

# 3. Launch Spring Boot application process
Write-Host "[*] Launching Spring Boot Application in Background..." -ForegroundColor Yellow
$appProcess = Start-Process -FilePath "java" -ArgumentList "-jar", "`"$($jarFile.FullName)`"" -PassThru -NoNewWindow

Write-Host "[+] Process started with PID: $($appProcess.Id)" -ForegroundColor Green

# 4. Wait for application to start and port 8080 to become active
$targetUrl = "http://localhost:8080/"
Write-Host "[*] Waiting for server to start listening at $targetUrl..." -ForegroundColor Yellow

$maxAttempts = 30
$attempt = 0
$serverReady = $false

while ($attempt -lt $maxAttempts) {
    $attempt++
    try {
        $response = Invoke-WebRequest -Uri $targetUrl -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            $serverReady = $true
            break
        }
    } catch {
        # Server not ready yet
    }
    Start-Sleep -Seconds 1
}

if ($serverReady) {
    Write-Host "[+] Server is live and responding!" -ForegroundColor Green
    Write-Host "[*] Opening default web browser to $targetUrl..." -ForegroundColor Yellow
    Start-Process $targetUrl
} else {
    Write-Host "[!] Server did not start listening on port 8080 within timeout. Please check logs." -ForegroundColor Red
}
