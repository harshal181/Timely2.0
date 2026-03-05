package com.om.timely.auth.domain.error

import com.om.timely.core.domain.util.Error
import com.om.timely.core.domain.util.NetworkError

sealed interface AuthError: Error {

    enum class Register: AuthError {
        USER_ALREADY_EXISTS
    }

    enum class Login: AuthError {
        INVALID_CREDENTIALS
    }

    data class Network(val networkError: NetworkError) : AuthError
}