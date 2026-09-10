package com.example.data.repository

import com.example.data.dao.AttendanceDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.StudentDao
import com.example.data.dao.UserDao
import com.example.data.model.AppNotification
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.AttendanceType
import com.example.data.model.NotificationCategory
import com.example.data.model.Student
import com.example.data.model.User
import com.example.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

data class StudentWithStats(
    val student: Student,
    val totalSessions: Int,
    val attendedSessions: Int,
    val missedSessions: Int,
    val specialSessions: Int,
    val attendancePercentage: Double?, // null means "No attendance recorded"
    val statusTier: AttendanceTier
)

enum class AttendanceTier(val label: String) {
    EXCELLENT("Excellent"),       // >= 90%
    GOOD("Good Standing"),        // >= 75%
    WARNING("Warning"),           // 65% - 74%
    CRITICAL("Critical Shortage"),// < 65%
    NO_RECORDS("No Records")      // 0 sessions held
}

class AttendanceRepository(
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao,
    private val notificationDao: NotificationDao,
    private val userDao: UserDao
) {
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()
    val allAttendanceRecords: Flow<List<AttendanceRecord>> = attendanceDao.getAllRecords()
    val allNotifications: Flow<List<AppNotification>> = notificationDao.getAllNotifications()
    val unreadNotificationsCount: Flow<Int> = notificationDao.getUnreadCount()

    // Combines Students and Attendance into StudentWithStats with Special Case calculation and 0-record fix
    val studentsWithStats: Flow<List<StudentWithStats>> = combine(
        allStudents,
        allAttendanceRecords
    ) { students, records ->
        val recordsByStudent = records.groupBy { it.studentId }
        students.map { student ->
            val studentRecords = recordsByStudent[student.id] ?: emptyList()
            val total = studentRecords.size
            
            // Special Cases (OD, MEDICAL, SPORTS, EXAM, LEAVE) count as Attended directly
            val attended = studentRecords.count { 
                it.status == AttendanceStatus.PRESENT || 
                (it.type in listOf(AttendanceType.OD, AttendanceType.MEDICAL, AttendanceType.SPORTS, AttendanceType.EXAM, AttendanceType.LEAVE))
            }
            val specialCount = studentRecords.count { it.type != AttendanceType.REGULAR && it.type != AttendanceType.CORRECTION }
            val missed = total - attended

            // If total == 0, percentage is null ("No attendance recorded") NOT 100%
            val percentage = if (total > 0) (attended.toDouble() / total.toDouble()) * 100.0 else null

            val tier = when {
                percentage == null -> AttendanceTier.NO_RECORDS
                percentage >= 90.0 -> AttendanceTier.EXCELLENT
                percentage >= 75.0 -> AttendanceTier.GOOD
                percentage >= 65.0 -> AttendanceTier.WARNING
                else -> AttendanceTier.CRITICAL
            }

            StudentWithStats(
                student = student,
                totalSessions = total,
                attendedSessions = attended,
                missedSessions = missed,
                specialSessions = specialCount,
                attendancePercentage = percentage,
                statusTier = tier
            )
        }
    }

    suspend fun getStudentWithStats(studentId: Long): StudentWithStats? = withContext(Dispatchers.IO) {
        val student = studentDao.getStudentByIdDirect(studentId) ?: return@withContext null
        val records = attendanceDao.getRecordsForStudentDirect(studentId)
        val total = records.size
        val attended = records.count { 
            it.status == AttendanceStatus.PRESENT || 
            (it.type in listOf(AttendanceType.OD, AttendanceType.MEDICAL, AttendanceType.SPORTS, AttendanceType.EXAM, AttendanceType.LEAVE))
        }
        val specialCount = records.count { it.type != AttendanceType.REGULAR && it.type != AttendanceType.CORRECTION }
        val missed = total - attended
        val percentage = if (total > 0) (attended.toDouble() / total.toDouble()) * 100.0 else null

        val tier = when {
            percentage == null -> AttendanceTier.NO_RECORDS
            percentage >= 90.0 -> AttendanceTier.EXCELLENT
            percentage >= 75.0 -> AttendanceTier.GOOD
            percentage >= 65.0 -> AttendanceTier.WARNING
            else -> AttendanceTier.CRITICAL
        }

        StudentWithStats(
            student = student,
            totalSessions = total,
            attendedSessions = attended,
            missedSessions = missed,
            specialSessions = specialCount,
            attendancePercentage = percentage,
            statusTier = tier
        )
    }

    // Direct Student creation by Teacher
    suspend fun addStudent(student: Student): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            val existing = studentDao.getStudentByRoll(student.rollNumber.trim())
            if (existing != null) {
                error("Roll number '${student.rollNumber}' is already registered.")
            }
            val id = studentDao.insertStudent(student.copy(rollNumber = student.rollNumber.trim()))
            notificationDao.insertNotification(
                AppNotification(
                    title = "Student Added",
                    message = "${student.name} (${student.rollNumber}) added to student roster.",
                    category = NotificationCategory.SYSTEM,
                    targetScreen = "STUDENTS",
                    targetStudentId = id
                )
            )
            id
        }
    }

    // Direct Student Update by Teacher
    suspend fun updateStudent(student: Student): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            studentDao.updateStudent(student.copy(rollNumber = student.rollNumber.trim()))
        }
    }

    // Direct Student Deletion by Teacher
    suspend fun deleteStudent(studentId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            studentDao.deleteStudentById(studentId)
        }
    }

    // Direct Teacher Special Case Attendance Entry (OD, Medical, Sports, Exam, Leave)
    suspend fun saveSpecialCaseAttendance(
        studentId: Long,
        date: String,
        subject: String,
        type: AttendanceType,
        remarks: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            attendanceDao.insertOrUpdate(
                AttendanceRecord(
                    studentId = studentId,
                    date = date,
                    status = AttendanceStatus.PRESENT,
                    type = type,
                    subject = subject.ifBlank { "Academic Course" },
                    period = "Period 2 (10:15 AM - 11:15 AM)",
                    note = "Special Attendance: ${type.name}",
                    remarks = remarks.ifBlank { "Recorded by Teacher" },
                    createdBy = "Teacher"
                )
            )
            val student = studentDao.getStudentByIdDirect(studentId)
            notificationDao.insertNotification(
                AppNotification(
                    title = "Special Attendance Recorded",
                    message = "${type.name} recorded for ${student?.name ?: "Student"} on $date ($subject).",
                    category = NotificationCategory.ATTENDANCE,
                    targetScreen = "STUDENTS",
                    targetStudentId = studentId
                )
            )
            Unit
        }
    }

    // Normal attendance roll call marking by Teacher
    suspend fun saveAttendanceSession(records: List<AttendanceRecord>): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            attendanceDao.insertOrUpdateAll(records)
            Unit
        }
    }

    // Notifications
    suspend fun markNotificationAsRead(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead()
    }

    // -------------------------------------------------------------
    // TEACHER AUTHENTICATION & REGISTRATION (100% REAL ROOM DATA)
    // -------------------------------------------------------------

    suspend fun getUserCount(): Int = withContext(Dispatchers.IO) {
        userDao.getUserCount()
    }

    suspend fun getActiveTeacherCount(): Int = withContext(Dispatchers.IO) {
        userDao.getActiveTeacherCount()
    }

    suspend fun registerTeacher(
        name: String,
        email: String,
        password: String,
        department: String = "Computer Science & Engineering"
    ): Result<User> = withContext(Dispatchers.IO) {
        runCatching {
            val trimmedName = name.trim()
            val trimmedEmail = email.trim().lowercase()
            if (trimmedName.isBlank()) error("Full Name is required.")
            if (trimmedEmail.isBlank() || !trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
                error("Please enter a valid institutional email address.")
            }
            if (password.length < 6) error("Password must be at least 6 characters long.")

            val existing = userDao.getUserByEmail(trimmedEmail)
            if (existing != null) error("An account with email $trimmedEmail already exists.")

            val user = User(
                name = trimmedName,
                username = trimmedEmail.substringBefore("@"),
                email = trimmedEmail,
                passwordHash = HashUtils.hashPassword(password),
                role = UserRole.TEACHER,
                department = department.ifBlank { "Computer Science & Engineering" },
                isActive = true
            )
            val id = userDao.insertUser(user)
            notificationDao.insertNotification(
                AppNotification(
                    title = "Teacher Account Created",
                    message = "Welcome to TrackEdu! Your account ($trimmedEmail) is active.",
                    category = NotificationCategory.SYSTEM,
                    targetScreen = "SETTINGS"
                )
            )
            user.copy(id = id)
        }
    }

    suspend fun authenticateTeacher(usernameOrEmail: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        runCatching {
            val query = usernameOrEmail.trim().lowercase()
            if (query.isBlank()) error("Please enter your email or username.")
            if (password.isBlank()) error("Please enter your password.")

            val user = userDao.getUserByUsernameOrEmail(query)
                ?: error("Invalid email or password.")

            if (!user.isActive) {
                error("This account has been deactivated.")
            }

            if (!HashUtils.verifyPassword(password, user.passwordHash)) {
                error("Invalid email or password.")
            }

            user
        }
    }

    // Completely wipe all database tables (Zero Data)
    suspend fun resetDatabase(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            studentDao.clearAllStudents()
            attendanceDao.clearAllAttendance()
            notificationDao.clearAll()
            userDao.clearAllUsers()
        }
    }
}
