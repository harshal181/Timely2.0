package com.om.timely.auth.presentation.login

import com.om.timely.auth.domain.UserRole
import com.om.timely.core.presentation.UiText

data class LoginState(
    val emailOrId: String = "",
    val isEmailOrIdValid: Boolean = true,
    
    val password: String = "",
    val isPasswordValid: Boolean = true,
    val isPasswordVisible: Boolean = false,
    
    val isLoading: Boolean = false,
    val error: UiText? = null
)
