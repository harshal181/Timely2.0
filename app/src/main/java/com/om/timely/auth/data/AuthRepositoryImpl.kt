package com.om.timely.auth.data

import android.util.Log
import com.om.timely.BuildConfig
import com.om.timely.auth.data.dto.AuthResultDto
import com.om.timely.auth.data.dto.CreateUserResultDto
import com.om.timely.auth.data.dto.FacultyCreateDto
import com.om.timely.auth.data.dto.LoginRequestDto
import com.om.timely.auth.data.dto.StudentCreateDto
import com.om.timely.auth.data.dto.UserCreateDto
import com.om.timely.auth.data.dto.UserOutDto
import com.om.timely.auth.data.mappers.toDomain
import com.om.timely.auth.domain.AuthRepository
import com.om.timely.auth.domain.AuthResult
import com.om.timely.auth.domain.User
import com.om.timely.auth.domain.UserRole
import com.om.timely.auth.domain.error.AuthError
import com.om.timely.core.data.safeCall
import com.om.timely.core.domain.AuthPreferences
import com.om.timely.core.domain.util.NetworkError
import com.om.timely.core.domain.util.Result
import com.om.timely.core.domain.util.map
import com.om.timely.core.domain.util.mapError
import com.om.timely.core.domain.util.onSuccess
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthRepositoryImpl(
    private val authPreferences: AuthPreferences,
    private val httpClient: HttpClient
): AuthRepository {

    override suspend fun checkAuthStatus(): Result<User, AuthError> {
        // 1. Get tokens and role from local storage
        val savedToken = authPreferences.getAccessToken()
        val savedRefreshToken = authPreferences.getRefreshToken()
        val savedRole = authPreferences.getUserRole()

        // 2. Fast fail: If token or role is missing, user is not authenticated
        if (savedToken == null || savedRole == null) {
            return Result.Error(AuthError.Network(NetworkError.UNAUTHORIZED))
        }

        // 3. Neatly ping the backend to validate the token
        // We use safeCall to keep the error mapping consistent with your Login/Signup
        val response = safeCall<UserOutDto> {
            httpClient.get("${BuildConfig.BASE_URL}/api/auth/me")
        }

        return response
            .map {
                it.toDomain()
            }
            .mapError { error ->
                // Map NetworkError to your domain AuthError
                error.toAuthError()
            }
    }



    override suspend fun login(
        identifier: String,
        password: String
    ): Result<AuthResult, AuthError> {
        val response = safeCall<AuthResultDto> {
            httpClient.post("${BuildConfig.BASE_URL}/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequestDto(identifier,password))
            }
        }
        Log.d("Response",response.toString())
        return  response
            .map { it.toDomain() }
            .onSuccess { authResult ->
                val accessToken = authResult.accessToken
                val refreshToken = authResult.refreshToken
                val role = authResult.role
                authPreferences.saveTokens(accessToken,refreshToken)
                authPreferences.saveUserRole(role.name)
            }
            .mapError { error ->
                when (error) {
                    NetworkError.UNAUTHORIZED -> AuthError.Login.INVALID_CREDENTIALS
                    else -> error.toAuthError()
                }
            }
    }

    override suspend fun signUpStudent(
        name: String,
        email: String,
        password: String,
        studId: String,
        semester: String,
        divison: String,
        course: String,
        batch: String
    ): Result<User, AuthError> {
        val requestDto = UserCreateDto(
            name = name,
            email = email,
            password = password,
            role = "STUDENT",
            studentDetails = StudentCreateDto(
                studId = studId, semester = semester.toInt(),
                division = divison, batch = batch, course = course
            )
        )

        // 2. Make the safe network call
        val response = safeCall<CreateUserResultDto> {
            httpClient.post("${BuildConfig.BASE_URL}/api/auth/signup") {
                contentType(ContentType.Application.Json)
                setBody(requestDto)
            }
        }
        return response
            .map { it.toDomain() }
            .mapError { error ->
                when (error) {
                    NetworkError.CONFLICT -> AuthError.Register.USER_ALREADY_EXISTS
                    else -> error.toAuthError()
                }
            }
    }

    override suspend fun signupFaculty(
        name: String,
        email: String,
        pass: String,
        facultyId: String,
        designation: String,
        office: String
    ): Result<User, AuthError> {
        val requestDto = UserCreateDto(
            name = name, email = email, password = pass, role = "FACULTY",
            facultyDetails = FacultyCreateDto(
                facId = facultyId, designation = designation, office = office
            )
        )

        val response = safeCall<CreateUserResultDto> {
            httpClient.post("${BuildConfig.BASE_URL}/auth/signup") {
                contentType(ContentType.Application.Json)
                setBody(requestDto)
            }
        }

        return response
            .map { it.toDomain() }
            .mapError { error ->
                when (error) {
                    NetworkError.CONFLICT -> AuthError.Register.USER_ALREADY_EXISTS
                    else -> error.toAuthError()
                }
            }
    }

//    Helper Function
    private fun NetworkError.toAuthError(): AuthError {
        return AuthError.Network(this)
    }
}
