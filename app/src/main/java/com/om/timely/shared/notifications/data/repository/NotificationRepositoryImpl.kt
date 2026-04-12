package com.om.timely.shared.notifications.data.repository

import com.om.timely.BuildConfig
import com.om.timely.core.data.local.dao.NotificationDao
import com.om.timely.core.data.safeCall
import com.om.timely.core.domain.util.NetworkError
import com.om.timely.core.domain.util.Result
import com.om.timely.core.domain.util.map
import com.om.timely.core.domain.util.mapError
import com.om.timely.shared.notifications.data.dto.NotificationDto
import com.om.timely.shared.notifications.data.mappers.toDomain
import com.om.timely.shared.notifications.data.mappers.toEntity
import com.om.timely.shared.notifications.domain.error.NotificationError
import com.om.timely.shared.notifications.domain.model.AppNotification
import com.om.timely.shared.notifications.domain.repository.NotificationRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.patch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationRepositoryImpl(
    private val httpClient: HttpClient,
    private val notificationDao: NotificationDao
): NotificationRepository {

    override fun getNotificationsFlow(): Flow<List<AppNotification>> {
        return notificationDao.getNotificationsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun fetchNotifications(): Result<Unit, NotificationError> {
        return safeCall<List<NotificationDto>> {
            httpClient.get("${BuildConfig.BASE_URL}/api/notifications/me")
        }.map { dtoList ->
            val entities = dtoList.map { it.toEntity() }
            // The user wanted to just replace or insert into the DB
            notificationDao.insertNotifications(entities)
            Unit
        }.mapError { error ->
            when (error) {
                NetworkError.NOT_FOUND -> NotificationError.Local.NOTIFICATION_NOT_FOUND
                else -> NotificationError.Network(error)
            }
        }
    }

    override suspend fun markAsRead(notificationId: Int): Result<Unit, NotificationError> {
        return safeCall<Unit> {
            httpClient.patch("${BuildConfig.BASE_URL}/api/notifications/${notificationId}/read")
        }.map {
            notificationDao.markAsRead(notificationId)
            Unit
        }.mapError { error ->
            NotificationError.Network(error)
        }
    }

    override suspend fun markAllAsRead(): Result<Unit, NotificationError> {
        notificationDao.markAllAsRead()
        return Result.Success(Unit)
    }
}
