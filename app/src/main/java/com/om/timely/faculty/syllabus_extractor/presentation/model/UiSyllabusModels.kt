package com.om.timely.faculty.syllabus_extractor.presentation.model

import java.util.UUID

data class SemesterUi(
    val semester: String,
    val subjects: List<ExtractedSubjectUi>
)

data class ExtractedSubjectUi(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val courseCode: String?,
    val lecture: String,
    val tutorial: String,
    val practical: String,
    val type: String,
    val electiveGroup: String?,
    val isEditing: Boolean = false,
    val nameError: String? = null,
    val codeError: String? = null,
    val hoursError: String? = null,
    val electiveGroupError: String? = null
)
