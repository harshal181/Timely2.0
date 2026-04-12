package com.om.timely.faculty.syllabus_extractor.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class SubjectType(val jsonValue: String) {
    CORE("core"),
    LAB("lab"),
    ELECTIVE("elective"),
    PROJECT("project"),
    INTERNSHIP("internship"),
    UNKNOWN("unknown");

    companion object {
        fun fromString(value: String?): SubjectType {
            return entries.find { it.jsonValue.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}

@Serializable
data class SubjectExtractionData(
    val semesters: List<SemesterData>
)

@Serializable
data class SemesterData(
    val semester: String,
    val subjects: List<SubjectData>
)

@Serializable
data class SubjectData(
    val id: String,
    val name: String,
    val courseCode: String?,
    val lecture: Int,
    val tutorial: Int,
    val practical: Int,
    val type: String,
    val electiveGroup: String?
)
