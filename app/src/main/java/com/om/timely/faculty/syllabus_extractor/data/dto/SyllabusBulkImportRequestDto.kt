package com.om.timely.faculty.syllabus_extractor.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyllabusBulkImportRequestDto(
    @SerialName("name")
    val name: String,

    @SerialName("dept")
    val dept: String,

    @SerialName("term_type")
    val termType: String,
    
    @SerialName("subjectData")
    val subjectData: List<SyllabusBulkImportSemesterDto>
)

@Serializable
data class SyllabusBulkImportSemesterDto(
    @SerialName("semester")
    val semester: String,
    
    @SerialName("subjects")
    val subjects: List<SyllabusBulkImportSubjectDto>
)

@Serializable
data class SyllabusBulkImportSubjectDto(
    @SerialName("name")
    val name: String,
    
    @SerialName("course_code")
    val courseCode: String? = null,
    
    @SerialName("lecture")
    val lecture: Int,
    
    @SerialName("tutorial")
    val tutorial: Int,
    
    @SerialName("practical")
    val practical: Int,
    
    @SerialName("type")
    val type: String,
    
    @SerialName("elective_group")
    val electiveGroup: String? = null
)
