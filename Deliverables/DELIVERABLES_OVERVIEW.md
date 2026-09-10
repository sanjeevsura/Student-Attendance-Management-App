# TrackEdu - Project Submission Deliverables Package

This directory contains the organized submission deliverables for the **TrackEdu (Student Attendance Management System)** project. Each deliverable is structured into its own dedicated folder for easy review and grading.

---

## 📂 Deliverables Directory Map

```
Deliverables/
├── DELIVERABLES_OVERVIEW.md                  <-- You are here (Submission Index)
│
├── 01_Complete_Source_Code/                  <-- Deliverable 1: Complete Project Source Code
│   ├── app/
│   │   ├── src/ (Kotlin, Jetpack Compose, Room SQLite, Res, Unit Tests)
│   │   ├── build.gradle.kts
│   │   └── proguard-rules.pro
│   ├── gradle/
│   │   └── wrapper/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── gradle.properties
│   ├── gradlew & gradlew.bat
│   ├── build_apks.bat
│   └── keystore.properties.example
│
├── 02_Database_Implementation/              <-- Deliverable 2: Database Implementation
│   ├── DATABASE_IMPLEMENTATION.md           (Schema, DAOs, Relational Model, & Architecture)
│   ├── database/
│   │   └── AppDatabase.kt
│   ├── dao/
│   │   ├── AttendanceDao.kt
│   │   ├── NotificationDao.kt
│   │   ├── RequestDao.kt
│   │   ├── StudentDao.kt
│   │   └── UserDao.kt
│   ├── entities_models/
│   │   ├── AttendanceRecord.kt
│   │   ├── Student.kt
│   │   ├── User.kt
│   │   ├── UserRole.kt
│   │   ├── AppRequest.kt
│   │   └── AppNotification.kt
│   └── repository/
│       ├── AttendanceRepository.kt
│       ├── SessionManager.kt
│       └── HashUtils.kt
│
├── 03_UI_Screenshots/                       <-- Deliverable 3: UI Screenshots
│   ├── SCREENSHOTS_CATALOG.md               (Catalog & Descriptions)
│   ├── 01_Login_Screen.png                  (Authentication & Security)
│   ├── 02_Teacher_Dashboard.png             (Home Overview & Metric Cards)
│   ├── 03_Dashboard_Analytics.png           (Real-Time Analytics & Risk Alerts)
│   ├── 04_Navigation_Drawer.png             (Modal Navigation System)
│   ├── 05_Appearance_Theme_Switcher.png     (Light/Dark Theme Selection)
│   ├── 06_Dark_Mode_Applied.png             (High-Contrast Dark Theme)
│   └── 07_Special_Attendance_Recording.png  (Direct On-Duty/Medical Form)
│
├── 04_Project_Documentation/                <-- Deliverable 4: Project Documentation
│   ├── PROJECT_DOCUMENTATION.md             (Complete System Documentation)
│   └── SYSTEM_SPECIFICATIONS.md             (Tech Stack, Architecture, & Security Policies)
│
├── 05_APK_File/                             <-- Deliverable 5: APK Files
│   ├── APK_INSTALLATION_AND_DETAILS.md      (Installation Guide, Checksums, & Signatures)
│   ├── TrackEdu-v1.0-release.apk            (Signed Production Release APK - 15.19 MB)
│   ├── app-release.apk                      (Signed Release Mirror)
│   ├── TrackEdu-v1.0-debug.apk              (Debug Build APK - 22.12 MB)
│   └── app-debug.apk                        (Debug Build Mirror)
│
└── 06_README_File/                          <-- Deliverable 6: README Files
    ├── README.md                            (Comprehensive Project Guide & Build Manual)
    └── QUICK_START.md                       (Fast Evaluation Checklist)
```

---

## 🎯 Verification & Integrity Check

* **Release Signature**: Signed with RSA 2048-bit key (`CN=TrackEdu`), verified using Android SDK `apksigner` (Scheme v2).
* **Automated Unit Tests**: 100% test pass rate (`.\gradlew.bat testDebugUnitTest`).
* **Zero Mock Policy**: 100% database-driven using Room SQLite.
* **Single-Role Model**: Exclusively tailored for Teachers without admin bottlenecks.
* **Non-Extending Session**: 5-minute strict timeout security enforced throughout.

---

## 📦 Single-File Archive
For portal uploads requiring a single compressed file, a pre-packaged zip archive is available at the project root:
* **`TrackEdu_Submission_Deliverables.zip`**
