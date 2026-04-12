package com.om.timely.faculty.syllabus_extractor.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyllabusExtractionResponseDto(
    val semesters: List<SemesterExtractedDto>
)

@Serializable
data class SemesterExtractedDto(
    val semester: String,
    val subjects: List<ExtractedSubjectDto>
)

@Serializable
data class ExtractedSubjectDto(
    val name: String,
    @SerialName("course_code") val courseCode: String? = null,
    val lecture: Int,
    val tutorial: Int,
    val practical: Int,
    val type: String,
    @SerialName("elective_group") val electiveGroup: String? = null
)
