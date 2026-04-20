package com.om.timely.shared.notifications.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // Format enum to Title Case (e.g., TIMETABLE_UPDATE -> Timetable Update)
    val typeLabel = notification.type.name
        .lowercase()
        .split("_")
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 0.dp,
        color = backgroundColor,
        border = BorderStroke(1.dp, BrandColors.BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Title and (Optional) Mark as Read
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = BrandColors.TextDark,
                    fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                if (!notification.isRead) {
                    IconButton(
                        onClick = onMarkAsRead,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(start = 8.dp) // Spacing from title
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Mark as read",
                            tint = typeConfig.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Message
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = BrandColors.TextMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Row: Type Badge • Time / Context
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // Pill-shaped Type Badge
                Box(
                    modifier = Modifier
                        .background(
                            color = typeConfig.color.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = typeConfig.color,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Dot Separator
                Text(
                    text = "•",
                    color = BrandColors.TextLightGray,
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Time String (or Semester/Metadata if you add it to AppNotification)
                Text(
                    text = getRelativeTimeSpanString(notification.timestamp),
                    style = MaterialTheme.typography.labelMedium,
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