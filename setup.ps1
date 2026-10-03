# Zero-Touch Prerequisites Setup Script for Windows
# Requires Administrator privileges for system-wide PATH updates and package installation.

$ErrorActionPreference = "Continue"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Campus Event Management System - Environment Setup Script" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# Function to install a package via Winget
function Install-WingetPackage {
    param (
        [string]$PackageId,
        [string]$Name
    )

    Write-Host "[*] Checking/Installing $Name ($PackageId)..." -ForegroundColor Yellow
    try {
        winget install --id $PackageId --silent --accept-source-agreements --accept-package-agreements
        Write-Host "[+] Installation process finished for $Name." -ForegroundColor Green
    } catch {
        Write-Host "[!] Winget installation attempt for $Name finished with notice: $_" -ForegroundColor DarkYellow
    }
}

# 1. Install Microsoft OpenJDK 21 and Apache Maven
Install-WingetPackage -PackageId "Microsoft.OpenJDK.21" -Name "Microsoft OpenJDK 21"
Install-WingetPackage -PackageId "Apache.Maven" -Name "Apache Maven"

# 2. Automatically refresh environment variables in current PowerShell session
Write-Host "[*] Refreshing Environment PATH Variables..." -ForegroundColor Yellow

# Get Machine and User PATHs from Registry
$machinePath = [Environment]::GetEnvironmentVariable("Path", [EnvironmentVariableTarget]::Machine)
$userPath = [Environment]::GetEnvironmentVariable("Path", [EnvironmentVariableTarget]::User)

# Combine machine and user paths
$combinedPath = "$machinePath;$userPath"
$env:Path = $combinedPath

# Search for JDK and Maven bin directories across common installation paths
$searchPatterns = @(
    "C:\Program Files\Microsoft\jdk-21*\bin",
    "C:\Program Files\Java\jdk-21*\bin",
    "C:\Program Files\Apache\maven-*\bin",
    "C:\Program Files\Apache\apache-maven-*\bin",
    "C:\Program Files\Maven\apache-maven-*\bin",
    "C:\Program Files\Apache Software Foundation\apache-maven-*\bin",
    "C:\apache-maven-*\bin",
    "C:\tools\apache-maven-*\bin",
    "C:\ProgramData\chocolatey\bin"
)

foreach ($pattern in $searchPatterns) {
    $foundDirs = Get-ChildItem -Path $pattern -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName
    if ($foundDirs) {
        foreach ($dir in $foundDirs) {
            if ($env:Path -notlike "*$dir*") {
                Write-Host "[+] Adding discovered tool directory to current PATH: $dir" -ForegroundColor Green
                $env:Path = "$dir;$env:Path"
            }
        }
    }
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Verifying Installed Tooling Versions" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$javaFound = $false
try {
    Write-Host "Java Version:" -ForegroundColor Yellow
    java -version
    $javaFound = $true
} catch {
    Write-Host "[!] Java command not found in current PATH." -ForegroundColor Red
}

$mvnFound = $false
try {
    Write-Host "`nMaven Version:" -ForegroundColor Yellow
    mvn -version
    $mvnFound = $true
} catch {
    Write-Host "[!] Maven command ('mvn') not found in current PATH." -ForegroundColor DarkYellow
}

if (-not $mvnFound -and (Test-Path ".\mvnw.cmd")) {
    Write-Host "[+] Maven Wrapper (mvnw.cmd) is available in the repository root and ready for deployment!" -ForegroundColor Green
}

Write-Host "`n[+] Environment Setup Complete! You can now run .\deploy.ps1 to build and launch the application." -ForegroundColor Green
