package com.om.timely.faculty.timetable_generation.domain.model

data class TimetableJobStatusModel(
    val jobId: String,
    val status: String,
    val stage: String,
    val error: String? = null,
    val warnings: List<String> = emptyList()
)
