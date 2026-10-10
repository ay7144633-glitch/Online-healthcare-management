@echo off
setlocal EnableExtensions
cd /d "%~dp0"

set "DB_HOST=localhost"
set "DB_PORT=3306"
set "DB_NAME=healthcare_management"
set "DB_USER=root"
set "MYSQL_EXE="
set "JAVAC_EXE="
set "MYSQL_SERVICE="
set "MAVEN_HOME=%LOCALAPPDATA%\Programs\Apache\apache-maven-3.9.11"
set "MAVEN_LOG=%TEMP%\carepoint-maven-install.log"
set "NEEDS_RESTART=0"

for /f "delims=" %%I in ('where javac 2^>nul') do if not defined JAVAC_EXE set "JAVAC_EXE=%%I"
if not defined JAVAC_EXE (
    where winget >nul 2>nul
    if errorlevel 1 (
        echo ERROR: Install JDK 17 or newer and make sure javac is on PATH.
        pause
        exit /b 1
    )
    echo Installing Eclipse Temurin JDK 17 with WinGet...
    winget install --id EclipseAdoptium.Temurin.17.JDK --exact --accept-source-agreements --accept-package-agreements
    if errorlevel 1 (
        echo ERROR: JDK installation failed.
        pause
        exit /b 1
    )
    set "NEEDS_RESTART=1"
)
if "%NEEDS_RESTART%"=="0" (
    for /f "tokens=2 delims==" %%I in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr /C:"java.home ="') do for /f "tokens=* delims= " %%J in ("%%I") do set "JAVA_HOME=%%J"
)
if "%NEEDS_RESTART%"=="0" (
    if not exist "%JAVA_HOME%\bin\javac.exe" (
        echo ERROR: A JDK with javac was not found. Install JDK 17 or newer and rerun.
        pause
        exit /b 1
    )
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

for /f "delims=" %%I in ('where mysql 2^>nul') do if not defined MYSQL_EXE set "MYSQL_EXE=%%I"
if not defined MYSQL_EXE (
    for %%V in (8.0 8.4) do if exist "%ProgramFiles%\MySQL\MySQL Server %%V\bin\mysql.exe" set "MYSQL_EXE=%ProgramFiles%\MySQL\MySQL Server %%V\bin\mysql.exe"
)
if not defined MYSQL_EXE (
    where winget >nul 2>nul
    if errorlevel 1 (
        echo ERROR: Install MySQL Server 8.0 or newer, including its command-line client.
        pause
        exit /b 1
    )
    echo Installing MySQL Server with WinGet. Complete any installer prompts, then rerun this file.
    winget install --id Oracle.MySQL --exact --accept-source-agreements --accept-package-agreements
    if errorlevel 1 (
        echo ERROR: MySQL installation failed.
        pause
        exit /b 1
    )
    set "NEEDS_RESTART=1"
)

if "%NEEDS_RESTART%"=="1" (
    echo.
    echo Required software was installed. Close this window, open a new terminal, and run build.cmd again.
    pause
    exit /b 0
)

where mvn >nul 2>nul
if errorlevel 1 (
    if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
        echo Downloading Apache Maven 3.9.11...
        powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $base=Join-Path $env:LOCALAPPDATA 'Programs\Apache'; New-Item -ItemType Directory -Force -Path $base | Out-Null; $url='https://archive.apache.org/dist/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.zip'; $zip=Join-Path $env:TEMP 'apache-maven-3.9.11-bin.zip'; $sum=Join-Path $env:TEMP 'apache-maven-3.9.11-bin.zip.sha512'; Invoke-WebRequest -Uri $url -OutFile $zip; Invoke-WebRequest -Uri ($url + '.sha512') -OutFile $sum; $expected=[regex]::Match((Get-Content $sum -Raw), '[A-Fa-f0-9]{128}').Value.ToLowerInvariant(); $actual=(Get-FileHash $zip -Algorithm SHA512).Hash.ToLowerInvariant(); if (-not $expected -or $actual -ne $expected) { throw 'Maven archive checksum verification failed.' }; Expand-Archive -Path $zip -DestinationPath $base -Force; Remove-Item $zip,$sum -Force" > "%MAVEN_LOG%" 2>&1
        if errorlevel 1 (
            echo ERROR: Maven download or checksum verification failed.
            echo Details are saved at "%MAVEN_LOG%":
            type "%MAVEN_LOG%"
            echo Check your internet connection and try again. The detailed log is kept for troubleshooting.
            pause
            exit /b 1
        )
    )
    set "PATH=%MAVEN_HOME%\bin;%PATH%"
)

if not defined MYSQL_EXE (
    for /f "delims=" %%I in ('where mysql 2^>nul') do if not defined MYSQL_EXE set "MYSQL_EXE=%%I"
)
if not defined MYSQL_EXE (
    echo ERROR: mysql.exe was not found. Add the MySQL Server bin folder to PATH and rerun.
    pause
    exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -Command "$version = (& javac -version 2>&1 | Out-String); if ($version -notmatch 'javac\s+(\d+)') { Write-Error ('Could not determine the JDK version: ' + $version.Trim()); exit 1 }; if ([int]$Matches[1] -lt 17) { Write-Error ('JDK 17 or newer is required; found ' + $version.Trim()); exit 1 }"
if errorlevel 1 (
    echo ERROR: Install and select JDK 17 or newer, then rerun build.cmd.
    pause
    exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -Command "$version = (& mvn -version 2>&1 | Out-String); if ($version -notmatch 'Apache Maven\s+(\d+)\.(\d+)') { Write-Error ('Could not determine the Maven version: ' + $version.Trim()); exit 1 }; $major = [int]$Matches[1]; $minor = [int]$Matches[2]; if ($major -lt 3 -or ($major -eq 3 -and $minor -lt 9)) { Write-Error ('Maven 3.9 or newer is required; found ' + $Matches[0]); exit 1 }"
if errorlevel 1 (
    echo ERROR: Install and select Maven 3.9 or newer, then rerun build.cmd.
    pause
    exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -Command "$version = (& '%MYSQL_EXE%' --version 2>&1 | Out-String); if ($version -notmatch 'Ver\s+(\d+)\.(\d+)') { Write-Error ('Could not determine the MySQL client version: ' + $version.Trim()); exit 1 }; $major = [int]$Matches[1]; $found = $Matches[0]; if ($version -notmatch 'MySQL') { Write-Error ('The MySQL 8.0 or newer client is required; found ' + $version.Trim()); exit 1 }; if ($major -lt 8) { Write-Error ('MySQL 8.0 or newer is required; found ' + $found); exit 1 }"
if errorlevel 1 (
    echo ERROR: Install and select the MySQL 8.0 or newer command-line client, then rerun build.cmd.
    pause
    exit /b 1
)

for /f "tokens=2" %%S in ('sc.exe query state^= all ^| findstr /R /C:"SERVICE_NAME: MySQL"') do if not defined MYSQL_SERVICE set "MYSQL_SERVICE=%%S"
if not defined MYSQL_SERVICE (
    echo ERROR: No MySQL Windows service was found. Install and configure MySQL Server first.
    pause
    exit /b 1
)
if /I "%~1"=="/check" (
    echo Java home: %JAVA_HOME%
    call mvn -version
    if errorlevel 1 exit /b 1
    "%MYSQL_EXE%" --version
    if errorlevel 1 exit /b 1
    echo Prerequisite check passed. Database setup and application startup were skipped.
    exit /b 0
)

powershell -NoProfile -ExecutionPolicy Bypass -Command "$service = Get-Service -Name '%MYSQL_SERVICE%'; if ($service.Status -ne 'Running') { Start-Service -Name $service.Name }"
if errorlevel 1 (
    echo ERROR: Could not start MySQL service '%MYSQL_SERVICE%'. Start it from Services or run this file as an administrator.
    pause
    exit /b 1
)

if not exist "database\schema.sql" (
    echo ERROR: database\schema.sql was not found.
    pause
    exit /b 1
)
echo.
echo Creating the database and tables. When prompted, type your MySQL root password and press Enter; input is hidden.
set "SCHEMA_TEMP=%TEMP%\healthcare-schema-%RANDOM%-%RANDOM%.sql"
set "ADMIN_COUNT_FILE=%TEMP%\healthcare-admin-count-%RANDOM%-%RANDOM%.txt"
copy /b "database\schema.sql" "%SCHEMA_TEMP%" >nul
if errorlevel 1 (
    echo ERROR: Could not prepare the database schema.
    pause
    exit /b 1
)
>>"%SCHEMA_TEMP%" echo SELECT COUNT(*) FROM users WHERE role='ADMIN';
"%MYSQL_EXE%" --batch --skip-column-names -u "%DB_USER%" -p -P "%DB_PORT%" < "%SCHEMA_TEMP%" > "%ADMIN_COUNT_FILE%"
set "MYSQL_EXIT=%ERRORLEVEL%"
del /q "%SCHEMA_TEMP%" >nul 2>nul
if not "%MYSQL_EXIT%"=="0" (
    del /q "%ADMIN_COUNT_FILE%" >nul 2>nul
    echo ERROR: Database setup failed. Check the MySQL password, service, and server version.
    echo This project requires MySQL Server 8.0 or newer; older versions do not support its database collation.
    echo See README.md under "Troubleshooting build and startup failures" for help.
    pause
    exit /b 1
)
set /p "ADMIN_COUNT="<"%ADMIN_COUNT_FILE%"
del /q "%ADMIN_COUNT_FILE%" >nul 2>nul
if not defined ADMIN_COUNT (
    echo ERROR: Could not check for an existing admin account.
    pause
    exit /b 1
)
if "%ADMIN_COUNT%"=="0" (
    set "BOOTSTRAP_ADMIN_NEEDED=1"
) else (
    set "BOOTSTRAP_ADMIN_NEEDED=0"
    echo An admin account already exists. The admin email and password prompts will be skipped.
)

echo.
echo Building and running tests...
call mvn clean test package
if errorlevel 1 (
    echo ERROR: Maven build or tests failed.
    echo See README.md under "Troubleshooting build and startup failures" for common causes.
    pause
    exit /b 1
)

echo.
echo Jetty will start at http://localhost:8081
if "%BOOTSTRAP_ADMIN_NEEDED%"=="1" echo The next prompts create an application admin account in the database; use an email and password for that account.
powershell -NoProfile -ExecutionPolicy Bypass -Command "$db = Read-Host 'MySQL root password for the application' -AsSecureString; $adminEmail = ''; $adminValue = ''; $admin = $null; if ('%BOOTSTRAP_ADMIN_NEEDED%' -eq '1') { $adminEmail = Read-Host 'Bootstrap admin email [admin@carepoint.local]'; if ([string]::IsNullOrWhiteSpace($adminEmail)) { $adminEmail = 'admin@carepoint.local' }; $admin = Read-Host 'Bootstrap admin password (at least 8 characters)' -AsSecureString }; $dbPtr = [IntPtr]::Zero; $adminPtr = [IntPtr]::Zero; try { $dbPtr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($db); if ($admin) { $adminPtr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($admin); $adminValue = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($adminPtr); if ($adminValue.Length -lt 8) { throw 'The bootstrap admin password must be at least 8 characters.' } }; $env:DB_HOST = '%DB_HOST%'; $env:DB_PORT = '%DB_PORT%'; $env:DB_NAME = '%DB_NAME%'; $env:DB_USER = '%DB_USER%'; $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($dbPtr); if ($admin) { $env:BOOTSTRAP_ADMIN_EMAIL = $adminEmail; $env:BOOTSTRAP_ADMIN_PASSWORD = $adminValue } else { Remove-Item Env:\BOOTSTRAP_ADMIN_EMAIL,Env:\BOOTSTRAP_ADMIN_PASSWORD -ErrorAction SilentlyContinue }; $server = Start-Process -FilePath $env:ComSpec -ArgumentList '/d /c mvn jetty:run' -WorkingDirectory (Get-Location).Path -NoNewWindow -PassThru; try { $ready = $false; $lastError = 'No response received yet.'; $deadline = [DateTime]::UtcNow.AddMinutes(2); while ([DateTime]::UtcNow -lt $deadline) { if ($server.WaitForExit(500)) { exit $server.ExitCode }; $response = $null; try { $request = [Net.HttpWebRequest]::Create('http://127.0.0.1:8081/'); $request.Timeout = 2000; $request.ReadWriteTimeout = 2000; $request.Proxy = $null; $response = $request.GetResponse(); $reader = New-Object IO.StreamReader($response.GetResponseStream()); try { $content = $reader.ReadToEnd() } finally { $reader.Dispose() }; if ([int]$response.StatusCode -eq 200 -and $content.Contains('<title>Carepoint | Healthcare, connected</title>')) { $ready = $true; Start-Process 'http://localhost:8081'; break }; $lastError = 'Unexpected HTTP ' + [int]$response.StatusCode } catch { $lastError = $_.Exception.Message } finally { if ($response) { $response.Dispose() } } }; if (-not $ready) { throw ('Carepoint did not return its homepage within two minutes. Last request: ' + $lastError + ' Check the MySQL service and DB_* settings, then review the Jetty startup errors above.') }; $server.WaitForExit(); exit $server.ExitCode } finally { if (-not $server.HasExited) { Stop-Process -Id $server.Id -Force } } } finally { if ($dbPtr -ne [IntPtr]::Zero) { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($dbPtr) }; if ($adminPtr -ne [IntPtr]::Zero) { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($adminPtr) }; $db.Dispose(); if ($admin) { $admin.Dispose() }; Remove-Item Env:\DB_PASSWORD,Env:\BOOTSTRAP_ADMIN_PASSWORD -ErrorAction SilentlyContinue }"
exit /b %ERRORLEVEL%
