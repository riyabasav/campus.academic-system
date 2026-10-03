# Campus Event Management System - Windows Zero-Touch Automation Guide

This guide explains how to set up, build, test, and run the Campus Event & Club Activity Management System on Windows using automated PowerShell scripts.

---

## Prerequisites & Installation (`setup.ps1`)

The `setup.ps1` script automatically installs the required dependencies silently using Windows Package Manager (`winget`) and updates process environment variables.

### Dependencies Installed:
- **Microsoft OpenJDK 21** (`Microsoft.OpenJDK.21`)
- **Apache Maven** (`Apache.Maven`)

---

## How to Run Automation Scripts

### Step 1: Open PowerShell as Administrator
1. Press `Win + X` and select **Terminal (Admin)** or **PowerShell (Admin)**.

### Step 2: Set Execution Policy
Allow PowerShell script execution for the current session:
```powershell
Set-ExecutionPolicy Unrestricted -Scope Process
```

### Step 3: Run Environment Setup Script
Run the prerequisite auto-installer script:
```powershell
.\setup.ps1
```
*This installs OpenJDK 21 and Maven silently and refreshes `PATH` environment variables.*

### Step 4: Run Automated Build, Test & Deployment Script
Run the automated build and deployment script:
```powershell
.\deploy.ps1
```
*This script automatically:*
1. Compiles the source code and executes all unit and Playwright Java E2E UI automated tests (`mvn clean package`).
2. Packages the application into a standalone runnable JAR file inside `target/`.
3. Starts the Spring Boot application asynchronously in the background.
4. Waits for the application server to start listening at `http://localhost:8080/`.
5. Launches your default web browser automatically to `http://localhost:8080/`.

---

## Accessing Application Features

- **Event Dashboard & Creation:** `http://localhost:8080/`
- **H2 In-Memory Database Console:** `http://localhost:8080/h2-console`
  - **JDBC URL:** `jdbc:h2:mem:campusdb`
  - **User:** `sa`
  - **Password:** *(leave empty)*
