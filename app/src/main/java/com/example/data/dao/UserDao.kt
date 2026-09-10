package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.User
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :usernameOrEmail OR email = :usernameOrEmail LIMIT 1")
    suspend fun getUserByUsernameOrEmail(usernameOrEmail: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE role = 'TEACHER' ORDER BY name ASC")
    fun getAllTeachers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRole(role: UserRole): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<User>): List<Long>

    @Update
    suspend fun updateUser(user: User)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: Long)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("SELECT COUNT(*) FROM users WHERE isActive = 1")
    suspend fun getActiveTeacherCount(): Int

    @Query("UPDATE users SET isActive = :isActive WHERE id = :id")
    suspend fun setUserActive(id: Long, isActive: Boolean)

    @Query("UPDATE users SET passwordHash = :passwordHash WHERE id = :id")
    suspend fun updatePassword(id: Long, passwordHash: String)

    @Query("DELETE FROM users")
    suspend fun clearAllUsers()
}
