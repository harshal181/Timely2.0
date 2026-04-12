package com.om.timely.faculty.syllabus_extractor.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SyllabusDraft(
    val id: Int = 0,
    val name: String,
    val dept: String,
    val termType: String,
    val semesters: List<SemesterData>,
    val createdAt: Long = System.currentTimeMillis()
)
