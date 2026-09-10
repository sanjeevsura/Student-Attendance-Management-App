package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val username: String,
    val passwordHash: String,
    val role: UserRole,
    val email: String = "",
    val department: String = "Computer Science & Engineering",
    val isActive: Boolean = true
)
