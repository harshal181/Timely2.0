package com.om.timely.auth.data.dto

import com.om.timely.auth.domain.UserRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val identifier: String,
    val password: String
)

@Serializable
data class StudentCreateDto(
    @SerialName("stud_id") val studId: String,
    val semester: Int,
    val division: String,
    val batch: String,
    val course: String
)

@Serializable
data class FacultyCreateDto(
    @SerialName("fac_id") val facId: String,
    val designation: String,
    val office: String
)
@Serializable
data class UserOutDto(
    val name: String,
    val email: String,
    val role: String,
    @SerialName("created_at") val createdAt: String?,
    @SerialName("updated_at") val updatedAt: String?
)
@Serializable
data class UserCreateDto(
    val name: String,
    val email: String,
    val password: String,
    val role: String, // Backend probably expects a string like "STUDENT"
    @SerialName("student_details") val studentDetails: StudentCreateDto? = null,
    @SerialName("faculty_details") val facultyDetails: FacultyCreateDto? = null
)


@Serializable
data class CreateUserResultDto(
    val name: String,
    val email: String,
    val role: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class AuthResultDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String,
    val role: String
)
