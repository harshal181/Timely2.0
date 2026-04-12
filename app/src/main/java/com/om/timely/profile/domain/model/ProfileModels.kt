package com.om.timely.profile.domain.model

import com.om.timely.auth.domain.UserRole

data class FullProfile(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val studentDetails: StudentProfile? = null,
    val facultyDetails: FacultyProfile? = null,
    val dept: String? = null
)

data class StudentProfile(
    val studId: String,
    val semester: Int,
    val division: String,
    val batch: String,
    val course: String // Used functionally as department on UI
)

data class FacultyProfile(
    val facId: String,
    val designation: String,
    val office: String,
    val subjects: List<ProfileSubject> = emptyList(),
    val department: String = ""
)

data class ProfileSubject(
    val id: Int,
    val name: String,
    val code: String,
    val colorArgb: Long
)
