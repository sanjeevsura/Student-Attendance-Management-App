package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class RequestType {
    ADD_STUDENT,
    ATTENDANCE_CORRECTION,
    SPECIAL_ATTENDANCE
}

enum class RequestStatus {
    PENDING,
    APPROVED,
    REJECTED
}

class RequestConverters {
    @TypeConverter
    fun fromType(type: RequestType): String = type.name

    @TypeConverter
    fun toType(value: String): RequestType = runCatching {
        RequestType.valueOf(value)
    }.getOrDefault(RequestType.ADD_STUDENT)

    @TypeConverter
    fun fromStatus(status: RequestStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): RequestStatus = runCatching {
        RequestStatus.valueOf(value)
    }.getOrDefault(RequestStatus.PENDING)
}

@Entity(tableName = "app_requests")
data class AppRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: RequestType,
    val submittedBy: String, // Teacher Name / Email
    val studentName: String = "",
    val studentRollNumber: String = "",
    val department: String = "Computer Science & Engineering",
    val year: String = "III Year",
    val section: String = "Section A",
    val email: String = "",
    val phone: String = "",
    val parentName: String = "",
    val parentPhone: String = "",
    val studentId: Long? = null,
    val date: String = "",
    val subject: String = "",
    val requestedStatus: AttendanceStatus = AttendanceStatus.PRESENT,
    val requestedType: AttendanceType = AttendanceType.REGULAR,
    val reason: String,
    val status: RequestStatus = RequestStatus.PENDING,
    val adminRemarks: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
