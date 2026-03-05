package com.om.timely.profile.presentation

import com.om.timely.core.presentation.UiText

sealed interface ProfileEvent {
    object SignOutSuccess : ProfileEvent
    object ProfileUpdateSuccess: ProfileEvent
    object PasswordChangeSuccess: ProfileEvent
    data class ShowError(val error: UiText) : ProfileEvent
}
