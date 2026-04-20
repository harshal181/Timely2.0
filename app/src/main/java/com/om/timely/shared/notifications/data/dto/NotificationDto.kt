package com.om.timely.shared.notifications.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: Int,
    val title: String,
    val message: String,
    val type: String,
    val timestamp: Long,
    val senderName: String,
    val isRead: Boolean,
    val isSentByMe: Boolean
)

@Serializable
data class NotificationSendRequestDto(
    val title: String,
    val message: String,
    val msgType: String,
    val topic: String
)
