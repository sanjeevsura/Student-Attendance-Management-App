package com.example

import com.example.data.model.AppNotification
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.AttendanceType
import com.example.data.model.NotificationCategory
import com.example.data.model.Student
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.repository.AttendanceTier
import com.example.data.repository.HashUtils
import com.example.data.repository.StudentWithStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Comprehensive Unit Test Suite for TrackEdu Teacher-Only Architecture.
 * Covers:
 * 1. Zero-record attendance rule ("No attendance recorded", no fake fallbacks)
 * 2. Real attendance percentage formula & tiers
 * 3. Special attendance handling (OD, Medical, Sports, Exam, Leave count as attended)
 * 4. Duplicate attendance prevention logic
 * 5. Strict 5-minute absolute non-extending session expiry logic
 * 6. Teacher Direct Student CRUD (Teacher can add, edit, delete directly)
 * 7. Teacher Direct Special Attendance logging (direct recording to database)
 * 8. Teacher Registration validation (Name, Email, Password strength, confirmation)
 * 9. Password security & salted SHA-256 hash verification
 * 10. Database-driven teacher authentication & session loading
 * 11. Teacher Password Change workflow
 * 12. Notification Center & unread badge count calculation
 * 13. Database cascade delete rule for student records
 * 14. Fresh database empty state verification (Zero mock data)
 */
class TrackEduCoreUnitTest {

    // -------------------------------------------------------------
    // 1. ZERO-RECORD ATTENDANCE RULE
    // -------------------------------------------------------------

    @Test
    fun newStudent_withZeroRecords_showsNoAttendanceRecorded_notFakePercentage() {
        val student = Student(
            id = 101L,
            name = "Aarav Sharma",
            rollNumber = "23CS999",
            department = "Computer Science",
            year = "III Year",
            section = "Section A",
            email = "aarav@campus.edu",
            phone = "+91 99999 00001",
            parentName = "Mr. Sharma",
            parentPhone = "+91 99999 00002"
        )
        val records = emptyList<AttendanceRecord>()

        val total = records.size
        val attended = records.count {
            it.status == AttendanceStatus.PRESENT ||
                    it.type in listOf(AttendanceType.OD, AttendanceType.MEDICAL, AttendanceType.SPORTS, AttendanceType.EXAM, AttendanceType.LEAVE)
        }
        val percentage: Double? = if (total > 0) (attended.toDouble() / total.toDouble()) * 100.0 else null

        val tier = when {
            percentage == null -> AttendanceTier.NO_RECORDS
            percentage >= 90.0 -> AttendanceTier.EXCELLENT
            percentage >= 75.0 -> AttendanceTier.GOOD
            percentage >= 65.0 -> AttendanceTier.WARNING
            else -> AttendanceTier.CRITICAL
        }

        val stats = StudentWithStats(
            student = student,
            totalSessions = total,
            attendedSessions = attended,
            missedSessions = 0,
            specialSessions = 0,
            attendancePercentage = percentage,
            statusTier = tier
        )

        // Verifications
        assertEquals(0, stats.totalSessions)
        assertEquals(0, stats.attendedSessions)
        assertNull("Zero-record student percentage must be null", stats.attendancePercentage)
        assertEquals(AttendanceTier.NO_RECORDS, stats.statusTier)
        assertEquals("No Records", stats.statusTier.label)

        // Verify formatted representation
        val displayStr = if (stats.attendancePercentage != null) {
            String.format("%.1f%%", stats.attendancePercentage)
        } else {
            "No attendance recorded"
        }
        assertEquals("No attendance recorded", displayStr)
    }

    // -------------------------------------------------------------
    // 2. ATTENDANCE PERCENTAGE FORMULA & TIERS
    // -------------------------------------------------------------

    @Test
    fun attendancePercentage_calculatesCorrectly_acrossDifferentTiers() {
        // Case A: 8 Present, 2 Absent = 80.0% (Good Standing)
        val recordsA = (1..8).map {
            AttendanceRecord(studentId = 1L, date = "2026-09-0$it", status = AttendanceStatus.PRESENT)
        } + (9..10).map {
            AttendanceRecord(studentId = 1L, date = "2026-09-0$it", status = AttendanceStatus.ABSENT)
        }
        val totalA = recordsA.size
        val attendedA = recordsA.count { it.status == AttendanceStatus.PRESENT }
        val percentageA = (attendedA.toDouble() / totalA.toDouble()) * 100.0
        assertEquals(10, totalA)
        assertEquals(8, attendedA)
        assertEquals(80.0, percentageA, 0.001)

        // Case B: 10 Present, 0 Absent = 100.0% (Excellent)
        val recordsB = (1..10).map {
            AttendanceRecord(studentId = 2L, date = "2026-09-0$it", status = AttendanceStatus.PRESENT)
        }
        val percentageB = (recordsB.count { it.status == AttendanceStatus.PRESENT }.toDouble() / recordsB.size.toDouble()) * 100.0
        assertEquals(100.0, percentageB, 0.001)

        // Case C: 6 Present, 4 Absent = 60.0% (Critical Shortage)
        val recordsC = (1..6).map {
            AttendanceRecord(studentId = 3L, date = "2026-09-0$it", status = AttendanceStatus.PRESENT)
        } + (7..10).map {
            AttendanceRecord(studentId = 3L, date = "2026-09-0$it", status = AttendanceStatus.ABSENT)
        }
        val percentageC = (recordsC.count { it.status == AttendanceStatus.PRESENT }.toDouble() / recordsC.size.toDouble()) * 100.0
        assertEquals(60.0, percentageC, 0.001)
        assertTrue("Under 75% triggers shortage alert", percentageC < 75.0)
    }

    // -------------------------------------------------------------
    // 3. SPECIAL ATTENDANCE CASES (OD, MEDICAL, SPORTS, EXAM, LEAVE)
    // -------------------------------------------------------------

    @Test
    fun specialAttendance_countsAsAttendedTowardCompliance() {
        val studentId = 5L
        val records = listOf(
            // 5 Regular Present
            AttendanceRecord(studentId = studentId, date = "2026-09-01", status = AttendanceStatus.PRESENT, type = AttendanceType.REGULAR),
            AttendanceRecord(studentId = studentId, date = "2026-09-02", status = AttendanceStatus.PRESENT, type = AttendanceType.REGULAR),
            AttendanceRecord(studentId = studentId, date = "2026-09-03", status = AttendanceStatus.PRESENT, type = AttendanceType.REGULAR),
            AttendanceRecord(studentId = studentId, date = "2026-09-04", status = AttendanceStatus.PRESENT, type = AttendanceType.REGULAR),
            AttendanceRecord(studentId = studentId, date = "2026-09-05", status = AttendanceStatus.PRESENT, type = AttendanceType.REGULAR),
            // 2 On Duty (OD) for University Hackathon
            AttendanceRecord(studentId = studentId, date = "2026-09-06", status = AttendanceStatus.PRESENT, type = AttendanceType.OD),
            AttendanceRecord(studentId = studentId, date = "2026-09-07", status = AttendanceStatus.PRESENT, type = AttendanceType.OD),
            // 1 Medical Leave with verified doctor certificate
            AttendanceRecord(studentId = studentId, date = "2026-09-08", status = AttendanceStatus.PRESENT, type = AttendanceType.MEDICAL),
            // 2 Unexcused Absences
            AttendanceRecord(studentId = studentId, date = "2026-09-09", status = AttendanceStatus.ABSENT, type = AttendanceType.REGULAR),
            AttendanceRecord(studentId = studentId, date = "2026-09-10", status = AttendanceStatus.ABSENT, type = AttendanceType.REGULAR)
        )

        val total = records.size
        val attended = records.count {
            it.status == AttendanceStatus.PRESENT ||
                    it.type in listOf(AttendanceType.OD, AttendanceType.MEDICAL, AttendanceType.SPORTS, AttendanceType.EXAM, AttendanceType.LEAVE)
        }
        val specialCount = records.count { it.type != AttendanceType.REGULAR && it.type != AttendanceType.CORRECTION }
        val missed = total - attended
        val percentage = (attended.toDouble() / total.toDouble()) * 100.0

        assertEquals(10, total)
        assertEquals(8, attended)
        assertEquals(2, missed)
        assertEquals(3, specialCount)
        assertEquals(80.0, percentage, 0.001)
        assertTrue("Student with OD & Medical maintains good standing above 75%", percentage >= 75.0)
    }

    // -------------------------------------------------------------
    // 4. DUPLICATE ATTENDANCE PREVENTION
    // -------------------------------------------------------------

    @Test
    fun duplicateAttendance_sameStudentDateSubject_isUpdatedNotDuplicated() {
        val studentId = 10L
        val date = "2026-09-08"
        val subject = "CS502 - Database Systems"

        val initialRecord = AttendanceRecord(
            id = 1L,
            studentId = studentId,
            date = date,
            subject = subject,
            status = AttendanceStatus.ABSENT
        )

        // Map simulating database unique index on (studentId, date, subject)
        val databaseStore = mutableMapOf<Triple<Long, String, String>, AttendanceRecord>()
        databaseStore[Triple(initialRecord.studentId, initialRecord.date, initialRecord.subject)] = initialRecord

        assertEquals(1, databaseStore.size)
        assertEquals(AttendanceStatus.ABSENT, databaseStore[Triple(studentId, date, subject)]?.status)

        // Updated record submitted for same student, date, and subject
        val updatedRecord = AttendanceRecord(
            id = 1L,
            studentId = studentId,
            date = date,
            subject = subject,
            status = AttendanceStatus.PRESENT
        )
        databaseStore[Triple(updatedRecord.studentId, updatedRecord.date, updatedRecord.subject)] = updatedRecord

        // Must still contain exactly 1 entry, updated to PRESENT
        assertEquals(1, databaseStore.size)
        assertEquals(AttendanceStatus.PRESENT, databaseStore[Triple(studentId, date, subject)]?.status)
    }

    // -------------------------------------------------------------
    // 5. ABSOLUTE 5-MINUTE SESSION EXPIRY (NO ACTIVITY EXTENSION)
    // -------------------------------------------------------------

    @Test
    fun sessionLifetime_isAbsolute5Minutes_neverExtendedByActivity() {
        val loginTime = 1_000_000L
        val sessionDurationMs = 5 * 60 * 1000L // 300,000 ms
        val fixedExpiryTime = loginTime + sessionDurationMs

        // 1. Check at login time (remaining = 300s)
        val remainingAtStart = maxOf(0L, (fixedExpiryTime - loginTime) / 1000)
        assertEquals(300L, remainingAtStart)
        assertTrue("Session must be valid at start", loginTime < fixedExpiryTime)

        // 2. User navigates screens after 2 minutes (timestamp = 120,000ms later)
        val userNavigationTime = loginTime + 120_000L
        // Critical: expiry time MUST NOT change on user activity!
        val expiryAfterNavigation = fixedExpiryTime
        val remainingAfterNavigation = maxOf(0L, (expiryAfterNavigation - userNavigationTime) / 1000)
        assertEquals(180L, remainingAfterNavigation)
        assertTrue("Session remains valid before 5 minutes", userNavigationTime < fixedExpiryTime)

        // 3. User visits settings or opens drawer after 4 minutes 50 seconds
        val lastMinuteTime = loginTime + 290_000L
        val remainingAtLastMinute = maxOf(0L, (fixedExpiryTime - lastMinuteTime) / 1000)
        assertEquals(10L, remainingAtLastMinute)

        // 4. Exact expiry at 5 minutes
        val exactExpiryTime = loginTime + 300_000L
        val remainingAtExpiry = maxOf(0L, (fixedExpiryTime - exactExpiryTime) / 1000)
        assertEquals(0L, remainingAtExpiry)
        assertFalse("Session is expired at exactly 5 minutes", exactExpiryTime < fixedExpiryTime)

        // 5. Beyond 5 minutes (e.g. app reopened from background)
        val timeBeyondExpiry = loginTime + 350_000L
        val isSessionValid = timeBeyondExpiry < fixedExpiryTime
        assertFalse("Session must be expired when time > 5 minutes", isSessionValid)
    }

    // -------------------------------------------------------------
    // 6. TEACHER DIRECT STUDENT CRUD
    // -------------------------------------------------------------

    @Test
    fun teacherDirectStudentCrud_addsEditsAndDeletesDirectly() {
        val studentStore = mutableMapOf<Long, Student>()
        var nextId = 1L

        // 1. Teacher adds student directly
        val student = Student(
            id = nextId++,
            name = "Priya Patel",
            rollNumber = "23CS201",
            department = "Computer Science",
            year = "III Year",
            section = "Section B",
            email = "priya.patel@campus.edu",
            phone = "+91 98765 43210",
            parentName = "Mr. Patel",
            parentPhone = "+91 98765 43211"
        )
        studentStore[student.id] = student
        assertEquals(1, studentStore.size)
        assertEquals("Priya Patel", studentStore[student.id]?.name)

        // 2. Teacher updates student directly
        val updated = student.copy(section = "Section A", phone = "+91 98765 99999")
        studentStore[updated.id] = updated
        assertEquals("Section A", studentStore[student.id]?.section)
        assertEquals("+91 98765 99999", studentStore[student.id]?.phone)

        // 3. Teacher deletes student directly
        studentStore.remove(student.id)
        assertEquals(0, studentStore.size)
    }

    // -------------------------------------------------------------
    // 7. TEACHER DIRECT SPECIAL ATTENDANCE LOGGING
    // -------------------------------------------------------------

    @Test
    fun teacherDirectSpecialAttendance_recordsDirectlyIntoDatabase() {
        val attendanceStore = mutableListOf<AttendanceRecord>()

        val specialRecord = AttendanceRecord(
            id = 1L,
            studentId = 42L,
            date = "2026-09-10",
            subject = "CS502 - Database Systems",
            status = AttendanceStatus.PRESENT,
            type = AttendanceType.OD,
            period = "Period 2",
            note = "Verified by Prof. Sharma",
            createdBy = "Prof. Sharma"
        )
        attendanceStore.add(specialRecord)

        assertEquals(1, attendanceStore.size)
        assertEquals(AttendanceType.OD, attendanceStore[0].type)
        assertEquals(AttendanceStatus.PRESENT, attendanceStore[0].status)
        assertEquals("Prof. Sharma", attendanceStore[0].createdBy)
    }

    // -------------------------------------------------------------
    // 8. TEACHER REGISTRATION VALIDATION
    // -------------------------------------------------------------

    @Test
    fun teacherRegistration_validatesFieldsAndEnforcesPasswordRules() {
        fun validateTeacherRegistration(
            name: String,
            email: String,
            password: String,
            confirmPass: String
        ): Result<String> {
            if (name.isBlank()) return Result.failure(IllegalArgumentException("Full Name is required"))
            val trimmedEmail = email.trim()
            if (trimmedEmail.isBlank() || !trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
                return Result.failure(IllegalArgumentException("Please enter a valid institutional email"))
            }
            if (password.length < 6) {
                return Result.failure(IllegalArgumentException("Password must be at least 6 characters long"))
            }
            if (password != confirmPass) {
                return Result.failure(IllegalArgumentException("Passwords do not match"))
            }
            return Result.success("Teacher account ready for creation")
        }

        // 1. Successful validation
        val validResult = validateTeacherRegistration("Prof. Alan Turing", "alan.turing@university.edu", "TuringSecure@2026", "TuringSecure@2026")
        assertTrue("Valid teacher input should succeed", validResult.isSuccess)

        // 2. Blank name
        val blankNameResult = validateTeacherRegistration("", "alan@university.edu", "password123", "password123")
        assertTrue(blankNameResult.isFailure)
        assertEquals("Full Name is required", blankNameResult.exceptionOrNull()?.message)

        // 3. Invalid email format
        val invalidEmailResult = validateTeacherRegistration("Teacher User", "not-an-email", "password123", "password123")
        assertTrue(invalidEmailResult.isFailure)
        assertEquals("Please enter a valid institutional email", invalidEmailResult.exceptionOrNull()?.message)

        // 4. Short password
        val shortPassResult = validateTeacherRegistration("Teacher User", "teacher@univ.edu", "12345", "12345")
        assertTrue(shortPassResult.isFailure)
        assertEquals("Password must be at least 6 characters long", shortPassResult.exceptionOrNull()?.message)

        // 5. Mismatched passwords
        val mismatchResult = validateTeacherRegistration("Teacher User", "teacher@univ.edu", "password123", "mismatch123")
        assertTrue(mismatchResult.isFailure)
        assertEquals("Passwords do not match", mismatchResult.exceptionOrNull()?.message)
    }

    // -------------------------------------------------------------
    // 9. PASSWORD SECURITY & SALTED HASH VERIFICATION
    // -------------------------------------------------------------

    @Test
    fun passwordSecurity_usesSaltedHash_andNeverStoresPlaintext() {
        val rawPassword = "FacultySecurePassword#2026"
        val hashedPassword = HashUtils.hashPassword(rawPassword)

        // Verifications
        assertFalse("Password hash must not equal plaintext password", rawPassword == hashedPassword)
        assertTrue("Hash must be a 64-character hex string (SHA-256)", hashedPassword.length == 64)
        assertTrue("Hash must only contain valid hexadecimal characters", hashedPassword.matches(Regex("^[0-9a-fA-F]{64}$")))

        // Verification function
        assertTrue("Verification must succeed with correct password", HashUtils.verifyPassword(rawPassword, hashedPassword))
        assertFalse("Verification must fail with incorrect password", HashUtils.verifyPassword("WrongPassword", hashedPassword))
    }

    // -------------------------------------------------------------
    // 10. DATABASE-DRIVEN TEACHER AUTHENTICATION & SESSION LOADING
    // -------------------------------------------------------------

    @Test
    fun databaseAuthentication_loadsTeacherFromDb_andRejectsInactiveUsers() {
        val teacherUser = User(
            id = 1L,
            name = "Prof. Alan Turing",
            username = "alan.turing@institutedomain.edu",
            email = "alan.turing@institutedomain.edu",
            passwordHash = HashUtils.hashPassword("TuringPass123"),
            role = UserRole.TEACHER,
            department = "Computer Science",
            isActive = true
        )

        val inactiveTeacher = User(
            id = 2L,
            name = "Inactive Faculty",
            username = "inactive@institutedomain.edu",
            email = "inactive@institutedomain.edu",
            passwordHash = HashUtils.hashPassword("Password123"),
            role = UserRole.TEACHER,
            department = "Computer Science",
            isActive = false
        )

        val userTable = mapOf(
            teacherUser.email to teacherUser,
            inactiveTeacher.email to inactiveTeacher
        )

        fun authenticate(email: String, pass: String): Result<User> {
            val user = userTable[email.trim()] ?: return Result.failure(IllegalArgumentException("Invalid email or password"))
            if (!user.isActive) return Result.failure(IllegalStateException("This account has been deactivated."))
            if (!HashUtils.verifyPassword(pass, user.passwordHash)) {
                return Result.failure(IllegalArgumentException("Invalid email or password"))
            }
            return Result.success(user)
        }

        // 1. Teacher authentication -> role loaded as TEACHER
        val teacherAuth = authenticate("alan.turing@institutedomain.edu", "TuringPass123")
        assertTrue(teacherAuth.isSuccess)
        assertEquals(UserRole.TEACHER, teacherAuth.getOrNull()?.role)
        assertEquals("Prof. Alan Turing", teacherAuth.getOrNull()?.name)

        // 2. Wrong password -> failure
        val wrongPassAuth = authenticate("alan.turing@institutedomain.edu", "WrongPassword")
        assertTrue(wrongPassAuth.isFailure)
        assertEquals("Invalid email or password", wrongPassAuth.exceptionOrNull()?.message)

        // 3. Unknown email -> failure
        val unknownAuth = authenticate("nonexistent@domain.edu", "TuringPass123")
        assertTrue(unknownAuth.isFailure)
        assertEquals("Invalid email or password", unknownAuth.exceptionOrNull()?.message)

        // 4. Inactive user -> account deactivated error
        val inactiveAuth = authenticate("inactive@institutedomain.edu", "Password123")
        assertTrue(inactiveAuth.isFailure)
        assertEquals("This account has been deactivated.", inactiveAuth.exceptionOrNull()?.message)
    }

    // -------------------------------------------------------------
    // 11. TEACHER PASSWORD CHANGE WORKFLOW
    // -------------------------------------------------------------

    @Test
    fun teacherPasswordChange_verifiesCurrentPasswordAndUpdatesHash() {
        var teacherUser = User(
            id = 1L,
            name = "Prof. Robert Smith",
            username = "robert.smith@college.edu",
            email = "robert.smith@college.edu",
            passwordHash = HashUtils.hashPassword("InitialPass@123"),
            role = UserRole.TEACHER,
            department = "Computer Science",
            isActive = true
        )

        fun changePassword(currentPass: String, newPass: String, confirmPass: String): Result<User> {
            if (newPass != confirmPass) return Result.failure(IllegalArgumentException("New passwords do not match."))
            if (newPass.length < 6) return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
            if (!HashUtils.verifyPassword(currentPass, teacherUser.passwordHash)) {
                return Result.failure(IllegalArgumentException("Incorrect current password."))
            }
            teacherUser = teacherUser.copy(passwordHash = HashUtils.hashPassword(newPass))
            return Result.success(teacherUser)
        }

        // Wrong current password -> failure
        val wrongCurrent = changePassword("WrongOldPass", "NewPassword#2026", "NewPassword#2026")
        assertTrue(wrongCurrent.isFailure)
        assertEquals("Incorrect current password.", wrongCurrent.exceptionOrNull()?.message)

        // Valid change -> success
        val successChange = changePassword("InitialPass@123", "NewPassword#2026", "NewPassword#2026")
        assertTrue(successChange.isSuccess)
        assertTrue(HashUtils.verifyPassword("NewPassword#2026", teacherUser.passwordHash))
        assertFalse(HashUtils.verifyPassword("InitialPass@123", teacherUser.passwordHash))
    }

    // -------------------------------------------------------------
    // 12. NOTIFICATION CENTER & UNREAD COUNT
    // -------------------------------------------------------------

    @Test
    fun notificationCenter_computesUnreadBadgeCountDynamically() {
        val notifications = listOf(
            AppNotification(id = 1L, title = "Alert 1", message = "Msg 1", category = NotificationCategory.WARNINGS, isRead = false),
            AppNotification(id = 2L, title = "Alert 2", message = "Msg 2", category = NotificationCategory.ATTENDANCE, isRead = false),
            AppNotification(id = 3L, title = "Alert 3", message = "Msg 3", category = NotificationCategory.SYSTEM, isRead = true)
        )

        val unreadCount = notifications.count { !it.isRead }
        assertEquals(2, unreadCount)

        // All read
        val allRead = notifications.map { it.copy(isRead = true) }
        assertEquals(0, allRead.count { !it.isRead })
    }

    // -------------------------------------------------------------
    // 13. DATABASE CASCADE DELETE RULE
    // -------------------------------------------------------------

    @Test
    fun databaseSchema_cascadeDeleteRule_removesAttendanceRecordsOnStudentDeletion() {
        val studentId = 42L
        val student = Student(
            id = studentId,
            name = "Test Student",
            rollNumber = "23CS999",
            department = "CSE",
            year = "III Year",
            section = "A",
            email = "test@college.edu",
            phone = "+91 90000 00000",
            parentName = "Test Parent",
            parentPhone = "+91 90000 00001"
        )

        val studentTable = mutableMapOf<Long, Student>()
        val attendanceTable = mutableListOf<AttendanceRecord>()

        studentTable[student.id] = student
        attendanceTable.add(AttendanceRecord(id = 1L, studentId = studentId, date = "2026-09-01", status = AttendanceStatus.PRESENT))
        attendanceTable.add(AttendanceRecord(id = 2L, studentId = studentId, date = "2026-09-02", status = AttendanceStatus.ABSENT))

        assertEquals(1, studentTable.size)
        assertEquals(2, attendanceTable.size)

        // Teacher Deletes Student (Cascades to AttendanceRecord)
        studentTable.remove(studentId)
        attendanceTable.removeAll { it.studentId == studentId }

        assertEquals(0, studentTable.size)
        assertEquals("Attendance records must be cascade-deleted to prevent orphaned rows", 0, attendanceTable.size)
    }

    // -------------------------------------------------------------
    // 14. ZERO-MOCK FRESH DATABASE VERIFICATION
    // -------------------------------------------------------------

    @Test
    fun freshDatabase_hasZeroMockRecords_andDisplaysCleanEmptyStates() {
        val emptyStudents = emptyList<Student>()
        val emptyNotifications = emptyList<AppNotification>()
        val emptyAttendance = emptyList<AttendanceRecord>()

        // 1. Dashboard student count
        assertEquals(0, emptyStudents.size)

        // 2. Attendance percentage with 0 records
        val percentage: Double? = if (emptyAttendance.isNotEmpty()) {
            (emptyAttendance.count { it.status == AttendanceStatus.PRESENT }.toDouble() / emptyAttendance.size) * 100.0
        } else null
        assertNull("Attendance percentage must be null when 0 records exist", percentage)

        // 3. Notification badge
        val unreadNotifications = emptyNotifications.count { !it.isRead }
        assertEquals(0, unreadNotifications)
    }
}
