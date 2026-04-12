package com.om.timely.student.home.presentation

import com.om.timely.core.presentation.UiText

sealed interface StudentHomeEvent {
    data class ShowError(val error: UiText) : StudentHomeEvent
    data object NavigateToNotifications : StudentHomeEvent
}
