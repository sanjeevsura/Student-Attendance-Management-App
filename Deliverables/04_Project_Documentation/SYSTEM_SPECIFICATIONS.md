# TrackEdu - Technical & System Specifications

## 1. Executive Summary
TrackEdu is a modern, offline-first Android attendance management system built exclusively with Kotlin, Jetpack Compose, and Room SQLite. It delivers high-efficiency attendance tracking, real-time analytics, dynamic theme customization, and automated low-attendance warnings without external server dependencies or cloud subscription costs.

---

## 2. Technology Stack & Component Specifications

| Layer | Component / Library | Version / Details | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Kotlin | 2.2.21 | Native Android development |
| **UI Toolkit** | Jetpack Compose & Material 3 | BOM 2025.01.00 | Modern declarative UI, edge-to-edge layout |
| **Persistence** | Room SQLite Persistence | 2.7.0-alpha13 | Local relational storage with compile-time query checks |
| **Architecture** | MVVM + Repository Pattern | AndroidX Lifecycle 2.8.7 | Separation of concerns, unidirectional data flow |
| **Concurrency** | Kotlin Coroutines & Flow | 1.10.1 | Asynchronous processing on Dispatchers.IO |
| **Security** | Salted SHA-256 (`HashUtils`) | Java Cryptography Architecture | Password hashing and session token generation |
| **Testing** | JUnit 4, Robolectric, Roborazzi | JUnit 4.13.2 / Robolectric 4.14.1 | Automated JVM unit tests & screenshot regression |
| **Build System** | Gradle | 9.3.1 (JVM 21) | Build pipeline and multi-channel artifact compilation |

---

## 3. Core Architectural Modules

1. **Teacher-Centric Identity & Lifecycle**:
   - Single-role operational paradigm designed around daily classroom needs.
   - Initial run detection prompts `SetupTeacherScreen` when no teachers are present in SQLite.
2. **Session Security & Timeout**:
   - 5-minute strict, non-extending session lifetime.
   - Evaluated continuously on startup, app resume, and periodic background checks.
3. **Roll-Call & Special Attendance Recording**:
   - Full-roster attendance marking with batch upsert capabilities.
   - Direct On-Duty (OD), Medical, and Sports leave recording directly to the database.
4. **Offline Resilience**:
   - 100% operable without internet connectivity.
   - Local Room SQLite database preserves all historical attendance logs.
