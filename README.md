# Carepoint Healthcare Management

A Java web application for coordinating patient appointments and clinic operations. It uses Jakarta Servlets, JSP, JDBC, and MySQL, with Maven and Jetty for local development.

## Features

- Public Carepoint home page with separate Patients, Doctors, Clinics, and How it works pages, plus clear sign-up and sign-in paths.
- Patient registration, sign-in, contact and password updates, appointment booking/rescheduling/cancellation, doctor-shared records, patient-managed health history, and post-visit feedback.
- Doctor-managed weekly appointment agenda, minute-precision availability, appointment status updates, editable visit notes, and patient rating summaries.
- Administrator tools for creating, editing, deactivating, and deleting accounts across all roles; scheduling appointments for patients; configuring clinic identity and public contact details; setting appointment lengths; and viewing monthly activity analytics.
- Responsive public pages and workspaces with a decorative ECG trace on the home page.
- Role-scoped database queries, prepared statements, salted PBKDF2 password hashes, session ID rotation, CSRF checks, and security response headers.

## Requirements

- JDK 17 or newer.
- Maven 3.9 or newer.
- MySQL 8.0 or newer.

## Windows quick start

From the project folder, run `build.cmd`. It checks that the JDK is 17 or newer, Maven is 3.9 or newer, and the MySQL command-line client is 8.0 or newer. It then sets up the database, builds and tests the project, starts Jetty, waits for the Carepoint homepage to respond successfully, and opens `http://localhost:8081` in your default browser. The database server must also be MySQL 8.0 or newer. If the homepage does not become ready within two minutes, the launcher reports the last request error instead of opening an unavailable page. Enter your MySQL root password when prompted; the password input is hidden. The script asks for it again for the application connection. It prompts for a bootstrap administrator email and password only if the database has no admin account. These passwords are not saved in the script.

If `build.cmd` installs Java or MySQL, it pauses with a message; reopen the terminal and run it again so Windows can refresh `PATH`. Maven is downloaded only after those prerequisites are available. If Maven's download fails, the script displays the error and keeps a detailed log at `%TEMP%\carepoint-maven-install.log`. On errors, the launcher pauses before closing so you can read the message. Starting the MySQL service may require an administrator terminal. Run `build.cmd /check` to verify that Java, Maven, the MySQL client, and a MySQL Windows service are present without setting up the database or starting the app.

## Troubleshooting build and startup failures

`build.cmd` prints an `ERROR:` message and stops when a required tool is missing or below its supported version. Maven also prints the specific compiler, test, or packaging error; read the first `[ERROR]` details above the final `BUILD FAILURE` summary. Common causes and fixes:

| Error or symptom | What to check |
| --- | --- |
| JDK/Maven/MySQL client version is below the required minimum | Install JDK 17+, Maven 3.9+, and the MySQL 8.0+ command-line client. Open a new terminal so `PATH` and `JAVA_HOME` point to the intended versions, then rerun `build.cmd`. |
| `Database setup failed` or `Unknown collation: utf8mb4_0900_ai_ci` | Confirm the MySQL **server** is version 8.0 or newer, that its Windows service is running, and that the root password is correct. The client version check does not determine the server version; the schema requires MySQL 8.0's `utf8mb4_0900_ai_ci` collation. |
| `Access denied` or a connection error | Check the MySQL username/password, service status, and host/port. The launcher uses `localhost:3306`; use the manual setup instructions if your server uses another port or account. |
| Maven reports compilation or test failures | Read the first Maven `[ERROR]` output and confirm the supported JDK is selected. For database integration-test failures, confirm MySQL is available and the `DB_*` settings are correct. |
| `Address already in use` or homepage readiness timeout | Port 8081 may be occupied, or Jetty may not have started. Stop the process using that port or use the manual run command with `-Djetty.http.port=8082`; check the Jetty error output and database settings. |
| `JAVA_HOME` is invalid | Set `JAVA_HOME` to the JDK installation directory, not its `bin` directory, and make sure `JAVA_HOME\bin` is on `PATH`. |

To check the database server version independently of the client, run `mysql -u root -p -e "SELECT VERSION();"` and confirm it reports 8.0 or newer.

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

If port 8081 is already in use, run Jetty on another port with `mvn -Djetty.http.port=8082 jetty:run`, then open `http://localhost:8082`.

Stop the development server with `Ctrl+C`. MySQL runs separately and is not automatically started or stopped by the web app.

## Sign-in and scheduling

- Patient registration and general patient/admin sign-in: `/register` and `/login`.
- Doctor sign-in: `/doctor/login`. Administrator sign-in: `/admin/login`.
- After sign-in, each account is sent to its role-specific workspace.
- Doctors publish availability for a future date. Start and end times can be entered to the minute. Each availability window must be at least 30 minutes and no longer than 12 hours; it must begin in the future on the selected date. The schedule offers Morning, Afternoon, and Full day quick-fill buttons.
- Patients can request only times that fit entirely within a doctor's published availability. A booking must be in the future, and overlapping non-cancelled appointments for the same doctor are rejected.
- Administrators can schedule an appointment for an active patient using an active doctor's published availability; booking rules and conflict checks are the same as for patient bookings.
- Administrators set the clinic name, public email, phone, address, and length of new appointments in **Clinic settings**. The clinic name appears in public-page branding, and the contact details appear in the public-site footer. Available appointment lengths are 10, 15, 20, 30, 45, 60, 90, 120, and 180 minutes; the default is 30 minutes. The selected duration is saved on each new appointment, so changing this setting does not change appointments that already exist.

The home page includes a decorative ECG line that draws from left to right in a repeating six-second cycle. It is a visual effect, not patient monitoring or medical advice.

## Project structure

```text
database/schema.sql                    MySQL schema
src/main/java/com/healthcare/data      JDBC repositories and database setup
src/main/java/com/healthcare/model    Domain records
src/main/java/com/healthcare/web      Servlets, security filter, startup listener
src/main/java/com/healthcare/util     Password hashing
src/main/webapp/WEB-INF/views         JSP pages, kept outside the public web root
src/main/webapp/assets                Responsive styles, page scripts, and ECG graphic
src/test/java                         Unit tests
```

## Java web rubric guide

- **Object-oriented design:** Servlets extend `HttpServlet`; `SecurityFilter` and `DatabaseListener` implement Jakarta interfaces. Immutable Java records model users, appointments, feedback, records, and analytics.
- **Core Java:** Repository results use typed collections and generics; streams support filtering, aggregation, and dashboard calculations. Validation failures and JDBC failures use explicit exceptions and are reported by the web layer.
- **JDBC and persistence:** Repository classes use prepared statements for parameterized SQL. Appointment rescheduling and multi-setting updates use transactions.
- **Servlets and web integration:** Annotation-mapped servlets handle role-specific routes and form actions; JSP views render dashboards; the security filter applies shared request protections.
- **Tests:** Unit tests cover password hashing. The database integration test covers booking, status changes, rescheduling, medical records, patient history, user operations, analytics, and clinic settings. To run it against the local MySQL database, first set the `DB_*` connection variables as shown above, then run:

```powershell
$env:DB_INTEGRATION_TESTS = 'true'
mvn clean test package
```

The integration test creates uniquely named temporary accounts and removes its test records afterward.

## Submission checklist

- Run `mvn clean test package` and confirm it succeeds.
- Keep the public GitHub repository current and include this README.
- Include clear presentation slides with relevant screenshots or diagrams of the app, its roles, and its database flow.
- Ensure the submission link is accessible to reviewers and submit both deliverables before the deadline.

## Scope note

This is an academic demonstration project, not a production system for real patient data. A production deployment needs a security and privacy review, transport encryption, managed secrets, audit logging, backups, and applicable healthcare compliance controls.