package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.local.entities.HomeworkStatus
import com.example.data.local.entities.HomeworkTag
import com.example.data.local.entities.PriorityLevel

class Converters {
    @TypeConverter
    fun fromHomeworkTag(tag: HomeworkTag): String = tag.name

    @TypeConverter
    fun toHomeworkTag(value: String): HomeworkTag = try {
        HomeworkTag.valueOf(value)
    } catch (e: Exception) {
        HomeworkTag.DPP
    }

    @TypeConverter
    fun fromPriorityLevel(priority: PriorityLevel): String = priority.name

    @TypeConverter
    fun toPriorityLevel(value: String): PriorityLevel = try {
        PriorityLevel.valueOf(value)
    } catch (e: Exception) {
        PriorityLevel.MEDIUM
    }

    @TypeConverter
    fun fromHomeworkStatus(status: HomeworkStatus): String = status.name

    @TypeConverter
    fun toHomeworkStatus(value: String): HomeworkStatus = try {
        HomeworkStatus.valueOf(value)
    } catch (e: Exception) {
        HomeworkStatus.PENDING
    }
}
