package com.om.timely.auth.presentation.login

data class LoginState(
    val emailOrId: String = "",
    val isEmailOrIdValid: Boolean = true,

    val password: String = "",
    val isPasswordValid: Boolean = true,
    val isPasswordVisible: Boolean = false,

    val isLoading: Boolean = false
)
