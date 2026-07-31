package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.BatchDao
import com.example.data.local.dao.HomeworkDao
import com.example.data.local.dao.StudySessionDao
import com.example.data.local.dao.TimetableDao
import com.example.data.local.entities.HomeworkTask
import com.example.data.local.entities.StudySession
import com.example.data.local.entities.TimetableClass
import com.example.data.local.entities.UserBatch

@Database(
    entities = [
        UserBatch::class,
        TimetableClass::class,
        HomeworkTask::class,
        StudySession::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun batchDao(): BatchDao
    abstract fun timetableDao(): TimetableDao
    abstract fun homeworkDao(): HomeworkDao
    abstract fun studySessionDao(): StudySessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "preppulse_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
