package com.om.timely.faculty.timetable_generation.domain.model

/**
 * A locally-persisted snapshot of a generated timetable the user hasn't published yet.
 */
data class TimetableDraft(
    val id: Int,
    val label: String,
    val dept: String,
    val division: String,
    val syllabusId: Int,
    val syllabusName: String,
    val templates: List<TimetableTemplate>,
    val warnings: List<String>,
    val savedAt: Long
)
