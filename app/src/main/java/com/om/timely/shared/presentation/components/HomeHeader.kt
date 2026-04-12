package com.om.timely.shared.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.profile.domain.model.FullProfile

@Composable
fun HomeHeader(
    profile: FullProfile?,
    greeting: String,
    onNotificationClick: () -> Unit
) {
    val name = profile?.name?.split(" ")?.take(2)?.joinToString(" ") ?: "User"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "$greeting,",
                color = Color(0xFF9CA3AF),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$name",
                color = Color(0xFF1F2937),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
        }

    }
}
