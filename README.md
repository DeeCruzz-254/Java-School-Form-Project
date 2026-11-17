# Student Information Interface

Event-driven student registration desktop app built with **Java Swing** and **MySQL**, demonstrating Action, Item, Key, Window and Mouse events.

Users enter a student's name, admission number, course and gender, then register them. Input is validated, duplicate admission numbers are rejected, feedback is shown in a display area, and records are stored in a MySQL database through JDBC. If the database is unavailable, the app keeps working in memory only.

## Features

- Form with name, admission number, course (combo box) and gender (radio buttons)
- Register, Clear and Exit buttons
- Feedback display area with timestamps, plus a status bar
- Input validation and live character counters
- MySQL persistence with duplicate detection across sessions
- Graceful fallback to in-memory mode if MySQL is not running

## Events Demonstrated

| Event | Where it is used |
|---|---|
| `ActionEvent` | Register, Clear and Exit buttons; Enter in the name field moves focus on |
| `ItemEvent` | Course combo box and gender radio buttons update the status bar (only on `SELECTED`) |
| `KeyEvent` | Blocks disallowed characters in the name and admission fields; live character counters |
| `WindowEvent` | Exit confirmation dialog (`windowClosing`) and initial focus (`windowOpened`) |
| `MouseEvent` | Button hover colours; double-click on the display area clears the log |

The Exit button dispatches a window-closing event, so the button and the window's X share one confirmation handler.

## Project Structure

```
Main.java              Entry point; starts the UI on the Event Dispatch Thread
Student.java           Model: name, admission number, course, gender
StudentFormFrame.java  View and controller: components, listeners, validation
StudentDAO.java        Data access: all JDBC / SQL code
setup.sql              Creates the database and table
```

## Requirements

- JDK 11 or later (developed with JDK 21)
- MySQL Server 8.x
- [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) (JDBC driver) jar

## Setup

### 1. Create the database

Start MySQL, then run `setup.sql` (for example in MySQL Workbench or the `mysql` command line). Then create an application user with rights on the database only:

```sql
CREATE USER IF NOT EXISTS 'student'@'localhost' IDENTIFIED BY 'choose_a_password';
GRANT ALL PRIVILEGES ON student_db.* TO 'student'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Configure the connection

Edit the three constants at the top of `StudentDAO.java`:

```java
private static final String URL = "jdbc:mysql://localhost:3306/student_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
private static final String USER = "student";
private static final String PASSWORD = "choose_a_password";
```

Change `3306` in the URL if your MySQL server listens on a different port.

### 3. Add the JDBC driver

Download Connector/J and place the jar (for example `mysql-connector-j-8.4.0.jar`) in the project folder.

### 4. Compile and run

The code compiles without the driver, but the driver must be on the classpath at run time.

**Windows:**
```
javac *.java
java -cp ".;mysql-connector-j-8.4.0.jar" Main
```

**Linux / macOS:**
```
javac *.java
java -cp ".:mysql-connector-j-8.4.0.jar" Main
```

The status bar shows **Connected to MySQL.** when the database is reachable, or **No database - running in memory only.** otherwise.

## Validation Rules

- **Name:** required; starts with a letter; letters, spaces, `'`, `-` and `.` only; maximum 40 characters
- **Admission number:** required; 5-15 characters of letters, digits, `/` or `-`; converted to upper case; must be unique
- **Course:** a real course must be selected
- **Gender:** one option must be selected

## Testing

Manual test cases cover each event type (blocked keys, status bar updates, hover colours, exit confirmation, double-click clear) and the database (persistence across restarts, duplicate rejection, fallback when MySQL is stopped). See the project documentation for the full test table.

## Limitations

- Database credentials are stored in source code (suitable for coursework only)
- Database calls run on the Event Dispatch Thread and could briefly block the UI
- No screen for viewing, editing or deleting stored records

## Possible Improvements

- Move database calls into a `SwingWorker`
- Add a View Records screen using a `JTable`
- Add update and delete operations
- Load existing records at startup
- Read connection settings from a properties file
