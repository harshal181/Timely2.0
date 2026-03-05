package com.om.timely.profile.domain.repository

import com.om.timely.core.domain.util.NetworkError
import com.om.timely.core.domain.util.Result
import com.om.timely.profile.domain.error.ProfileError
import com.om.timely.profile.domain.model.FacultyProfile
import com.om.timely.profile.domain.model.FullProfile
import com.om.timely.profile.domain.model.StudentProfile

interface ProfileRepository {
    suspend fun getProfile(): Result<FullProfile, ProfileError>
    suspend fun updateStudent(
        name: String,
        semester: Int,
        division: String,
        batch: String
    ): Result<FullProfile, ProfileError>
    suspend fun updateFaculty(
        name: String,
        designation: String,
        office: String
    ): Result<FullProfile,ProfileError>
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit,ProfileError>
    suspend fun subscribeToNotifications(): Result<Unit, ProfileError>
    suspend fun logout()
}
