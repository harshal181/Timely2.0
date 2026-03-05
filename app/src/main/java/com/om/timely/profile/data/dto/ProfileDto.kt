package com.om.timely.profile.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FullProfileOutDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    @SerialName("created_at") val createdAt: String?,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("student_details") val studentDetails: StudentOutDto? = null,
    @SerialName("faculty_details") val facultyDetails: FacultyOutDto? = null
)

@Serializable
data class UpdateUserDto(
    val name: String,
    @SerialName("student_details") val studentDetails: StudentDetailsDto? = null,
    @SerialName("faculty_details") val facultyDetails: FacultyDetailDto? = null

)

@Serializable
data class StudentDetailsDto(
    val semester: Int,
    val division: String,
    val batch: String
)
@Serializable
data class FacultyDetailDto(
    val designation: String,
    val office: String
)

@Serializable
data class StudentOutDto(
    val id: Int,
    @SerialName("user_id") val userId: String,
    @SerialName("stud_id") val studId: String,
    val semester: Int,
    val division: String,
    val batch: String,
    val course: String
)

@Serializable
data class FacultyOutDto(
    val id: Int,
    @SerialName("user_id") val userId: String,
    @SerialName("fac_id") val facId: String,
    val designation: String,
    val office: String
)
