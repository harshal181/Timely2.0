package com.om.timely.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A locally-saved snapshot of a fully-generated timetable.
 * The full template list is JSON-encoded via kotlinx.serialization.
 */
@Entity(tableName = "timetable_drafts")
data class TimetableDraftEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    /** Human-readable label shown in the schema selection screen */
    val label: String,
    val dept: String,
    val division: String,
    val syllabusId: Int,
    val syllabusName: String,
    /** Full serialized List<TimetableTemplate> as JSON */
    val templatesJson: String,
    /** Serialized List<String> warnings as JSON */
    val warningsJson: String = "[]",
    val savedAt: Long = System.currentTimeMillis()
)
