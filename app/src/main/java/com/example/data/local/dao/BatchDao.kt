package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.UserBatch
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchDao {
    @Query("SELECT * FROM user_batches ORDER BY id DESC")
    fun getAllBatches(): Flow<List<UserBatch>>

    @Query("SELECT * FROM user_batches WHERE isActive = 1 LIMIT 1")
    fun getActiveBatch(): Flow<UserBatch?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: UserBatch): Long

    @Update
    suspend fun updateBatch(batch: UserBatch)

    @Query("UPDATE user_batches SET isActive = 0")
    suspend fun deactivateAllBatches()
}
