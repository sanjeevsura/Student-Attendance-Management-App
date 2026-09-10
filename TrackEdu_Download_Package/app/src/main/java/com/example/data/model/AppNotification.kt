package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class NotificationCategory {
    ATTENDANCE,
    REQUESTS,
    WARNINGS,
    SYSTEM
}

class NotificationConverters {
    @TypeConverter
    fun fromCategory(cat: NotificationCategory): String = cat.name

    @TypeConverter
    fun toCategory(value: String): NotificationCategory = runCatching {
        NotificationCategory.valueOf(value)
    }.getOrDefault(NotificationCategory.SYSTEM)
}

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val category: NotificationCategory = NotificationCategory.SYSTEM,
    val isRead: Boolean = false,
    val targetScreen: String? = null,
    val targetStudentId: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)
