package com.om.timely.faculty.timetable_generation.domain.model

data class TimetablePdfAsset(
    val label: String,
    val group: String,
    val semester: Int?,
    val filename: String,
    val url: String,
    val secureUrl: String
)
