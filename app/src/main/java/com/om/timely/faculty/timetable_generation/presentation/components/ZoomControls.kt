package com.om.timely.faculty.timetable_generation.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ZoomControls(
    currentScale: Float,
    onScaleChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp)), // Light gray border
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Zoom Out Button
        IconButton(
            onClick = { onScaleChange((currentScale - 0.1f).coerceAtLeast(0.5f)) }
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.DarkGray)
        }

        // Percentage Text
        Text(
            text = "${(currentScale * 100).toInt()}%",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // Zoom In Button
        IconButton(
            onClick = { onScaleChange((currentScale + 0.1f).coerceAtMost(2.5f)) }
        ) {
            Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.DarkGray)
        }
    }
}