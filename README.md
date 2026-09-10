# TrackEdu — Student Attendance Management App

> **Production-Grade Android Academic Attendance Monitoring Platform**  
> Built with **Kotlin**, **Jetpack Compose**, **Material 3 (Google Stitch Light/Dark)**, **Room (SQLite)**, **MVVM**, **Repository Pattern**, **Coroutines**, and **StateFlow**.

---

## 1. Project Overview

**TrackEdu** is an offline-first Android application designed for teachers and faculty members to manage student attendance, track academic standing, record direct special attendance (OD, Medical, Sports, Exam, Leave, Other), analyze compliance reports, and maintain student rosters.

TrackEdu operates on a **Single Role Architecture: TEACHER**. The teacher is the complete user of the application and has direct authority to manage student records and attendance roll calls without unnecessary administrative bottlenecks or approval queues.

The application eliminates paper roll calls, prevents attendance data corruption, eliminates fake fallback metrics, protects against session tampering through a **Strict 5-Minute Absolute Lifetime Session**, and provides a dedicated **Direct Special Attendance System** with live Room SQLite data flow.

---

## 2. Core Architecture

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

## 3. Teacher Capabilities Matrix

| Module | Teacher Capability | Status |
| :--- | :--- | :---: |
| **Teacher Registration** | Create institutional teacher account on first launch | ✅ Direct |
| **Authentication** | Secure email/username + SHA-256 salted password hash | ✅ Direct |
| **Session Lifetime** | Strict non-extending 300-second session security | ✅ Real-time |
| **Student Enrollment** | Add new students directly into SQLite Room database | ✅ Direct |
| **Student Modification** | Update student profile, contact info, roll numbers | ✅ Direct |
| **Student Deletion** | Delete student with confirmation & cascade cleanup | ✅ Direct |
| **Attendance Roll Call** | Mark Present/Absent for date, subject, and section | ✅ Direct |
| **Special Attendance** | Direct logging of OD, Medical, Sports, Exam, Leave | ✅ Direct |
| **Attendance Math** | Zero-record student displays `"No attendance recorded"` | ✅ Mathematical |
| **Academic Analytics** | Weekly turnout, distribution, and shortage alerts | ✅ Live Flow |
| **Notification Center** | Dynamic unread count & persistent notification states | ✅ Real SQLite |
| **Theme Customization** | Light (Stitch), Dark (Slate), and System Default | ✅ Persistent |
| **Admin Role & Queue** | Removed entirely from application | ❌ Eliminated |

---

## 4. Key Technical Pillars

### 4.1 Real Database Persistence (SQLite via Room)
- **Database**: `trackedu_attendance.db`
- **Entities**: `User` (teachers), `Student`, `AttendanceRecord`, `AppNotification`
- **Zero Mock Business Data**: The database initializes clean with 0 fake records. All counts, percentages, trends, and rosters stream directly from Room DAOs through Kotlin Coroutine `Flow` and `StateFlow`.

### 4.2 Critical UI Layout & Android WindowInsets
- **Zero Status Bar Overlap**: The application top bar, navigation drawer header, and screen content utilize modern Compose safe inset APIs (`WindowInsets.statusBars`, `statusBarsPadding()`, `navigationBarsPadding()`, `Scaffold.contentWindowInsets`).
- **Clean Separation**: Interactive elements (hamburger menu, title, notification bell, avatar) sit safely below Android's system status bar (Time, Wi-Fi, Signal, Battery, Camera Notch).
- **Theme Bar Contrast**: Status bar icon contrast adapts dynamically (`isAppearanceLightStatusBars = !isDarkTheme`).

### 4.3 Absolute 5-Minute Session Security
- **Strict 300s Duration**: Sessions expire exactly 300 seconds from successful authentication.
- **No Activity Extension**: Screen navigation, drawer toggles, and UI interactions do not reset the timer.
- **Triple-Layer Enforcement**: Verified at startup, on activity resume (`MainActivity.onResume()`), and via a continuous 1-second coroutine watcher.
- **Safe Expiration**: Automatically clears credentials, shows the session expired dialog, wipes navigation back-stack, and navigates to Login.

### 4.4 Mathematical Integrity & 0-Record Rule
- Newly registered students with 0 attendance sessions display `"No attendance recorded"` rather than arbitrary fallback percentages.
- Special attendance cases (`OD`, `MEDICAL`, `SPORTS`, `EXAM`, `LEAVE`) count toward attended sessions in statutory attendance calculations:
  $$\text{Percentage} = \frac{\text{Present} + \text{Special Attended}}{\text{Total Sessions}} \times 100\%$$

---

## 5. Directory Structure

```text
app/src/main/java/com/example/
├── MainActivity.kt                         // Lifecycle, WindowInsets, screen transitions
├── data/
│   ├── dao/
│   │   ├── AttendanceDao.kt               // Room DAO for attendance records
│   │   ├── NotificationDao.kt             // Room DAO for notifications
│   │   ├── StudentDao.kt                  // Room DAO for student roster
│   │   └── UserDao.kt                     // Room DAO for teacher accounts
│   ├── database/
│   │   └── AppDatabase.kt                 // Room database & migrations
│   ├── model/
│   │   ├── AppNotification.kt             // Notification entity
│   │   ├── AttendanceRecord.kt            // Attendance entity & unique index
│   │   ├── Student.kt                     // Student entity & contact info
│   │   ├── User.kt                        // Teacher user entity
│   │   └── UserRole.kt                    // Teacher role definition
│   └── repository/
│       ├── AttendanceRepository.kt        // Database operations & live Flows
│       ├── HashUtils.kt                   // Salted SHA-256 password hashing
│       └── SessionManager.kt              // 5-minute session & theme preferences
├── ui/
│   ├── components/
│   │   ├── TrackEduBottomBar.kt           // Bottom navigation
│   │   ├── TrackEduNavigationDrawer.kt    // Side navigation drawer with safe insets
│   │   └── TrackEduTopBar.kt              // Top app bar with statusBarsPadding
│   ├── screens/
│   │   ├── AddStudentScreen.kt            // Student registration
│   │   ├── AttendanceRecordsScreen.kt     // Session history & filters
│   │   ├── DashboardScreen.kt             // Dynamic teacher dashboard
│   │   ├── EditStudentScreen.kt           // Student profile edit
│   │   ├── LandingScreen.kt               // Welcome portal
│   │   ├── LoginScreen.kt                 // Teacher authentication
│   │   ├── MarkAttendanceScreen.kt        // Daily roll call session
│   │   ├── NotificationsScreen.kt         // Notification center
│   │   ├── ReportsScreen.kt               // Academic compliance analytics
│   │   ├── SettingsScreen.kt              // Profile, password change & theme
│   │   ├── SetupTeacherScreen.kt          // Initial teacher account creation
│   │   ├── SpecialCaseScreen.kt           // Direct special attendance entry
│   │   ├── SplashScreen.kt                // Startup brand animation
│   │   ├── StudentDetailScreen.kt         // Student profile & contact actions
│   │   └── StudentsRosterScreen.kt        // Student directory & search
│   └── theme/
│       ├── Color.kt                       // Stitch Color Palette
│       ├── Theme.kt                       // Light & Dark theme setups
│       └── Type.kt                        // Typography tokens
└── viewmodel/
    └── AttendanceViewModel.kt             // StateFlow management & operations
```

---

## 6. Build & Test Instructions

### Running Automated Tests
```powershell
.\gradlew.bat clean testDebugUnitTest --no-daemon
```

### Assembling Debug APK
```powershell
.\gradlew.bat assembleDebug --no-daemon
```
The resulting APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 7. Quality Assurance Checklist

- [x] **Single-Role Model**: Zero Admin roles, Admin logins, or Admin screens.
- [x] **Real Teacher Account**: Initial startup launches `SetupTeacherScreen` when 0 teachers exist.
- [x] **WindowInsets Safety**: Top bar, drawer, and bottom navigation sit safely within device insets without status bar overlap.
- [x] **Direct Roster Management**: Teachers can add, edit, delete, search, and filter students directly.
- [x] **Direct Special Attendance**: OD, Medical, Sports, Exam, Leave logged directly to SQLite without approval queues.
- [x] **Zero Mock Data**: No fallback numbers (87.5%, 62%, 54%), no fake lists, completely database-driven.
- [x] **5-Minute Non-Extending Session**: Enforced at startup, on resume, and every second.
- [x] **Unit Test Suite**: 100% test pass rate across all core business logic and security policies.
