package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    indices = [Index(value = ["rollNumber"], unique = true)]
)
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val rollNumber: String,
    val department: String = "Computer Science & Engineering",
    val year: String = "III Year",
    val section: String = "Section A",
    val email: String = "",
    val phone: String = "",
    val parentName: String = "Guardian",
    val parentPhone: String = "",
    val avatarInitials: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getInitials(): String {
        if (avatarInitials.isNotBlank()) return avatarInitials
        val parts = name.trim().split("\\s+".toRegex())
        return when {
            parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercase() ?: ""}${parts[1].firstOrNull()?.uppercase() ?: ""}"
            parts.isNotEmpty() && parts[0].isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "ST"
        }
    }
}
