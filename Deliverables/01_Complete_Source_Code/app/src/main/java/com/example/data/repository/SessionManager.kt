package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserRole
import com.example.data.model.UserSession

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("trackedu_session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
        private const val KEY_DEPARTMENT = "department"
        private const val KEY_LOGIN_TIMESTAMP = "login_timestamp"
        private const val KEY_EXPIRATION_TIMESTAMP = "expiration_timestamp"
        private const val KEY_THEME_MODE = "theme_mode" // "DARK", "LIGHT", "SYSTEM"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_SAVED_USERNAME = "saved_username"
        private const val KEY_SAVED_ROLE = "saved_role"

        // STRICT REQUIREMENT: Maximum 5 minutes absolute session lifetime
        const val SESSION_DURATION_MS = 5 * 60 * 1000L
    }

    /**
     * Remember Me persistence for login convenience without bypassing 5-min session security.
     */
    fun saveRememberMe(remember: Boolean, username: String) {
        prefs.edit().apply {
            putBoolean(KEY_REMEMBER_ME, remember)
            if (remember) {
                putString(KEY_SAVED_USERNAME, username)
            } else {
                remove(KEY_SAVED_USERNAME)
            }
            apply()
        }
    }

    fun isRememberMe(): Boolean {
        return prefs.getBoolean(KEY_REMEMBER_ME, false)
    }

    fun getSavedUsername(): String {
        return prefs.getString(KEY_SAVED_USERNAME, "") ?: ""
    }

    /**
     * Creates a new authentication session with an exact 5-minute expiry ceiling.
     * The session timer is absolute and must NEVER be extended automatically.
     */
    fun saveSession(
        id: String,
        name: String,
        username: String,
        email: String,
        role: UserRole,
        department: String
    ) {
        val now = System.currentTimeMillis()
        val expiry = now + SESSION_DURATION_MS

        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, id)
            putString(KEY_USER_NAME, name)
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
            putString(KEY_ROLE, role.name)
            putString(KEY_DEPARTMENT, department)
            putLong(KEY_LOGIN_TIMESTAMP, now)
            putLong(KEY_EXPIRATION_TIMESTAMP, expiry)
            apply()
        }
    }

    /**
     * Checks whether the session is currently active and within the 5-minute window.
     */
    fun isSessionValid(): Boolean {
        if (!prefs.getBoolean(KEY_IS_LOGGED_IN, false)) return false
        val expiry = prefs.getLong(KEY_EXPIRATION_TIMESTAMP, 0L)
        val now = System.currentTimeMillis()
        return now < expiry
    }

    fun isLoggedIn(): Boolean = isSessionValid()

    fun getSessionExpiry(): Long {
        return prefs.getLong(KEY_EXPIRATION_TIMESTAMP, 0L)
    }

    fun getSessionRemainingSeconds(): Long {
        if (!isSessionValid()) return 0L
        val expiry = getSessionExpiry()
        val now = System.currentTimeMillis()
        return if (expiry > now) (expiry - now) / 1000L else 0L
    }

    fun getSession(): UserSession? {
        if (!isSessionValid()) {
            clearSession()
            return null
        }
        val id = prefs.getString(KEY_USER_ID, "") ?: return null
        val name = prefs.getString(KEY_USER_NAME, "") ?: ""
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        val roleStr = prefs.getString(KEY_ROLE, UserRole.TEACHER.name) ?: UserRole.TEACHER.name
        val role = try {
            UserRole.valueOf(roleStr)
        } catch (_: Exception) {
            UserRole.TEACHER
        }
        val department = prefs.getString(KEY_DEPARTMENT, "Computer Science & Engineering") ?: "Computer Science & Engineering"

        return UserSession(
            id = id,
            name = name,
            email = email,
            role = role,
            department = department
        )
    }

    /**
     * Clears authentication session data only.
     * Note: Theme preference is strictly preserved and database data is never touched.
     */
    fun clearSession() {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            remove(KEY_USER_ID)
            remove(KEY_USER_NAME)
            remove(KEY_USERNAME)
            remove(KEY_EMAIL)
            remove(KEY_ROLE)
            remove(KEY_DEPARTMENT)
            remove(KEY_LOGIN_TIMESTAMP)
            remove(KEY_EXPIRATION_TIMESTAMP)
            apply()
        }
    }

    /**
     * Persistent theme preference (White/Light by default matching Google Stitch design).
     */
    fun saveThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
    }

    fun getThemeMode(): String {
        return prefs.getString(KEY_THEME_MODE, "LIGHT") ?: "LIGHT"
    }
}
