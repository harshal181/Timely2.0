package com.om.timely.shared.notifications.domain.model

data class AppNotification(
    val id: Int,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long,
    val senderName: String,
    val isRead: Boolean,
    val isSentByMe: Boolean = false
)
