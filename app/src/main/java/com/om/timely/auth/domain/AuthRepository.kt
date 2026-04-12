package com.om.timely.auth.domain

import com.om.timely.auth.domain.error.AuthError
import com.om.timely.core.domain.util.Result

interface AuthRepository {
    suspend fun checkAuthStatus(): Result<User, AuthError>
    suspend fun login(identifier: String, password: String): Result<AuthResult,AuthError>
    suspend fun signUpStudent(
        name: String,
        email: String,
        password: String,
        studId: String,
        semester: String,
        divison: String,
        course: String,
        batch: String
    ): Result<User,AuthError>

    suspend fun signupFaculty(
        name: String,
        email: String,
        pass: String,
        facultyId: String,
        dept: String,
        designation: String,
        office: String
    ): Result<User, AuthError>
}
