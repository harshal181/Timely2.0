package com.om.timely.shared.notifications.domain.error

import com.om.timely.core.domain.util.Error
import com.om.timely.core.domain.util.NetworkError
import com.om.timely.faculty.home.domain.error.TimeTableError

sealed interface NotificationError : Error {
    data class Network(val networkError: NetworkError) : NotificationError
    
    enum class Local : NotificationError {
        DISK_FULL,
        UNKNOWN,
        NOTIFICATION_NOT_FOUND
    }
}
