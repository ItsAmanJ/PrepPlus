package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.TimetableClass
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_classes ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllClasses(): Flow<List<TimetableClass>>

    @Query("SELECT * FROM timetable_classes WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getClassesForDay(dayOfWeek: Int): Flow<List<TimetableClass>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(timetableClass: TimetableClass): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<TimetableClass>)

    @Update
    suspend fun updateClass(timetableClass: TimetableClass)

    @Query("DELETE FROM timetable_classes WHERE id = :id")
    suspend fun deleteClassById(id: Long)

    @Query("DELETE FROM timetable_classes WHERE batchId = :batchId")
    suspend fun clearBatchSchedule(batchId: Long)

    @Query("DELETE FROM timetable_classes")
    suspend fun clearAllClasses()
}
