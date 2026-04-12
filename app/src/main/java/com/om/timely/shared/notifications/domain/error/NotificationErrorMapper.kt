package com.om.timely.shared.notifications.domain.error

import com.om.timely.R
import com.om.timely.core.domain.util.NetworkError
import com.om.timely.core.presentation.UiText
import com.om.timely.core.presentation.asUiText

fun NotificationError.asUiText(): UiText {
    return when (this) {
        NotificationError.Local.DISK_FULL -> UiText.StringResource(R.string.disk_full)
        NotificationError.Local.UNKNOWN -> UiText.StringResource(R.string.unknown_error)
        NotificationError.Local.NOTIFICATION_NOT_FOUND -> UiText.StringResource(R.string.notification_not_found)
        is NotificationError.Network -> this.networkError.asUiText()
    }
}

//fun NetworkError.toNotificationError(): NotificationError.Network {
//    return when(this) {
//        NetworkError.REQUEST_TIMEOUT -> NotificationError.Network.REQUEST_TIMEOUT
//        NetworkError.UNAUTHORIZED -> NotificationError.Network.UNAUTHORIZED
//        NetworkError.CONFLICT -> NotificationError.Network.CONFLICT
//        NetworkError.NO_INTERNET -> NotificationError.Network.NO_INTERNET
//        NetworkError.SERVER_ERROR -> NotificationError.Network.SERVER_ERROR
//        NetworkError.UNKNOWN -> NotificationError.Network.UNKNOWN
//        else -> NotificationError.Network.UNKNOWN
//    }
//}
