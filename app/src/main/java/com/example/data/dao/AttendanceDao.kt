package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records ORDER BY date DESC, timestamp DESC")
    fun getAllRecords(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date = :date ORDER BY timestamp ASC")
    fun getRecordsForDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date = :date AND subject = :subject")
    fun getRecordsForDateAndSubject(date: String, subject: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getRecordsForStudent(studentId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId")
    suspend fun getRecordsForStudentDirect(studentId: Long): List<AttendanceRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(records: List<AttendanceRecord>): List<Long>

    @Query("SELECT COUNT(*) FROM attendance_records")
    fun getTotalRecordCount(): Flow<Int>

    @Query("DELETE FROM attendance_records WHERE date = :date AND subject = :subject")
    suspend fun deleteRecordsForDateAndSubject(date: String, subject: String)

    @Query("DELETE FROM attendance_records")
    suspend fun clearAllAttendance()
}
