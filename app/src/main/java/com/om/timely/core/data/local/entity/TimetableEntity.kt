package com.om.timely.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetables")
data class TimetableEntity(
    @PrimaryKey
    val id: String, // e.g., "sem_6_div_A_Mon"
    val semester: Int,
    val division: String,
    val dayOfWeek: String,
    val dayId: Int? = null,
    val timelineJson: String, // Stored as a JSON Array string to preserve polymorphic types cleanly
    val updatedAt: Long
)
