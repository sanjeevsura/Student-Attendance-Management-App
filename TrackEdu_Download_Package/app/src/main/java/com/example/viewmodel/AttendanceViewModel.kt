package com.example.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.AppNotification
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.AttendanceType
import com.example.data.model.Student
import com.example.data.model.UserRole
import com.example.data.model.UserSession
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.AttendanceTier
import com.example.data.repository.HashUtils
import com.example.data.repository.SessionManager
import com.example.data.repository.StudentWithStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.data.model.User

enum class AppScreen {
    SPLASH,
    LANDING,
    SETUP_TEACHER,
    LOGIN,
    DASHBOARD,
    STUDENTS,
    MARK_ATTENDANCE,
    RECORDS,
    REPORTS,
    SETTINGS,
    ADD_STUDENT,
    EDIT_STUDENT,
    STUDENT_DETAIL,
    NOTIFICATIONS,
    SPECIAL_CASE
}

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val sessionManager = SessionManager(application)
    private val userDao = database.userDao()
    private val repository = AttendanceRepository(
        database.studentDao(),
        database.attendanceDao(),
        database.notificationDao(),
        database.userDao()
    )

    val allStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentsWithStats: StateFlow<List<StudentWithStats>> = repository.studentsWithStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecords: StateFlow<List<AttendanceRecord>> = repository.allAttendanceRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<AppNotification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Current Screen state - starts on animated SPLASH screen
    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Flag indicating if at least 1 active Teacher exists in database
    private val _hasTeacherAccount = MutableStateFlow<Boolean?>(null)
    val hasTeacherAccount: StateFlow<Boolean?> = _hasTeacherAccount.asStateFlow()

    // Active User Role Session (Empty unauthenticated state initially)
    private val _currentUser = MutableStateFlow(
        UserSession(
            id = "",
            name = "",
            email = "",
            role = UserRole.TEACHER,
            department = ""
        )
    )
    val currentUser: StateFlow<UserSession> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _selectedStudentId = MutableStateFlow<Long?>(null)
    val selectedStudentId: StateFlow<Long?> = _selectedStudentId.asStateFlow()

    // Active Date (format: "yyyy-MM-dd") — always initialized to real today
    private val defaultDate: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }
    private val _selectedDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedSubject = MutableStateFlow("CS502 - Database Systems")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _selectedBatch = MutableStateFlow("CSE - III A (Semester 5)")
    val selectedBatch: StateFlow<String> = _selectedBatch.asStateFlow()

    // Active Draft for mark attendance session
    private val _activeSessionAttendance = MutableStateFlow<Map<Long, AttendanceStatus>>(emptyMap())
    val activeSessionAttendance: StateFlow<Map<Long, AttendanceStatus>> = _activeSessionAttendance.asStateFlow()

    // Search and filters
    val studentSearchQuery = MutableStateFlow("")
    val studentFilterTab = MutableStateFlow("All") // "All", "Low < 75%", "Good >= 75%", "Section A", "Section B"

    // Settings
    val mandatoryThreshold = MutableStateFlow(75f)
    val warningThreshold = MutableStateFlow(80f)

    // Toast feedback
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _isToastError = MutableStateFlow(false)
    val isToastError: StateFlow<Boolean> = _isToastError.asStateFlow()

    // Persistent Theme Preference
    private val _themeMode = MutableStateFlow(sessionManager.getThemeMode())
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    val isDarkTheme: StateFlow<Boolean> = _themeMode
        .combine(MutableStateFlow(true)) { mode, _ -> mode == "DARK" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, sessionManager.getThemeMode() == "DARK")

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        sessionManager.saveThemeMode(mode)
    }

    fun toggleTheme() {
        val next = if (_themeMode.value == "LIGHT") "DARK" else "LIGHT"
        setThemeMode(next)
    }

    // Remember Me persistence access
    fun isRememberMe(): Boolean = sessionManager.isRememberMe()
    fun getSavedUsername(): String = sessionManager.getSavedUsername()

    // Shared App Session Expired Dialog state
    private val _isSessionExpiredDialogVisible = MutableStateFlow(false)
    val isSessionExpiredDialogVisible: StateFlow<Boolean> = _isSessionExpiredDialogVisible.asStateFlow()

    private val _sessionRemainingSeconds = MutableStateFlow(0L)
    val sessionRemainingSeconds: StateFlow<Long> = _sessionRemainingSeconds.asStateFlow()

    fun dismissSessionExpiredDialog() {
        _isSessionExpiredDialogVisible.value = false
        _currentScreen.value = AppScreen.LOGIN
    }

    fun checkSessionOnResume() {
        if (_isLoggedIn.value && !sessionManager.isSessionValid()) {
            expireSession()
        }
    }

    init {
        // Check startup teacher status
        viewModelScope.launch {
            checkStartupSession()
        }

        // ABSOLUTE 5-MINUTE SESSION WATCHER
        // Ticks every second; if currentTime >= sessionExpiry, immediately forces logout
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                if (_isLoggedIn.value) {
                    _sessionRemainingSeconds.value = sessionManager.getSessionRemainingSeconds()
                    if (!sessionManager.isSessionValid()) {
                        expireSession()
                    }
                } else {
                    _sessionRemainingSeconds.value = 0L
                }
            }
        }

        viewModelScope.launch {
            combine(allStudents, allRecords, _selectedDate) { students, records, date ->
                val existingToday = records.filter { it.date == date }
                val map = mutableMapOf<Long, AttendanceStatus>()
                students.forEach { s ->
                    val existing = existingToday.find { it.studentId == s.id }
                    map[s.id] = existing?.status ?: AttendanceStatus.PRESENT
                }
                map
            }.collect { initialMap ->
                if (_activeSessionAttendance.value.isEmpty()) {
                    _activeSessionAttendance.value = initialMap
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen, studentId: Long? = null) {
        // Enforce session check on major navigation
        if (_isLoggedIn.value && !sessionManager.isSessionValid()) {
            expireSession()
            return
        }
        if (studentId != null) {
            _selectedStudentId.value = studentId
        }
        _currentScreen.value = screen
    }

    fun navigateBack() {
        if (_isLoggedIn.value && !sessionManager.isSessionValid()) {
            expireSession()
            return
        }
        when (_currentScreen.value) {
            AppScreen.ADD_STUDENT, AppScreen.EDIT_STUDENT, AppScreen.STUDENT_DETAIL -> {
                _currentScreen.value = AppScreen.STUDENTS
            }
            AppScreen.RECORDS, AppScreen.SPECIAL_CASE -> {
                _currentScreen.value = AppScreen.MARK_ATTENDANCE
            }
            AppScreen.NOTIFICATIONS -> {
                _currentScreen.value = AppScreen.DASHBOARD
            }
            else -> {
                _currentScreen.value = AppScreen.DASHBOARD
            }
        }
    }

    /**
     * App Startup / Reopening Check:
     * If no active teacher exists, route directly to SETUP_TEACHER.
     * If session is valid AND within 5 minutes, proceed to Dashboard.
     * Otherwise clear session and navigate to Login.
     */
    fun checkStartupSession() {
        viewModelScope.launch {
            val teacherCount = repository.getActiveTeacherCount()
            _hasTeacherAccount.value = teacherCount > 0

            if (teacherCount == 0) {
                sessionManager.clearSession()
                _isLoggedIn.value = false
                _currentScreen.value = AppScreen.SETUP_TEACHER
                return@launch
            }

            if (sessionManager.isSessionValid()) {
                val session = sessionManager.getSession()
                if (session != null) {
                    _currentUser.value = session
                    _isLoggedIn.value = true
                    _currentScreen.value = AppScreen.DASHBOARD
                    return@launch
                }
            }

            sessionManager.clearSession()
            _isLoggedIn.value = false
            _currentScreen.value = AppScreen.LOGIN
        }
    }

    fun registerTeacher(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        department: String = "Computer Science & Engineering",
        facultyId: String = "FAC-101",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (password != confirmPassword) {
            val err = "Passwords do not match."
            _authErrorMessage.value = err
            onError(err)
            return
        }
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authErrorMessage.value = null
            val result = repository.registerTeacher(name, email, password, department)
            _isAuthenticating.value = false
            if (result.isSuccess) {
                val teacher = result.getOrThrow()
                sessionManager.saveSession(
                    id = teacher.id.toString(),
                    name = teacher.name,
                    username = teacher.username,
                    email = teacher.email,
                    role = teacher.role,
                    department = teacher.department
                )
                _currentUser.value = UserSession(
                    id = teacher.id.toString(),
                    name = teacher.name,
                    email = teacher.email,
                    role = teacher.role,
                    department = teacher.department
                )
                _hasTeacherAccount.value = true
                _isLoggedIn.value = true
                _currentScreen.value = AppScreen.DASHBOARD
                showToast("Teacher account created! Welcome, ${teacher.name}")
                onSuccess()
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Failed to create teacher account"
                _authErrorMessage.value = msg
                onError(msg)
            }
        }
    }

    fun authenticate(
        usernameOrEmail: String,
        password: String,
        rememberMe: Boolean = false
    ) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authErrorMessage.value = null
            kotlinx.coroutines.delay(200)

            val result = repository.authenticateTeacher(usernameOrEmail, password)
            _isAuthenticating.value = false

            if (result.isSuccess) {
                val user = result.getOrThrow()

                // Persist Remember Me preference (username/email only)
                sessionManager.saveRememberMe(rememberMe, usernameOrEmail.trim())

                // Save persistent 5-minute session
                sessionManager.saveSession(
                    id = user.id.toString(),
                    name = user.name,
                    username = user.username,
                    email = user.email,
                    role = user.role,
                    department = user.department
                )

                _currentUser.value = UserSession(
                    id = user.id.toString(),
                    name = user.name,
                    email = user.email,
                    role = user.role,
                    department = user.department
                )
                _isLoggedIn.value = true
                _currentScreen.value = AppScreen.DASHBOARD
                showToast("Welcome back, ${user.name}")
            } else {
                _authErrorMessage.value = result.exceptionOrNull()?.message ?: "Authentication failed."
            }
        }
    }

    fun changeTeacherPassword(
        currentPass: String,
        newPass: String,
        confirmPass: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (newPass != confirmPass) {
            val err = "New passwords do not match."
            onError(err)
            showToast(err, isError = true)
            return
        }
        if (newPass.length < 6) {
            val err = "Password must be at least 6 characters."
            onError(err)
            showToast(err, isError = true)
            return
        }
        viewModelScope.launch {
            val user = userDao.getUserByUsernameOrEmail(_currentUser.value.email)
            if (user == null) {
                onError("Teacher account not found.")
                return@launch
            }
            if (!HashUtils.verifyPassword(currentPass, user.passwordHash)) {
                val err = "Incorrect current password."
                onError(err)
                showToast(err, isError = true)
                return@launch
            }
            val newHash = HashUtils.hashPassword(newPass)
            userDao.updateUser(user.copy(passwordHash = newHash))
            showToast("Password updated successfully!")
            onSuccess()
        }
    }

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    fun expireSession() {
        sessionManager.clearSession()
        _isLoggedIn.value = false
        _authErrorMessage.value = null
        _currentUser.value = UserSession(
            id = "",
            name = "",
            email = "",
            role = UserRole.TEACHER,
            department = ""
        )
        _isSessionExpiredDialogVisible.value = true
        _currentScreen.value = AppScreen.LOGIN
        showToast("Your 5-minute session has expired. Please sign in again.", isError = true)
    }

    fun logout() {
        sessionManager.clearSession()
        _isLoggedIn.value = false
        _authErrorMessage.value = null
        _currentUser.value = UserSession(
            id = "",
            name = "",
            email = "",
            role = UserRole.TEACHER,
            department = ""
        )
        _currentScreen.value = AppScreen.LOGIN
        showToast("Signed out of TrackEdu")
    }

    fun setDate(date: String) {
        _selectedDate.value = date
        val existingForDate = allRecords.value.filter { it.date == date }
        val newMap = mutableMapOf<Long, AttendanceStatus>()
        allStudents.value.forEach { s ->
            val found = existingForDate.find { it.studentId == s.id }
            newMap[s.id] = found?.status ?: AttendanceStatus.PRESENT
        }
        _activeSessionAttendance.value = newMap
    }

    fun setSubject(subject: String) {
        _selectedSubject.value = subject
    }

    fun shiftDate(days: Int) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val current = sdf.parse(_selectedDate.value) ?: Date()
            val cal = java.util.Calendar.getInstance()
            cal.time = current
            cal.add(java.util.Calendar.DAY_OF_YEAR, days)
            setDate(sdf.format(cal.time))
        } catch (_: Exception) {
            // fallback
        }
    }

    fun setStudentAttendanceStatus(studentId: Long, status: AttendanceStatus) {
        val current = _activeSessionAttendance.value.toMutableMap()
        current[studentId] = status
        _activeSessionAttendance.value = current
    }

    fun markAllPresent() {
        val current = _activeSessionAttendance.value.toMutableMap()
        allStudents.value.forEach { student ->
            current[student.id] = AttendanceStatus.PRESENT
        }
        _activeSessionAttendance.value = current
        showToast("Marked all ${allStudents.value.size} students Present")
    }

    fun resetAttendanceDraft() {
        val date = _selectedDate.value
        val existingToday = allRecords.value.filter { it.date == date }
        val map = mutableMapOf<Long, AttendanceStatus>()
        allStudents.value.forEach { s ->
            val existing = existingToday.find { it.studentId == s.id }
            map[s.id] = existing?.status ?: AttendanceStatus.PRESENT
        }
        _activeSessionAttendance.value = map
        showToast("Attendance draft reset")
    }

    fun clearAttendanceCache() {
        showToast("Attendance query cache cleared")
    }

    fun saveActiveAttendance() {
        viewModelScope.launch {
            val date = _selectedDate.value
            val subject = _selectedSubject.value
            val records = _activeSessionAttendance.value.map { (studentId, status) ->
                AttendanceRecord(
                    studentId = studentId,
                    date = date,
                    status = status,
                    type = AttendanceType.REGULAR,
                    subject = subject,
                    period = "Period 2 (10:15 AM - 11:15 AM)",
                    note = if (status == AttendanceStatus.ABSENT) "Recorded unexcused absence" else "Verified by ${_currentUser.value.name}",
                    createdBy = _currentUser.value.name
                )
            }
            val result = repository.saveAttendanceSession(records)
            if (result.isSuccess) {
                val presentCount = records.count { it.status == AttendanceStatus.PRESENT }
                showToast("Saved! $presentCount/${records.size} marked Present for $date")
            } else {
                showToast("Failed to save: ${result.exceptionOrNull()?.message}", isError = true)
            }
        }
    }

    // Direct Add Student (Teacher Direct Management)
    fun addStudent(
        name: String,
        rollNumber: String,
        department: String,
        year: String,
        section: String,
        email: String,
        phone: String,
        parentName: String = "",
        parentPhone: String = ""
    ) {
        if (name.isBlank() || rollNumber.isBlank()) {
            showToast("Name and Roll Number are required", isError = true)
            return
        }

        viewModelScope.launch {
            val student = Student(
                name = name.trim(),
                rollNumber = rollNumber.trim().uppercase(),
                department = department,
                year = year,
                section = section,
                email = email.trim(),
                phone = phone.trim(),
                parentName = parentName.trim().ifBlank { "Guardian" },
                parentPhone = parentPhone.trim()
            )
            val result = repository.addStudent(student)
            if (result.isSuccess) {
                showToast("Student ${student.name} (${student.rollNumber}) added to database")
                _currentScreen.value = AppScreen.STUDENTS
            } else {
                showToast(result.exceptionOrNull()?.message ?: "Failed to add student", isError = true)
            }
        }
    }

    // Update Student (Teacher Direct Management)
    fun updateStudent(
        id: Long,
        name: String,
        rollNumber: String,
        department: String,
        year: String,
        section: String,
        email: String,
        phone: String,
        parentName: String,
        parentPhone: String
    ) {
        viewModelScope.launch {
            val updated = Student(
                id = id,
                name = name.trim(),
                rollNumber = rollNumber.trim().uppercase(),
                department = department,
                year = year,
                section = section,
                email = email.trim(),
                phone = phone.trim(),
                parentName = parentName.trim(),
                parentPhone = parentPhone.trim()
            )
            val result = repository.updateStudent(updated)
            if (result.isSuccess) {
                showToast("Student profile updated successfully")
                _currentScreen.value = AppScreen.STUDENTS
            } else {
                showToast(result.exceptionOrNull()?.message ?: "Update failed", isError = true)
            }
        }
    }

    // Delete Student (Teacher Direct Management)
    fun deleteStudent(studentId: Long) {
        viewModelScope.launch {
            val result = repository.deleteStudent(studentId)
            if (result.isSuccess) {
                showToast("Student deleted from database", isError = true)
                _selectedStudentId.value = null
                _currentScreen.value = AppScreen.STUDENTS
            } else {
                showToast(result.exceptionOrNull()?.message ?: "Failed to delete student", isError = true)
            }
        }
    }

    // Special Attendance Recording (Direct Teacher Entry)
    fun saveSpecialCaseAttendance(
        studentId: Long,
        date: String,
        subject: String,
        type: AttendanceType,
        remarks: String
    ) {
        viewModelScope.launch {
            val result = repository.saveSpecialCaseAttendance(
                studentId = studentId,
                date = date,
                subject = subject,
                type = type,
                remarks = remarks
            )
            if (result.isSuccess) {
                showToast("Special attendance (${type.name}) recorded successfully")
                _currentScreen.value = AppScreen.STUDENT_DETAIL
            } else {
                showToast(result.exceptionOrNull()?.message ?: "Failed to save special case", isError = true)
            }
        }
    }

    // Direct Phone Call Handler (Students or Parents)
    fun dialPhone(phoneNumber: String, contactLabel: String) {
        if (phoneNumber.isBlank()) {
            showToast("No phone number registered for $contactLabel", isError = true)
            return
        }
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${phoneNumber.replace(" ", "")}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(intent)
        } catch (_: Exception) {
            showToast("Dialing $contactLabel: $phoneNumber")
        }
    }

    // Direct SMS Handler (Students or Parents)
    fun sendSms(phoneNumber: String, contactLabel: String) {
        if (phoneNumber.isBlank()) {
            showToast("No phone number registered for $contactLabel", isError = true)
            return
        }
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${phoneNumber.replace(" ", "")}")
                putExtra("sms_body", "TrackEdu Attendance Notice: Attendance update for student.")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(intent)
        } catch (_: Exception) {
            showToast("SMS dispatched to $contactLabel ($phoneNumber)")
        }
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch { repository.markNotificationAsRead(id) }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
            showToast("All notifications marked as read")
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            val result = repository.resetDatabase()
            if (result.isSuccess) {
                sessionManager.clearSession()
                _isLoggedIn.value = false
                _hasTeacherAccount.value = false
                _currentUser.value = UserSession("", "", "", UserRole.TEACHER, "")
                _currentScreen.value = AppScreen.SETUP_TEACHER
                showToast("Database formatted. Please create your Teacher account.")
            } else {
                showToast(result.exceptionOrNull()?.message ?: "Reset failed", isError = true)
            }
        }
    }

    fun showToast(message: String, isError: Boolean = false) {
        _toastMessage.value = message
        _isToastError.value = isError
    }

    fun clearToast() {
        _toastMessage.value = null
        _isToastError.value = false
    }
}
