package com.om.timely.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.timely.R
import com.om.timely.auth.domain.UserRole
import com.om.timely.auth.domain.error.AuthError
import com.om.timely.core.domain.util.NetworkError
import com.om.timely.core.domain.util.Result
import com.om.timely.core.presentation.UiText
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
    private val repository: ProfileRepository
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
                    oldPassword = action.newPass,
                    newPassword = action.confirmPass
                )) }
            }
            ProfileAction.OnSaveProfile -> {
                updateUser()
            }
            ProfileAction.OnUpdatePassword -> {}
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
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            when (val result = repository.getProfile()) {
                is Result.Success -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            profile = result.data,
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

    private fun signOut() {
        viewModelScope.launch {
            repository.logout()
            eventChannel.send(ProfileEvent.SignOutSuccess)
        }
    }
}
