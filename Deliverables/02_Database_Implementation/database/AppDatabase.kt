package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AttendanceDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.RequestDao
import com.example.data.dao.StudentDao
import com.example.data.dao.UserDao
import com.example.data.model.AppNotification
import com.example.data.model.AppRequest
import com.example.data.model.AttendanceConverters
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.AttendanceType
import com.example.data.model.NotificationCategory
import com.example.data.model.NotificationConverters
import com.example.data.model.RequestConverters
import com.example.data.model.RequestStatus
import com.example.data.model.RequestType
import com.example.data.model.Student
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.repository.HashUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import androidx.room.migration.Migration

import com.example.data.model.UserConverters

@Database(
    entities = [Student::class, AttendanceRecord::class, AppRequest::class, AppNotification::class, User::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(AttendanceConverters::class, RequestConverters::class, NotificationConverters::class, UserConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun requestDao(): RequestDao
    abstract fun notificationDao(): NotificationDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `app_requests` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `type` TEXT NOT NULL,
                        `submittedBy` TEXT NOT NULL,
                        `studentName` TEXT NOT NULL DEFAULT '',
                        `studentRollNumber` TEXT NOT NULL DEFAULT '',
                        `department` TEXT NOT NULL DEFAULT 'Computer Science & Engineering',
                        `year` TEXT NOT NULL DEFAULT 'III Year',
                        `section` TEXT NOT NULL DEFAULT 'Section A',
                        `email` TEXT NOT NULL DEFAULT '',
                        `phone` TEXT NOT NULL DEFAULT '',
                        `parentName` TEXT NOT NULL DEFAULT '',
                        `parentPhone` TEXT NOT NULL DEFAULT '',
                        `studentId` INTEGER,
                        `date` TEXT NOT NULL DEFAULT '',
                        `subject` TEXT NOT NULL DEFAULT '',
                        `requestedStatus` TEXT NOT NULL DEFAULT 'PRESENT',
                        `requestedType` TEXT NOT NULL DEFAULT 'REGULAR',
                        `reason` TEXT NOT NULL,
                        `status` TEXT NOT NULL DEFAULT 'PENDING',
                        `adminRemarks` TEXT NOT NULL DEFAULT '',
                        `createdAt` INTEGER NOT NULL DEFAULT 0,
                        `updatedAt` INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `app_notifications` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `message` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `isRead` INTEGER NOT NULL DEFAULT 0,
                        `targetScreen` TEXT,
                        `timestamp` INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `users` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `username` TEXT NOT NULL,
                        `passwordHash` TEXT NOT NULL,
                        `role` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `department` TEXT NOT NULL,
                        `isActive` INTEGER NOT NULL DEFAULT 1
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_1_2.migrate(db)
                MIGRATION_2_3.migrate(db)
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trackedu_attendance.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_1_3)
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
