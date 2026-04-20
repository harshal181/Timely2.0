package com.om.timely.shared.notifications.data.repository

import com.om.timely.BuildConfig
import com.om.timely.core.data.local.dao.NotificationDao
import com.om.timely.core.data.safeCall
import com.om.timely.core.domain.util.NetworkError
import com.om.timely.core.domain.util.Result
import com.om.timely.core.domain.util.map
import com.om.timely.core.domain.util.mapError
import com.om.timely.shared.notifications.data.dto.NotificationDto
import com.om.timely.shared.notifications.data.dto.NotificationSendRequestDto
import com.om.timely.shared.notifications.data.mappers.toDomain
import com.om.timely.shared.notifications.data.mappers.toEntity
import com.om.timely.shared.notifications.domain.error.NotificationError
import com.om.timely.shared.notifications.domain.model.AppNotification
import com.om.timely.shared.notifications.domain.repository.NotificationRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationRepositoryImpl(
    private val httpClient: HttpClient,
    private val notificationDao: NotificationDao
): NotificationRepository {

    override suspend fun sendNotification(
        title: String,
        message: String,
        type: String,
        topic: String
    ): Result<Unit, NotificationError> {
        val requestPayload = NotificationSendRequestDto(
            title = title,
            message = message,
            msgType = type,
            topic = topic
        )
        return safeCall<Unit> {
            httpClient.post("${BuildConfig.BASE_URL}/api/notifications/send") {
                contentType(ContentType.Application.Json)
                setBody(requestPayload)
            }
        }.map {
            Unit
        }.mapError { error ->
            when (error) {
                NetworkError.NOT_FOUND -> NotificationError.Local.NOTIFICATION_NOT_FOUND
                else -> NotificationError.Network(error)
            }
        }
    }


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
            notificationDao.clearNotifications()
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
        notificationDao.markAsRead(notificationId)
        return safeCall<Unit> {
            httpClient.patch("${BuildConfig.BASE_URL}/api/notifications/${notificationId}/read")
        }.map {
            Unit
        }.mapError { error ->
            NotificationError.Network(error)
        }
    }

    override suspend fun markAllAsRead(): Result<Unit, NotificationError> {
        notificationDao.markAllAsRead()
        return safeCall<Unit> {
            httpClient.patch("${BuildConfig.BASE_URL}/api/notifications/read-all")
        }.map {
            Unit
        }.mapError { error ->
            NotificationError.Network(error)
        }
    }
}
