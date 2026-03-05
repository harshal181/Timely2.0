package com.om.timely.auth.presentation.login

import com.om.timely.core.domain.util.Action

sealed interface LoginAction : Action {
    data class OnEmailOrIdChanged(val value: String) : LoginAction
    data class OnPasswordChanged(val value: String) : LoginAction
    data object OnTogglePasswordVisibility : LoginAction
    data object OnLoginClick : LoginAction
    data object OnSignupClick : LoginAction
}
