package com.om.timely.auth.domain

enum class UserRole {
    STUDENT,
    FACULTY
}

data class AuthResult(
    val accessToken: String,
    val refreshToken: String,
    val role: UserRole
)

data class User(
    val name: String,
    val email: String,
    val role: UserRole,
    val dept: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

//data class CreateUserResult (
//    val name: String,
//    val email: String,
//    val role: UserRole,
//    val created_at: String? = null,
//    val updated_at: String? = null
//)
//
//data class LoginRequest(
//    val identifier: String,
//    val password: String
//)
//
//data class StudentCreate(
//    val stud_id: String,
//    val semester: Int,
//    val division: String,
//    val batch: String,
//    val course: String
//)
//
//data class FacultyCreate(
//    val fac_id: String,
//    val designation: String,
//    val office: String
//)
//
//data class UserCreate(
//    val name: String,
//    val email: String,
//    val password: String,
//    val role: UserRole,
//    val student_details: StudentCreate? = null,
//    val faculty_details: FacultyCreate? = null
//)


