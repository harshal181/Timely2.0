package com.om.timely.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val message: String,
    val type: String,
    val timestamp: Long,
    val senderName: String,
    val isRead: Boolean,
    val isSentByMe: Boolean
)
