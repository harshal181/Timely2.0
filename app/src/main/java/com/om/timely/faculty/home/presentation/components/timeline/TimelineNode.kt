package com.om.timely.faculty.home.presentation.components.timeline

import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TimelineNode(
    timeString: String,
    dotColor: Color,
    isOngoing: Boolean = false,
    isLast: Boolean = false,
    extraDragHandle: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 0.dp)
            .drawBehind {
                if (!isLast) {
                    val xPos = 45.dp.toPx() + 16.dp.toPx() + 10.dp.toPx() // Time width + spacer + center of Node
                    val yStart = 18.dp.toPx() + 16.dp.toPx() + 4.dp.toPx() // Dot top padding + dot size + spacing
                    drawLine(
                        color = dotColor.copy(alpha = 0.3f),
                        start = androidx.compose.ui.geometry.Offset(xPos, yStart),
                        end = androidx.compose.ui.geometry.Offset(xPos, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
    ) {
        // Time Column
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(45.dp).padding(top = 16.dp) // Base top padding
        ) {
            val parts = timeString.split(" ")
            if (parts.size == 2) {
                Text(
                    text = parts[0],
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = parts[1],
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = timeString,
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Node & Line Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(20.dp)
        ) {
            val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
            val pulseRadius by infiniteTransition.animateFloat(
                initialValue = 8f,
                targetValue = 14f,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation = androidx.compose.animation.core.tween(1000),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                )
            )
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 0.1f,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation = androidx.compose.animation.core.tween(1000),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                )
            )

            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .padding(top = 18.dp)
                    .size(if (isOngoing) 28.dp else 16.dp)
            ) {
                if (isOngoing) {
                    drawCircle(
                        color = dotColor.copy(alpha = pulseAlpha), 
                        radius = pulseRadius.dp.toPx()
                    )
                }
                drawCircle(color = dotColor.copy(alpha = 0.2f), radius = 8.dp.toPx())
                drawCircle(color = Color.White, radius = 6.dp.toPx())
                drawCircle(color = dotColor, radius = 4.dp.toPx())
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        if (extraDragHandle != null) {
            extraDragHandle()
        }

        // Content
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp)
        ) {
            content()
        }
    }
}
