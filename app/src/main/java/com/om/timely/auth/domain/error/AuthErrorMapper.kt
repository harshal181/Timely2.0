package com.om.timely.auth.domain.error

import com.om.timely.R
import com.om.timely.core.presentation.UiText
import com.om.timely.core.presentation.asUiText

fun AuthError.asUiText(): UiText {
    return when(this) {
        is AuthError.Network -> this.networkError.asUiText()
        AuthError.Login.INVALID_CREDENTIALS -> {
            UiText.StringResource(R.string.invalid_credentials)
        }

        AuthError.Register.USER_ALREADY_EXISTS -> {
            UiText.StringResource(R.string.error_email_exists)
        }
    }
}