# Carepoint Healthcare Management

A Java web application for coordinating patient appointments and clinic operations. It uses Jakarta Servlets, JSP, JDBC, and MySQL, with Maven and Jetty for local development.

## Features

- Public Carepoint home page with separate Patients, Doctors, Clinics, and How it works pages, plus clear sign-up and sign-in paths.
- Patient registration, sign-in, contact and password updates, appointment booking/rescheduling/cancellation, doctor-shared records, patient-managed health history, and post-visit feedback.
- Doctor-managed weekly appointment agenda and availability, appointment status updates, editable visit notes, and patient rating summaries.
- Administrator tools for account creation/editing/deletion, access and role management, appointment rescheduling, clinic settings, and monthly activity analytics.
- Role-scoped database queries, prepared statements, salted PBKDF2 password hashes, session ID rotation, CSRF checks, and security response headers.

## Requirements

- JDK 17 or newer.
- Maven 3.9 or newer.
- MySQL 8.0 or newer.

## Database setup

Start MySQL when you are ready to use the application. In Windows, open `services.msc`, select **MySQL80**, and choose **Start** if it is stopped. MySQL is expected on port 3306; if your installation uses port 3307, use 3307 in the commands and environment below.

Create the database and tables from the project root. In PowerShell, use the MySQL client installed with MySQL Server:

```powershell
Get-Content .\database\schema.sql | & 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' -u root -p -P 3306
```

Set the non-secret database connection values for the current PowerShell session:

```powershell
$env:DB_HOST = 'localhost'
$env:DB_PORT = '3306'
$env:DB_NAME = 'healthcare_management'
$env:DB_USER = 'root'
```

If Jetty reports `Access denied for user 'root'@'localhost' (using password: NO)`, MySQL is reachable but the app was not given your MySQL password. Enter the password you chose during MySQL setup when starting Jetty below. Do not use the placeholder `your-local-mysql-password` or commit credentials to the repository. If you do not know the root password, reset it using the MySQL Installer first.

To create an administrator account on first startup, set these before running the application. Use a strong local password. The application creates this account only if the email does not already exist.

```powershell
$env:BOOTSTRAP_ADMIN_EMAIL = 'admin@carepoint.local'
$env:BOOTSTRAP_ADMIN_PASSWORD = 'replace-with-a-strong-password'
```

The application also creates missing tables when it starts. The database itself must already exist. If you initially start without the bootstrap administrator variables, set them in PowerShell and restart the app; the administrator account is created on startup if its email is not already registered.

## Run locally

If Maven reports that `JAVA_HOME` is invalid, point it at the JDK directory, not its `bin` folder. In the same PowerShell window where you set the `DB_*` values, run:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-27'
mvn clean test package
$securePassword = Read-Host 'MySQL root password' -AsSecureString
$passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
try {
	$env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
	[Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
	$passwordPointer = [IntPtr]::Zero
	$securePassword.Dispose()
	mvn jetty:run
} finally {
	if ($passwordPointer -ne [IntPtr]::Zero) {
		[Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
	}
	Remove-Item Env:\DB_PASSWORD -ErrorAction SilentlyContinue
}
```

Open [http://localhost:8081](http://localhost:8081) to see the public home page. Visitors can choose **Log in** or **Create account**. Use the bootstrapped admin credentials or register a patient account. Administrators can add doctor accounts; doctors can then publish availability for patient booking.

Stop the development server with `Ctrl+C`. MySQL runs separately and is not automatically started or stopped by the web app.

## Project structure

```text
database/schema.sql                    MySQL schema
src/main/java/com/healthcare/data      JDBC repositories and database setup
src/main/java/com/healthcare/model    Domain records
src/main/java/com/healthcare/web      Servlets, security filter, startup listener
src/main/java/com/healthcare/util     Password hashing
src/main/webapp/WEB-INF/views         JSP pages, kept outside the public web root
src/main/webapp/assets                Responsive application styles
src/test/java                         Unit tests
```

## Submission checklist

- Run `mvn clean test package` and confirm it succeeds.
- Keep the public GitHub repository current and include this README.
- Include clear presentation slides with relevant screenshots or diagrams of the app, its roles, and its database flow.
- Ensure the submission link is accessible to reviewers and submit both deliverables before the deadline.

## Scope note

This is an academic demonstration project, not a production system for real patient data. A production deployment needs a security and privacy review, transport encryption, managed secrets, audit logging, backups, and applicable healthcare compliance controls.