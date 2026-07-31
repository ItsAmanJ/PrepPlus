package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_batches")
data class UserBatch(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val batchCode: String,
    val coachingName: String,
    val targetExam: String = "JEE Advanced / NEET",
    val isActive: Boolean = true
)
