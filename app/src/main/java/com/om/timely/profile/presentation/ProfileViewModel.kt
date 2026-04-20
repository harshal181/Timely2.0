package com.om.timely.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.timely.R
import com.om.timely.auth.domain.UserRole
import com.om.timely.auth.domain.error.AuthError
import com.om.timely.core.domain.util.NetworkError
import com.om.timely.core.domain.util.Result
import com.om.timely.core.presentation.UiText
import com.om.timely.core.service.FcmTopicManager
import com.om.timely.profile.data.dto.ChangePasswordRequest
import com.om.timely.faculty.subjects.domain.repository.SubjectRepository
import com.om.timely.profile.domain.error.asUiText
import com.om.timely.profile.domain.model.FullProfile
import com.om.timely.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<ProfileEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        loadProfile()
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.OnSignOutClick -> signOut()
            is ProfileAction.OnToggleNotifications -> {
                _state.update { it.copy(isNotificationsEnabled = action.enabled) }
            }

            ProfileAction.OnChangePasswordClick -> {
                onAction(ProfileAction.TogglePasswordSheet(true))
            }
            ProfileAction.OnEditProfileClick -> {
                onAction(ProfileAction.ToggleEditSheet(true))
            }
            is ProfileAction.OnEditProfileDataChange -> {
                _state.update { it.copy(editProfileState = action.editState) }
            }
            is ProfileAction.OnPasswordDataChange -> {
                _state.update { it.copy(changePasswordState = it.changePasswordState.copy(
                    oldPassword = action.oldPas,
                    newPassword = action.newPass,
                    confirmPassword = action.confirmPass
                )) }
            }
            ProfileAction.OnSaveProfile -> {
                updateUser()
            }
            ProfileAction.OnUpdatePassword -> {
                changePassword()
            }
            is ProfileAction.ToggleEditSheet -> {
                _state.update { it.copy(isEditSheetVisible = action.isOpen,
                    editProfileState = if (action.isOpen) {
                        EditProfileState(
                            name = it.profile?.name ?: "",
                            department = it.profile?.studentDetails?.course ?: "",
                            semester = it.profile?.studentDetails?.semester?.toString() ?: "",
                            division = it.profile?.studentDetails?.division ?: ""
                        )
                    } else it.editProfileState)
                }
            }
            is ProfileAction.TogglePasswordSheet -> {
                _state.update { it.copy(isChangePasswordSheetVisible = action.isOpen) }
            }
            
            // Subject Mapping Handlers
            ProfileAction.OnManageSubjectsClicked -> {
                _state.update { it.copy(isAssignSheetVisible = true) }
            }
            ProfileAction.OnDismissManageSubjectsSheet -> {
                val assignedIds = _state.value.profile?.facultyDetails?.subjects?.map { it.id }?.toSet() ?: emptySet()
                _state.update { it.copy(isAssignSheetVisible = false, selectedSubjectIds = assignedIds) }
            }
            is ProfileAction.OnSubjectSelectionToggled -> {
                _state.update { currentState ->
                    val currentSelection = currentState.selectedSubjectIds.toMutableSet()
                    if (currentSelection.contains(action.subjectId)) {
                        currentSelection.remove(action.subjectId)
                    } else {
                        currentSelection.add(action.subjectId)
                    }
                    currentState.copy(selectedSubjectIds = currentSelection)
                }
            }
            ProfileAction.OnSaveSubjectAssignments -> {
                saveSubjectAssignments()
            }

        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            when (val result = repository.getProfile()) {
                is Result.Success -> {
                    val assignedIds = result.data.facultyDetails?.subjects?.map { it.id }?.toSet() ?: emptySet()
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            profile = result.data,
                            selectedSubjectIds = assignedIds,
                            error = null
                        ) 
                    }
                }
                is Result.Error -> {
                    val errorMessage = result.error.asUiText()
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                    eventChannel.send(ProfileEvent.ShowError(errorMessage))
                }
            }

            // Background observe all available subjects globally
            viewModelScope.launch {
                subjectRepository.getAllSubjects().collect { subjectsList ->
                    _state.update { it.copy(allAvailableSubjects = subjectsList) }
                }
            }
            
            // Trigger network sync for subjects into DB
            viewModelScope.launch {
                subjectRepository.fetchAllSubjects()
            }
        }
    }
    private fun updateUser() {
        viewModelScope.launch {
            val currentState = _state.value
            val editData = currentState.editProfileState
            val userRole = currentState.profile?.role

            // Start loading and clear previous errors
            _state.update { it.copy(isLoading = true, error = null) }

            val result = when (userRole) {
                UserRole.STUDENT -> {
                    repository.updateStudent(
                        name = editData.name,
                        semester = editData.semester.toIntOrNull() ?: 0,
                        division = editData.division,
                        batch = editData.department // UI 'Department' maps to backend 'batch'
                    )
                }
                UserRole.FACULTY -> {
                    repository.updateFaculty(
                        name = editData.name,
                        designation = editData.designation,
                        office = editData.office
                    )
                }
                else -> {
                    // Handle cases where role might be null or unknown
                    _state.update { it.copy(isLoading = false) }
                    return@launch
                }
            }

            // Handle the result from the repository
            when (result) {
                is Result.Success -> {
                    _state.update { it.copy(
                        isLoading = false,
                        profile = result.data,
                        isEditSheetVisible = false // Close the sheet on success
                    ) }
                }
                is Result.Error -> {
                    val errorMessage = result.error.asUiText()
                    _state.update { it.copy(
                        isLoading = false,
                        error = errorMessage // Helper to convert error to displayable text
                    ) }
                    eventChannel.send(ProfileEvent.ShowError(errorMessage))
                }
            }
        }
    }

    private fun changePassword() {
        val passwordState = _state.value.changePasswordState

        // 1. Validation logic
        val validationError = validatePasswords(
            passwordState.newPassword,
            passwordState.confirmPassword
        )

        if (validationError != null) {
            viewModelScope.launch { eventChannel.send(ProfileEvent.ShowError(validationError)) }
            return
        }

        // 2. Network Request
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = repository.changePassword(
                oldPassword = passwordState.oldPassword,
                newPassword = passwordState.newPassword
            )

            _state.update { it.copy(isLoading = false) }

            when (result) {
                is Result.Success -> {
                    _state.update { it.copy(isChangePasswordSheetVisible = false, changePasswordState = ChangePasswordState()) }
                    eventChannel.send(ProfileEvent.PasswordChangeSuccess)
                }
                is Result.Error -> {
                    val errorMessage = result.error.asUiText()
                    _state.update { it.copy(
                        isLoading = false,
                        error = errorMessage // Helper to convert error to displayable text
                    ) }
                    eventChannel.send(ProfileEvent.ShowError(errorMessage))
                }
            }
        }
    }

    private fun validatePasswords(new: String, confirm: String): UiText? {
        return when {
            new != confirm -> UiText.StringResource(R.string.passwords_do_not_match)
            new.length < 6 -> UiText.StringResource(R.string.password_too_short)
            else -> null
        }
    }



    private fun signOut() {
        viewModelScope.launch {
            // Unsubscribe from FCM topics before clearing tokens.
            // Profile is already in state — no extra network call needed.
            val profile = _state.value.profile
            if (profile != null) {
                FcmTopicManager.unsubscribeForProfile(profile)
            }

            repository.logout()
            eventChannel.send(ProfileEvent.SignOutSuccess)
        }
    }

    private fun saveSubjectAssignments() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isAssignSheetVisible = false) }
            
            val originalAssignedIds = _state.value.profile?.facultyDetails?.subjects?.map { it.id }?.toSet() ?: emptySet()
            val newSelectedIds = _state.value.selectedSubjectIds
            
            // Calculate Diffs
            val idsToAssign = newSelectedIds - originalAssignedIds
            val idsToUnassign = originalAssignedIds - newSelectedIds
            
            // Execute operations sequentially
            var hasError = false
            for (id in idsToAssign) {
                if (repository.assignSubject(id) is Result.Error) hasError = true
            }
            for (id in idsToUnassign) {
                if (repository.unassignSubject(id) is Result.Error) hasError = true
            }
            
            if (hasError) {
                eventChannel.send(ProfileEvent.ShowError(UiText.DynamicString("Some assignments failed. Refetching latest states.")))
            }
            
            loadProfile() 
        }
    }
}
