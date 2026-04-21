package com.om.timely.faculty.timetable_generation.domain.model

data class TimetableGroup(
    val id: Int,
    val name: String,
    val dept: String,
    val termType: String,
    val isActive: Boolean,
    val templateCount: Int,
    val semesters: List<Int>,
    val createdAt: String,
    val updatedAt: String,
    val templates: List<TimetableTemplate> = emptyList()
)
