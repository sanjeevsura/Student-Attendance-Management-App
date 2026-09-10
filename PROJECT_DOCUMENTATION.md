# TrackEdu — Complete Project Documentation & Technical Specification

## Project Overview
**TrackEdu** is a production-grade Android academic attendance monitoring system designed exclusively for teachers and faculty. The application provides an offline-first, highly responsive, database-driven experience using Android Jetpack Compose, Material 3, SQLite Room, Kotlin Coroutines, and StateFlow.

---

## 1. System Architecture & Role Model
TrackEdu operates on a **Single Role (Teacher-Only)** architecture. There is no Admin role, no Admin dashboard, and no administrative approval bottleneck.

### High-Level Architecture
```text
                    TRACKEDU
                       │
                    SPLASH
                       │
                 TEACHER LOGIN
                       │
                       ▼
              TEACHER DASHBOARD
                       │
       ┌───────────────┼────────────────┐
       │               │                │
       ▼               ▼                ▼
   STUDENTS        ATTENDANCE       SPECIAL
  MANAGEMENT       MANAGEMENT      ATTENDANCE
       │               │                │
   Add Student      Present/Absent   OD
   Edit Student     History          Medical
   Delete Student   Percentage       Sports
   View Student     Reports          Exam
                                    Leave
                                    Other
       │               │                │
       └───────────────┼────────────────┘
                       ▼
                NOTIFICATIONS
                       │
                       ▼
                REPORTS / SETTINGS
```

---

## 2. Database Implementation (SQLite via Room)

- **Database Name**: `trackedu_attendance.db`
- **ORM**: Android Room 2.6.1 + KSP
- **Persistence Pattern**: MVVM + Repository Pattern + SQLite Room + Kotlin Coroutine Flow

### Database Schema

#### 1. `users` Table (Teacher Accounts)
| Field | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique teacher identifier |
| `name` | TEXT | NOT NULL | Full name of teacher |
| `username` | TEXT | NOT NULL UNIQUE | Institutional username |
| `email` | TEXT | NOT NULL UNIQUE | Institutional email |
| `passwordHash` | TEXT | NOT NULL | Salted SHA-256 password hash |
| `role` | TEXT | NOT NULL DEFAULT 'TEACHER' | User role (always TEACHER) |
| `department` | TEXT | NOT NULL | Academic department |
| `isActive` | INTEGER | NOT NULL DEFAULT 1 | Account active status (1 = Active) |
| `createdAt` | INTEGER | NOT NULL | Epoch timestamp |

#### 2. `students` Table (Student Roster)
| Field | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique student identifier |
| `name` | TEXT | NOT NULL | Student full name |
| `rollNumber` | TEXT | NOT NULL UNIQUE | University registration number |
| `department` | TEXT | NOT NULL | Academic branch |
| `year` | TEXT | NOT NULL DEFAULT 'III Year' | Academic year |
| `section` | TEXT | NOT NULL DEFAULT 'Section A' | Class section |
| `email` | TEXT | NOT NULL DEFAULT '' | Student email |
| `phone` | TEXT | NOT NULL DEFAULT '' | Student phone |
| `parentName` | TEXT | NOT NULL DEFAULT '' | Parent/Guardian name |
| `parentPhone` | TEXT | NOT NULL DEFAULT '' | Parent contact number |
| `createdAt` | INTEGER | NOT NULL | Enrollment timestamp |
| `updatedAt` | INTEGER | NOT NULL | Last update timestamp |

#### 3. `attendance_records` Table (Roll Call & Special Attendance)
| Field | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique record identifier |
| `studentId` | INTEGER | NOT NULL, FOREIGN KEY -> students(id) | Enrolled student reference |
| `date` | TEXT | NOT NULL | Session date (`YYYY-MM-DD`) |
| `subject` | TEXT | NOT NULL | Subject / Course name |
| `status` | TEXT | NOT NULL | Status (`PRESENT`, `ABSENT`) |
| `type` | TEXT | NOT NULL DEFAULT 'REGULAR' | Type (`REGULAR`, `OD`, `MEDICAL`, `SPORTS`, `EXAM`, `LEAVE`, `OTHER`) |
| `remarks` | TEXT | NOT NULL DEFAULT '' | Justification or duty remarks |
| `markedBy` | TEXT | NOT NULL DEFAULT 'Teacher' | Teacher identifier |
| `timestamp` | INTEGER | NOT NULL | Session recording timestamp |

#### 4. `app_notifications` Table (Notification Center)
| Field | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique notification identifier |
| `title` | TEXT | NOT NULL | Notification title |
| `message` | TEXT | NOT NULL | Event details |
| `category` | TEXT | NOT NULL | Category (`ATTENDANCE`, `STUDENT`, `SYSTEM`) |
| `isRead` | INTEGER | NOT NULL DEFAULT 0 | Read flag (0 = Unread, 1 = Read) |
| `targetScreen`| TEXT | NULLABLE | Deep link destination screen |
| `timestamp` | INTEGER | NOT NULL | Event timestamp |

---

## 3. Mathematical Calculations & Business Rules

1. **Zero-Record Rule**:
   - If total sessions held = 0: Attendance percentage is `null` &rarr; Displayed as `"No attendance recorded"`.
   - Never displays fake placeholder percentages (e.g. 87.5%).
2. **Statutory Attendance Formula**:
   $$\text{Attendance Percentage} = \frac{\text{Sessions Attended}}{\text{Total Sessions Held}} \times 100$$
   - `Sessions Attended` = `Count(status == PRESENT)` + `Count(type in [OD, MEDICAL, SPORTS, EXAM, LEAVE])`
   - `Total Sessions Held` = Total recorded attendance sessions for that student.
3. **Academic Standing Tiers**:
   - **Excellent**: $\ge 90.0\%$
   - **Good Standing**: $75.0\% - 89.9\%$
   - **Warning**: $65.0\% - 74.9\%$
   - **Critical Shortage**: $< 65.0\%$
   - **No Records**: 0 sessions held

---

## 4. Strict 5-Minute Session Security

- **Lifetime**: Exactly 300 seconds ($T + 300\text{s}$).
- **Non-Extending**: UI touch events and navigation do not prolong the session.
- **Enforcement**:
  - Validated on application start.
  - Checked on `onResume()` in `MainActivity`.
  - Monitored continuously via a 1-second coroutine watcher.
- **Action on Expiry**: Wipes user credentials, presents the session timeout dialog, clears protected back-stack, and routes to `LoginScreen`.

---

## 5. Android WindowInsets Layout Architecture

- **System Status Bar Separation**: Top app bar, navigation drawer header, and screen content utilize `statusBarsPadding()`, positioning all interactive elements (hamburger icon, title, notification bell, avatar badge) safely below Android system status bar indicators (Time, Wi-Fi, Signal, Battery, Camera Cutout).
- **Theme Bar Contrast**: `WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !isDarkTheme` ensures dark status bar icons on Light theme and light status bar icons on Dark theme.
- **Navigation Insets**: Bottom bars utilize `navigationBarsPadding()` to avoid clipping into Android gesture navigation bars.

---

## 6. How to Build and Run

### Prerequisites
- JDK 17+
- Android SDK (API Level 34 / Android 14)
- Gradle 9.3.1 (included via `./gradlew`)

### Build Commands
```powershell
# Run all unit tests
.\gradlew.bat testDebugUnitTest --no-daemon

# Build Debug APK
.\gradlew.bat assembleDebug --no-daemon

# Clean and Build All
.\gradlew.bat clean testDebugUnitTest assembleDebug --no-daemon
```
