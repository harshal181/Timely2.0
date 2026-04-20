package com.om.timely.profile.data.repository

import com.om.timely.BuildConfig
import com.om.timely.core.data.safeCall
import com.om.timely.core.domain.AuthPreferences
import com.om.timely.core.domain.util.NetworkError
import com.om.timely.core.domain.util.Result
import com.om.timely.core.domain.util.map
import com.om.timely.core.domain.util.mapError
import com.om.timely.profile.data.dto.ChangePasswordRequest
import com.om.timely.profile.data.dto.FacultyDetailDto
import com.om.timely.profile.data.dto.FullProfileOutDto
import com.om.timely.profile.data.dto.StudentDetailsDto
import com.om.timely.profile.data.dto.UpdateUserDto
import com.om.timely.profile.data.mappers.toDomain
import com.om.timely.profile.domain.error.ProfileError
import com.om.timely.profile.domain.model.FullProfile
import com.om.timely.profile.domain.repository.ProfileRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ProfileRepositoryImpl(
    private val httpClient: HttpClient,
    private val authPreferences: AuthPreferences
): ProfileRepository {

    override suspend fun getProfile(): Result<FullProfile, ProfileError> {
        val response = safeCall<FullProfileOutDto> {
            httpClient.get("${BuildConfig.BASE_URL}/api/users/profile")
        }
        return response.map { it.toDomain() }
            .mapError { error ->
                when (error) {
                    NetworkError.NOT_FOUND -> ProfileError.PROFILE_NOT_FOUND
                    else -> ProfileError.Network(error)
                }
            }
    }

    override suspend fun getAllFaculties(): Result<List<FullProfile>, ProfileError> {
        val response = safeCall<List<FullProfileOutDto>> {
            httpClient.get("${BuildConfig.BASE_URL}/api/users/faculties")
        }
        println("NETWORK_TRACE: Faculties fetch result -> $response")
        return response.map { list -> list.map { it.toDomain() } }
            .mapError { error ->
                when (error) {
                    NetworkError.NOT_FOUND -> ProfileError.PROFILE_NOT_FOUND
                    else -> ProfileError.Network(error)
                }
            }
    }

    override suspend fun updateStudent(
        name: String,
        semester: Int,
        division: String,
        batch: String
    ): Result<FullProfile, ProfileError> {
        val response = safeCall<FullProfileOutDto> {
            httpClient.put("${BuildConfig.BASE_URL}/api/users/profile") {
                contentType(ContentType.Application.Json)
                setBody(
                    UpdateUserDto(
                        name = name,
                        studentDetails = StudentDetailsDto(
                            semester = semester,
                            division = division,
                            batch = batch
                        )
                    )
                )
            }
        }
        return response.map { it.toDomain() }
            .mapError { error ->
                when (error) {
                    NetworkError.NOT_FOUND -> ProfileError.PROFILE_UPDATE_FAILED
                    else -> ProfileError.Network(error)
                }
            }
    }

    override suspend fun updateFaculty(
        name: String,
        designation: String,
        office: String
    ): Result<FullProfile, ProfileError> {
        val response = safeCall<FullProfileOutDto> {
            httpClient.put("${BuildConfig.BASE_URL}/api/users/profile") {
                contentType(ContentType.Application.Json)
                setBody(UpdateUserDto(
                    name = name,
                    facultyDetails = FacultyDetailDto(
                        designation = designation,
                        office = office
                    )
                ))
            }
        }
        return response.map { it.toDomain() }
            .mapError { error ->
                when (error) {
                    NetworkError.NOT_FOUND -> ProfileError.PROFILE_UPDATE_FAILED
                    else -> ProfileError.Network(error)
                }
            }
    }



    override suspend fun assignSubject(subjectId: Int): Result<Unit, ProfileError> {
        val response = safeCall<Unit> {
            httpClient.post("${BuildConfig.BASE_URL}/api/subjects/$subjectId/assign")
        }
        return response.mapError { error ->
            when (error) {
                NetworkError.CONFLICT -> ProfileError.PROFILE_UPDATE_FAILED
                NetworkError.NOT_FOUND -> ProfileError.PROFILE_UPDATE_FAILED
                else -> ProfileError.Network(error)
            }
        }
    }

    override suspend fun unassignSubject(subjectId: Int): Result<Unit, ProfileError> {
        val response = safeCall<Unit> {
            httpClient.delete("${BuildConfig.BASE_URL}/api/subjects/$subjectId/remove")
        }
        return response.mapError { error ->
            when (error) {
                NetworkError.NOT_FOUND -> ProfileError.PROFILE_UPDATE_FAILED
                else -> ProfileError.Network(error)
            }
        }
    }

    override suspend fun changePassword(
        oldPassword: String,
        newPassword: String
    ): Result<Unit, ProfileError> {
        val response = safeCall<Unit> {
            httpClient.put("${BuildConfig.BASE_URL}/api/users/password") {
                contentType(ContentType.Application.Json)
                setBody(ChangePasswordRequest(oldPassword, newPassword))
            }
        }

        return response.map {
            // Password changed successfully
            Unit
        }.mapError { error ->
            when (error) {
                NetworkError.BAD_REQUEST -> ProfileError.INCORREC_PASSWORD
                NetworkError.CONFLICT -> ProfileError.PASSWORD_ALREDY_USED
                else -> ProfileError.Network(error)
            }
        }
    }

    override suspend fun subscribeToNotifications(): Result<Unit, ProfileError> {
        TODO("Not yet implemented")
    }

    override suspend fun logout() {
        // 1. Wipe all persisted tokens (access, refresh, role)
        authPreferences.clearTokens()

        // 2. Invalidate Ktor's in-memory bearer token cache.
        //    Without this, Ktor reuses the cached BearerTokens object and keeps
        //    sending the old JWT on the next checkAuthStatus request, making the
        //    app appear still logged in until the process is killed.
        try {
            httpClient.authProvider<BearerAuthProvider>()?.clearToken()
        } catch (_: Exception) {
            // Plugin might not be installed in test environments — safe to ignore
        }
    }
}
