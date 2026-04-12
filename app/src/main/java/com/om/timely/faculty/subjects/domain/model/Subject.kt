package com.om.timely.faculty.subjects.domain.model

data class Subject(
    val id: Int,
    val syllabusId: Int,
    val name: String,
    val code: String,
    val lecture: Int,
    val tutorial: Int,
    val practical: Int,
    val type: String,
    val abbreviation: String,
    val semester: Int,
    val department: String,
    val electiveGroup: String? = null,
    val isSelected: Boolean,
    val colorArgb: Int
)
