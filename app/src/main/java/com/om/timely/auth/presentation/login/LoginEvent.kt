package com.om.timely.auth.presentation.login

import com.om.timely.core.domain.util.Event
import com.om.timely.core.presentation.UiText

sealed interface LoginEvent : Event {
    data class LoginSuccess(val route: String) : LoginEvent
    data class LoginError(val error: UiText) : LoginEvent
    data object NavigateToSignup : LoginEvent
}

