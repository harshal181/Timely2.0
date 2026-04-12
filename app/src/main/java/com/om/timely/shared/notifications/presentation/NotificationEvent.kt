package com.om.timely.shared.notifications.presentation

import com.om.timely.core.presentation.UiText

sealed interface NotificationEvent {
    data class ShowError(val error: UiText) : NotificationEvent
    data class ShowSnackbar(val message: UiText) : NotificationEvent
}
