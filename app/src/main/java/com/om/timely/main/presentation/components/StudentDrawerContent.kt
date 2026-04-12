package com.om.timely.main.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.R
import com.om.timely.profile.domain.model.FullProfile

@Composable
fun StudentDrawerContent(
    profile: FullProfile?,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    val name = profile?.name ?: "Arjun Mehta"
    val email = profile?.email ?: "student@college.edu"
    val initial = name.firstOrNull()?.toString() ?: "A"

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(Color.White)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .shadow(12.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp), spotColor = Color(0xFF7C3AED).copy(alpha = 0.4f))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF7C3AED), Color(0xFF6366F1))
                    ),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .padding(24.dp)
        ) {
            // Close Button
            IconButton(
                onClick = onCloseClick,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White.copy(alpha = 0.8f)
                )
            }

            Column(
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                // Avatar
                Surface(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape),
                    color = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initial,
                            color = Color(0xFF7C3AED),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Name & Email
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = email,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Menu Items
        DrawerMenuItem(
            label = "Home",
            icon = ImageVector.vectorResource(R.drawable.home_24),
            onClick = onHomeClick,
            isSelected = true // Mocked for design
        )
        DrawerMenuItem(
            label = "Profile",
            icon = ImageVector.vectorResource(R.drawable.user_24),
            onClick = onProfileClick
        )

        Spacer(modifier = Modifier.weight(1f))

        // 3. Logout Button at the bottom
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onLogoutClick() },
            color = Color(0xFFFEF2F2), // Light red
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = Color(0xFFEF4444)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Logout",
                    color = Color(0xFFEF4444),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isSelected: Boolean = false
) {
    Surface(
        color = if (isSelected) Color(0xFFF5F3FF) else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF7C3AED) else Color(0xFF6B7280),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                color = if (isSelected) Color(0xFF7C3AED) else Color(0xFF1F2937),
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
