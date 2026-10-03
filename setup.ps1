# Zero-Touch Prerequisites Setup Script for Windows
# Requires Administrator privileges for system-wide PATH updates and package installation.

$ErrorActionPreference = "Stop"

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
        Write-Host "[!] Winget installation for $Name encountered an notice/warning: $_" -ForegroundColor DarkYellow
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

# Update current process PATH
$env:Path = $combinedPath

# Check common default installation locations if not found
$jdkDefaultPath = "C:\Program Files\Microsoft\jdk-21*\bin"
$mvnDefaultPath = "C:\Program Files\Apache\maven-*\bin", "C:\Program Files\Maven\apache-maven-*\bin"

$resolvedJdkPaths = Get-ChildItem -Path $jdkDefaultPath -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName
if ($resolvedJdkPaths) {
    foreach ($p in $resolvedJdkPaths) {
        if ($env:Path -notlike "*$p*") {
            $env:Path = "$p;$env:Path"
        }
    }
}

$resolvedMvnPaths = Get-ChildItem -Path $mvnDefaultPath -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName
if ($resolvedMvnPaths) {
    foreach ($p in $resolvedMvnPaths) {
        if ($env:Path -notlike "*$p*") {
            $env:Path = "$p;$env:Path"
        }
    }
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Verifying Installed Tooling Versions" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

try {
    Write-Host "Java Version:" -ForegroundColor Yellow
    java -version
} catch {
    Write-Host "[!] Java not found in current PATH. Please restart terminal or verify OpenJDK 21 installation." -ForegroundColor Red
}

try {
    Write-Host "`nMaven Version:" -ForegroundColor Yellow
    mvn -version
} catch {
    Write-Host "[!] Maven not found in current PATH. Please restart terminal or verify Maven installation." -ForegroundColor Red
}

Write-Host "`n[+] Environment Setup Complete! You can now run .\deploy.ps1 to build and launch the application." -ForegroundColor Green
