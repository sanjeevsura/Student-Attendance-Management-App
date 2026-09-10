package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class AttendanceStatus {
    PRESENT,
    ABSENT
}

enum class AttendanceType {
    REGULAR,
    MEDICAL,
    OD,         // On Duty
    SPORTS,
    EXAM,
    LEAVE,
    CORRECTION,
    OTHER
}

class AttendanceConverters {
    @TypeConverter
    fun fromStatus(status: AttendanceStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): AttendanceStatus = runCatching {
        AttendanceStatus.valueOf(value)
    }.getOrDefault(AttendanceStatus.PRESENT)

    @TypeConverter
    fun fromType(type: AttendanceType): String = type.name

    @TypeConverter
    fun toType(value: String): AttendanceType = runCatching {
        AttendanceType.valueOf(value)
    }.getOrDefault(AttendanceType.REGULAR)
}

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studentId", "date", "subject"], unique = true),
        Index(value = ["studentId"]),
        Index(value = ["date"])
    ]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val date: String, // e.g. "2026-09-07"
    val status: AttendanceStatus,
    val type: AttendanceType = AttendanceType.REGULAR,
    val subject: String = "CS502 - Database Systems",
    val period: String = "Period 2 (10:15 AM - 11:15 AM)",
    val note: String = "",
    val remarks: String = "",
    val createdBy: String = "Prof. Sharma",
    val updatedAt: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis()
)
