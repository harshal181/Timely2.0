package com.om.timely.profile.presentation

import com.om.timely.core.presentation.UiText
import com.om.timely.faculty.subjects.domain.model.Subject
import com.om.timely.profile.domain.model.FullProfile

data class ProfileState(
    val isLoading: Boolean = false,
    val profile: FullProfile? = null,
    val error: UiText? = null,
    val editProfileState: EditProfileState = EditProfileState(),
    val changePasswordState: ChangePasswordState = ChangePasswordState(),
    val isEditSheetVisible: Boolean = false,
    val isChangePasswordSheetVisible: Boolean = false,
    val isNotificationsEnabled: Boolean = true, // Mocked for now

    // Subject Mapping Architecture State
    val isAssignSheetVisible: Boolean = false,
    val allAvailableSubjects: List<Subject> = emptyList(),
    val selectedSubjectIds: Set<Int> = emptySet() // Checkbox state buffer
)

data class ChangePasswordState(
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = ""
)
data class EditProfileState(
    val name: String = "",
    val department: String = "",
    val semester: String = "",
    val division: String = "",
    val office: String = "",
    val designation: String = ""  // Added
)
