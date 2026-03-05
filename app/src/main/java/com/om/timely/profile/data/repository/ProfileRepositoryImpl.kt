package com.om.timely.profile.data.repository

import com.om.timely.BuildConfig
import com.om.timely.core.data.safeCall
import com.om.timely.core.domain.AuthPreferences
import com.om.timely.core.domain.util.NetworkError
import com.om.timely.core.domain.util.Result
import com.om.timely.core.domain.util.map
import com.om.timely.core.domain.util.mapError
import com.om.timely.profile.data.dto.FacultyDetailDto
import com.om.timely.profile.data.dto.FullProfileOutDto
import com.om.timely.profile.data.dto.StudentDetailsDto
import com.om.timely.profile.data.dto.UpdateUserDto
import com.om.timely.profile.data.mappers.toDomain
import com.om.timely.profile.data.mappers.toDto
import com.om.timely.profile.domain.error.ProfileError
import com.om.timely.profile.domain.model.FacultyProfile
import com.om.timely.profile.domain.model.FullProfile
import com.om.timely.profile.domain.model.StudentProfile
import com.om.timely.profile.domain.repository.ProfileRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
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



    override suspend fun changePassword(
        oldPassword: String,
        newPassword: String
    ): Result<Unit, ProfileError> {
        TODO("Not yet implemented")
    }

    override suspend fun subscribeToNotifications(): Result<Unit, ProfileError> {
        TODO("Not yet implemented")
    }

    override suspend fun logout() {
        authPreferences.clearTokens()
    }
}
