package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class HomeworkTag {
    DPP,        // Daily Practice Problem
    PYQ,        // Previous Year Questions
    MODULE,     // Coaching Module Exercise
    REVISION,   // Concept Notes & Revision
    SHEET,      // Assignment Sheet
    MOCK_TEST   // Mock Test Practice
}

enum class PriorityLevel {
    HIGH,
    MEDIUM,
    LOW
}

enum class HomeworkStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}

@Entity(tableName = "homework_tasks")
data class HomeworkTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val classId: Long? = null,
    val subject: String,
    val title: String,
    val notes: String = "", // Rich formulas or instructions
    val imagePath: String? = null, // Local URI or file path for uploaded/captured image
    val tag: HomeworkTag = HomeworkTag.DPP,
    val priority: PriorityLevel = PriorityLevel.MEDIUM,
    val status: HomeworkStatus = HomeworkStatus.PENDING,
    val dueDate: Long, // Epoch millis
    val createdAt: Long = System.currentTimeMillis(),
    val timeSpentSeconds: Long = 0, // Recorded by Focus Timer
    val isBacklog: Boolean = false // Flagged if overdue and incomplete
)
