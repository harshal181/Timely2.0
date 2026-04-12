package com.om.timely.student.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.R
import com.om.timely.profile.domain.model.FullProfile

@Composable
fun StudentHomeHeader(
    profile: FullProfile?,
    greeting: String,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    hasNotifications: Boolean = true // Mocked for design
) {
    val name = profile?.name?.split(" ")?.firstOrNull() ?: "Arjun"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Menu Toggle
        Surface(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(15.dp))
                .clickable { onMenuClick() },
            color = Color.White,
            shadowElevation = 2.dp,
            tonalElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color(0xFF1F2937),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Center: Greeting
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$greeting,",
                color = Color(0xFF9CA3AF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    color = Color(0xFF1F2937),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                // Using a text emoji for the hand waved!
                Text("👋", fontSize = 20.sp)
            }
        }

        // Right: Search and Notification
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box {
                HeaderIcon(
                    icon = ImageVector.vectorResource(R.drawable.bell_24),
                    onClick = onNotificationClick
                )
                if (hasNotifications) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.Red, CircleShape)
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderIcon(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        color = Color.White,
        shadowElevation = 2.dp,
        tonalElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF1F2937),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
