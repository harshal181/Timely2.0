package com.om.timely.core.presentation

import com.om.timely.R
import com.om.timely.core.domain.util.NetworkError

fun NetworkError.asUiText(): UiText {
    return when(this) {
        NetworkError.REQUEST_TIMEOUT -> UiText.StringResource(R.string.error_request_timeout)
        NetworkError.TOO_MANY_REQUEST -> UiText.StringResource(R.string.error_too_many_requests)
        NetworkError.NO_INTERNET -> UiText.StringResource(R.string.error_no_internet)
        NetworkError.SERVER_ERROR -> UiText.StringResource(R.string.error_unknown)
        NetworkError.SERIALIZATION_ERROR -> UiText.StringResource(R.string.error_unknown)
        NetworkError.UNKNOWN -> UiText.StringResource(R.string.error_unknown)
        // Note: BAD_REQ, UNAUTHORIZED, and CONFLICT are usually handled
        // by feature-specific mappers, but you can add defaults here.
        else -> UiText.StringResource(R.string.error_unknown)
    }
}