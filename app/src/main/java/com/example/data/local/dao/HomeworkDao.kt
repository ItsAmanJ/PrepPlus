package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.HomeworkTask
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeworkDao {
    @Query("SELECT * FROM homework_tasks ORDER BY priority DESC, dueDate ASC")
    fun getAllTasks(): Flow<List<HomeworkTask>>

    @Query("SELECT * FROM homework_tasks WHERE isBacklog = 0 AND status != 'COMPLETED' ORDER BY dueDate ASC")
    fun getActiveTasks(): Flow<List<HomeworkTask>>

    @Query("SELECT * FROM homework_tasks WHERE (isBacklog = 1 OR (dueDate < :currentTimestamp AND status != 'COMPLETED')) AND status != 'COMPLETED' ORDER BY priority DESC, dueDate ASC")
    fun getBacklogTasks(currentTimestamp: Long = System.currentTimeMillis()): Flow<List<HomeworkTask>>

    @Query("SELECT * FROM homework_tasks WHERE status = 'COMPLETED' ORDER BY dueDate DESC")
    fun getCompletedTasks(): Flow<List<HomeworkTask>>

    @Query("SELECT * FROM homework_tasks WHERE classId = :classId ORDER BY id DESC")
    fun getTasksForClass(classId: Long): Flow<List<HomeworkTask>>

    @Query("SELECT * FROM homework_tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): HomeworkTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: HomeworkTask): Long

    @Update
    suspend fun updateTask(task: HomeworkTask)

    @Query("DELETE FROM homework_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("UPDATE homework_tasks SET isBacklog = 1 WHERE dueDate < :currentTimestamp AND status != 'COMPLETED'")
    suspend fun markOverdueAsBacklog(currentTimestamp: Long = System.currentTimeMillis())

    @Query("UPDATE homework_tasks SET timeSpentSeconds = timeSpentSeconds + :additionalSeconds WHERE id = :id")
    suspend fun addTimeSpent(id: Long, additionalSeconds: Long)
}
