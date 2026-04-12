package com.om.timely.shared.notifications.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle // Added for the read button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton // Added for the read button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.om.timely.shared.notifications.domain.model.AppNotification
import com.om.timely.shared.notifications.domain.model.NotificationType
import com.om.timely.ui.theme.BrandColors

@Composable
fun NotificationCard(
    notification: AppNotification,
    onMarkAsRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typeConfig = getNotificationTypeConfig(notification.type)

    // Change background slightly if it's unread to make it stand out more
    val backgroundColor = if (!notification.isRead) BrandColors.BgWhite else BrandColors.BgOffWhite

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 0.dp,
        color = backgroundColor,
        border = BorderStroke(1.dp, BrandColors.BorderLight)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon Container (Your existing implementation is perfect here)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(typeConfig.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = typeConfig.icon,
                    contentDescription = null,
                    tint = typeConfig.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = BrandColors.TextDark,
                        fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )

                    // NEW: Individual Mark as Read Button
                    if (!notification.isRead) {
                        IconButton(
                            onClick = onMarkAsRead,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "Mark as read",
                                tint = typeConfig.color // Match the theme of the notification type
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandColors.TextMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = getRelativeTimeSpanString(notification.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = BrandColors.TextLightGray
                )
            }
        }
    }
}

data class NotificationUIConfig(val icon: ImageVector, val color: Color)

@Composable
fun getNotificationTypeConfig(type: NotificationType): NotificationUIConfig {
    return when(type) {
        NotificationType.TIMETABLE_UPDATE -> NotificationUIConfig(Icons.Default.DateRange, BrandColors.PrimaryPurple)
        NotificationType.CLASS_CANCELLED -> NotificationUIConfig(Icons.Default.Close, BrandColors.Error)
        NotificationType.FACULTY_CHANGE -> NotificationUIConfig(Icons.Default.Person, BrandColors.Info) // Blue
        NotificationType.EXTRA_CLASS -> NotificationUIConfig(Icons.Default.AddCircle, BrandColors.ExtraClass) // Green
        NotificationType.GENERAL_NOTICE -> NotificationUIConfig(Icons.Default.Info, BrandColors.GeneralNotice) // Orange
        NotificationType.HOLIDAY_NOTICE -> NotificationUIConfig(Icons.Default.Warning, BrandColors.Warning) // Amber
        NotificationType.PUSH -> NotificationUIConfig(Icons.Default.Info, BrandColors.GeneralNotice)
    }
}

fun getRelativeTimeSpanString(timeMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timeMillis
    
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 1 -> "$days days ago"
        days == 1L -> "1 day ago"
        hours > 1 -> "$hours hours ago"
        hours == 1L -> "1 hour ago"
        minutes > 1 -> "$minutes mins ago"
        else -> "Just now"
    }
}
