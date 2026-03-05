package com.om.timely.auth.presentation.signup

import com.om.timely.core.domain.util.Event
import com.om.timely.core.presentation.UiText

sealed interface SignupEvent : Event {
    data class SignupSuccess(val route: String) : SignupEvent
    data class SignupError(val error: UiText) : SignupEvent
}
