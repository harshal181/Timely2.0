package com.om.timely.shared.notifications.presentation

sealed interface NotificationAction {
    data class OnFilterSelected(val filter: NotificationFilter) : NotificationAction
    data class OnNotificationClicked(val notificationId: Int) : NotificationAction
    object OnRefresh : NotificationAction
    object OnMarkAllAsReadClicked : NotificationAction

    // Faculty Actions
    data class OnToggleSendDialog(val isVisible: Boolean) : NotificationAction
    data class OnTitleChanged(val title: String) : NotificationAction
    data class OnMessageChanged(val message: String) : NotificationAction
    data class OnTypeSelected(val type: com.om.timely.shared.notifications.domain.model.NotificationType) : NotificationAction
    data class OnTargetScopeSelected(val scope: String) : NotificationAction
    object OnSendNotificationClicked : NotificationAction
}
