package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.StudySession
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<StudySession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession): Long

    @Query("SELECT SUM(durationSeconds) FROM study_sessions WHERE subject = :subject")
    fun getTotalTimeForSubject(subject: String): Flow<Long?>

    @Query("SELECT SUM(durationSeconds) FROM study_sessions")
    fun getTotalStudyTime(): Flow<Long?>
}
