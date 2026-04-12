package com.om.timely.profile.domain.error

import com.om.timely.R
import com.om.timely.auth.domain.error.AuthError
import com.om.timely.core.presentation.UiText
import com.om.timely.core.presentation.asUiText


fun ProfileError.asUiText(): UiText {
    return when(this) {
        ProfileError.PROFILE_NOT_FOUND -> {
            UiText.StringResource(R.string.error_profile_not_found)
        }
        ProfileError.PROFILE_UPDATE_FAILED -> {
            UiText.StringResource(R.string.error_profile_update_failed)
        }

        is ProfileError.Network -> this.networkError.asUiText()
        ProfileError.INCORREC_PASSWORD -> {
            UiText.StringResource(R.string.passwords_do_not_match)
        }
        ProfileError.PASSWORD_ALREDY_USED -> {
            UiText.StringResource(R.string.password_already_used)
        }
    }
}

