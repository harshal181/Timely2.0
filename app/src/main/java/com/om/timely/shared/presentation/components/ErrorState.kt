package com.om.timely.shared.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyState(
    title: String = "No Classes Today",
    message: String = "Your schedule is clear.",
    actionText: String = "Refresh",
    onActionClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF9CA3AF)
        )
        Text(
            text = message,
            fontSize = 13.sp,
            color = Color(0xFF6B7280)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = actionText,
            color = Color(0xFF059669),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onActionClick() }
                .padding(8.dp)
        )
    }
}

@Composable
fun ErrorState(
    error: String = "Failed to load schedule",
    onRetry: () -> Unit
) {
    EmptyState(
        title = "Something went wrong",
        message = error,
        actionText = "Try Again",
        onActionClick = onRetry
    )
}
