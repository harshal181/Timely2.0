package com.om.timely.faculty.timetable_generation.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.faculty.timetable_generation.presentation.TimetableGenerationAction
import com.om.timely.faculty.timetable_generation.presentation.TimetableGenerationState
import com.om.timely.ui.theme.BrandColors

private data class StageInfo(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val tint: Color
)

private fun stageInfo(stage: String): StageInfo = when (stage) {
    "queued"    -> StageInfo(Icons.Default.AccessTime, "Job Queued", "Your request is in the queue and will start soon.", Color(0xFFF59E0B))
    "solving"   -> StageInfo(Icons.Default.Pending, "Solving Constraints", "The CP solver is computing the optimal schedule…", Color(0xFF3B82F6))
    "completed" -> StageInfo(Icons.Default.CheckCircle, "Completed!", "Timetable is ready. Loading your results…", Color(0xFF10B981))
    "failed"    -> StageInfo(Icons.Outlined.Notifications, "Generation Failed", "Something went wrong. Please try again.", Color(0xFFEF4444))
    else        -> StageInfo(Icons.Default.AccessTime, "Starting…", "Preparing the generation job.", Color(0xFF6B7280))
}

@Composable
fun ProcessingScreen(
    state: TimetableGenerationState,
    onAction: (TimetableGenerationAction) -> Unit
) {
    val info = stageInfo(state.jobStage)
    val isTerminal = state.jobStage == "completed" || state.jobStage == "failed"

    // Pulse animation for the spinner ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Animated icon pill
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(if (!isTerminal) pulseScale else 1f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(info.tint.copy(alpha = 0.18f), info.tint.copy(alpha = 0.06f))
                    )
                )
                .border(2.dp, info.tint.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = info.icon,
                contentDescription = null,
                tint = info.tint,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = info.title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = info.subtitle,
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Job ID chip
        state.activeJobId?.let { jobId ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFEDE9FE),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Job: ${jobId.take(16)}…",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF5B21B6),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        // Indeterminate loading bar while job is in-progress
        if (!isTerminal) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = BrandColors.PrimaryPurple,
                trackColor = Color(0xFFE5E7EB)
            )
            Spacer(modifier = Modifier.height(40.dp))
        } else {
            Spacer(modifier = Modifier.height(40.dp))
        }

        // Notification hint
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
            elevation = CardDefaults.cardElevation(0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "You'll receive a notification when complete — you can leave this screen.",
                    fontSize = 12.sp,
                    color = Color(0xFF065F46),
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Run in Background / Cancel action
        OutlinedButton(
            onClick = { onAction(TimetableGenerationAction.StopPollingAndLeave) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B7280))
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Go back — wait for notification", fontWeight = FontWeight.Medium)
        }
    }
}
