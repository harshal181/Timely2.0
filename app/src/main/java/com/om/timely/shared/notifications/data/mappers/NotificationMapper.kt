package com.om.timely.shared.notifications.data.mappers

import com.om.timely.core.data.local.entity.NotificationEntity
import com.om.timely.shared.notifications.data.dto.NotificationDto
import com.om.timely.shared.notifications.domain.model.AppNotification
import com.om.timely.shared.notifications.domain.model.NotificationType

fun NotificationDto.toEntity(): NotificationEntity {
    return NotificationEntity(
        id = id,
        title = title,
        message = message,
        type = type,
        timestamp = timestamp,
        senderName = senderName,
        isRead = isRead,
        isSentByMe = isSentByMe
    )
}

fun NotificationEntity.toDomain(): AppNotification {
    return AppNotification(
        id = id,
        title = title,
        message = message,
        type = try { NotificationType.valueOf(type) } catch (e: Exception) { NotificationType.GENERAL_NOTICE },
        timestamp = timestamp,
        senderName = senderName,
        isRead = isRead,
        isSentByMe = isSentByMe
    )
}
