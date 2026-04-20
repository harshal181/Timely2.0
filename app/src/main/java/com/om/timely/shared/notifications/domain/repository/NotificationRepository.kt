package com.om.timely.shared.notifications.domain.repository

import com.om.timely.core.domain.util.Result
import com.om.timely.shared.notifications.domain.error.NotificationError
import com.om.timely.shared.notifications.domain.model.AppNotification

import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun sendNotification(
        title: String,
        message: String,
        type: String,
        topic: String
    ): Result<Unit,NotificationError>
    fun getNotificationsFlow(): Flow<List<AppNotification>>
    suspend fun fetchNotifications(): Result<Unit, NotificationError>
    suspend fun markAsRead(notificationId: Int): Result<Unit, NotificationError>
    suspend fun markAllAsRead(): Result<Unit, NotificationError>
}
