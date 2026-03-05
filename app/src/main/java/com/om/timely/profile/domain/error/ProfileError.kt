package com.om.timely.profile.domain.error

import com.om.timely.auth.domain.error.AuthError
import com.om.timely.core.domain.util.Error
import com.om.timely.core.domain.util.NetworkError

sealed interface ProfileError:  Error {
    object PROFILE_NOT_FOUND: ProfileError
    object PROFILE_UPDATE_FAILED: ProfileError

    data class Network(val networkError: NetworkError) : ProfileError
}