package com.om.timely.profile.presentation

sealed interface ProfileAction {
    object OnSignOutClick : ProfileAction
    data class OnToggleNotifications(val enabled: Boolean) : ProfileAction
    data object OnChangePasswordClick : ProfileAction
    data object OnUpdatePassword : ProfileAction
    data object OnEditProfileClick : ProfileAction
    data object OnSaveProfile : ProfileAction
    data class OnEditProfileDataChange(val editState: EditProfileState) : ProfileAction
    data class OnPasswordDataChange(val oldPas: String, val newPass: String, val confirmPass: String) : ProfileAction

    data class ToggleEditSheet(val isOpen: Boolean) : ProfileAction
    data class TogglePasswordSheet(val isOpen: Boolean) : ProfileAction
    
    // Subject Mapping Action handlers
    object OnManageSubjectsClicked : ProfileAction
    object OnDismissManageSubjectsSheet : ProfileAction
    data class OnSubjectSelectionToggled(val subjectId: Int) : ProfileAction
    object OnSaveSubjectAssignments : ProfileAction
}
