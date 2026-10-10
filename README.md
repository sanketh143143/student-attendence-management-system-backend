# Student Attendance Management System — Java + MySQL

## Included
- Java Swing desktop UI
- Login, student CRUD, attendance entry and subject-wise percentage reports
- MySQL schema and sample data
- PDF project report

## Requirements
- JDK 17+
- MySQL Server 8+
- Maven 3.8+
- IDE such as IntelliJ IDEA, Eclipse or VS Code

## Setup
1. Start MySQL Server.
2. Run `database/schema.sql` in MySQL Workbench or MySQL CLI.
3. Open `src/main/java/com/attendance/Main.java` and change `DB_USER` / `DB_PASSWORD`. Replace `YOUR_MYSQL_PASSWORD` with your MySQL password.
4. Open a terminal in this project folder (where `pom.xml` is located).
5. Run `mvn clean compile`
6. Run `mvn exec:java`
7. Login using username `admin` and password `admin123`.

## Demonstration
1. Login.
2. In Students, add a student (example: AIML004 / Meena / CSE-AIML / III Year).
3. In Mark Attendance, select that student, enter a subject and date in YYYY-MM-DD format, select Present, and save.
4. Add another attendance entry (different date or subject), perhaps with status Absent.
5. In Reports, click Show All or search by roll number to see counts and percentages.
6. Select a student in Students to demonstrate Update Selected or Delete Selected.

## Attendance formula
Attendance % = (Present records / Total attendance records) × 100, calculated separately for each subject.

## Troubleshooting
- Access denied: check DB_USER and DB_PASSWORD.
- Unknown database: run `database/schema.sql`.
- Communications link failure: start MySQL and confirm port 3306.
- Duplicate roll number: choose a unique roll number.
- Date error: use YYYY-MM-DD.

## Security note
This is a classroom prototype. The demo password is stored as plain text and is not suitable for production. A real system should hash passwords, implement role-based access, validate inputs, and use backups and audit logs.
