package com.om.timely.faculty.subjects.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubjectDto(
    val id: Int,
    @SerialName("syllabus_id")
    val syllabusId: Int,
    val name: String,
    val abbr: String,
    val code: String,
    val sem: Int,
    val dept: String,
    val lecture: Int,
    val tutorial: Int,
    val practical: Int,
    val type: String,
    @SerialName("elective_group")
    val electiveGroup: String? = null,
    @SerialName("is_selected")
    val isSelected: Boolean,
    @SerialName("color_argb")
    val colorArgb: Long // Keeping Long to prevent color integer overflow in Compose
)

@Serializable
data class SubjectCreateDto(
    @SerialName("syllabus_id")
    val syllabusId: Int,
    val name: String,
    val abbr: String,
    val code: String,
    val sem: Int,
    val lecture: Int = 0,
    val tutorial: Int = 0,
    val practical: Int = 0,
    val type: String,
    @SerialName("elective_group")
    val electiveGroup: String? = null,
    @SerialName("is_selected")
    val isSelected: Boolean = false,
    @SerialName("color_argb")
    val colorArgb: Long? = null
)

@Serializable
data class SubjectUpdateDto(
    @SerialName("syllabus_id")
    val syllabusId: Int? = null,
    val name: String? = null,
    val abbr: String? = null,
    val code: String? = null,
    val sem: Int? = null,
    val dept: String? = null,
    val lecture: Int? = null,
    val tutorial: Int? = null,
    val practical: Int? = null,
    val type: String? = null,
    @SerialName("elective_group")
    val electiveGroup: String? = null,
    @SerialName("is_selected")
    val isSelected: Boolean? = null,
    @SerialName("color_argb")
    val colorArgb: Long? = null
)
