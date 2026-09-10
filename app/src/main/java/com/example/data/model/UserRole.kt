package com.example.data.model

import androidx.room.TypeConverter

enum class UserRole {
    TEACHER
}

class UserConverters {
    @TypeConverter
    fun fromRole(role: UserRole): String = role.name

    @TypeConverter
    fun toRole(value: String): UserRole = UserRole.TEACHER
}

data class UserSession(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole = UserRole.TEACHER,
    val department: String = "Computer Science & Engineering"
)

