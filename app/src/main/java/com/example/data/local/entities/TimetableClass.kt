package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable_classes")
data class TimetableClass(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val batchId: Long = 1,
    val dayOfWeek: Int, // 1 = Monday, 2 = Tuesday, ... 7 = Sunday
    val subject: String, // e.g. "Physics", "Chemistry", "Mathematics", "Biology"
    val topic: String, // e.g. "Rotational Dynamics & Torque"
    val roomNumber: String, // e.g. "Hall 302"
    val teacher: String, // e.g. "Prof. H.C. Verma"
    val startTime: String, // "08:30"
    val endTime: String // "10:00"
)
