package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppRequest
import com.example.data.model.RequestStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface RequestDao {
    @Query("SELECT * FROM app_requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<AppRequest>>

    @Query("SELECT * FROM app_requests WHERE status = :status ORDER BY createdAt DESC")
    fun getRequestsByStatus(status: RequestStatus): Flow<List<AppRequest>>

    @Query("SELECT * FROM app_requests WHERE submittedBy = :submittedBy ORDER BY createdAt DESC")
    fun getRequestsByTeacher(submittedBy: String): Flow<List<AppRequest>>

    @Query("SELECT COUNT(*) FROM app_requests WHERE status = 'PENDING'")
    fun getPendingRequestCount(): Flow<Int>

    @Query("SELECT * FROM app_requests WHERE id = :id LIMIT 1")
    suspend fun getRequestById(id: Long): AppRequest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: AppRequest): Long

    @Update
    suspend fun updateRequest(request: AppRequest)

    @Query("DELETE FROM app_requests WHERE id = :id")
    suspend fun deleteRequestById(id: Long)

    @Query("DELETE FROM app_requests")
    suspend fun clearAllRequests()
}
