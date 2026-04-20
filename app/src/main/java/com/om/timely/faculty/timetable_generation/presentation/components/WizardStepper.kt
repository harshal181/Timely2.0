package com.om.timely.faculty.timetable_generation.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.faculty.timetable_generation.presentation.WizardStep

@Composable
fun WizardStepper(
    currentStep: WizardStep,
    modifier: Modifier = Modifier
) {
    // Always show the stepper for the 3-step flow

    val steps = listOf(
        WizardStep.SCHEMA_SELECTION to "Schema",
        WizardStep.PROCESSING to "Processing",
        WizardStep.REVIEW to "Review"
    )
    
    // We get the index, excluding PROCESSING
    val currentIndex = steps.indexOfFirst { it.first == currentStep }.coerceAtLeast(0)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, pair ->
            val isActive = index <= currentIndex
            val isCompleted = index < currentIndex
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) Color(0xFF7C3AED) else Color(0xFFE5E7EB)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                    } else {
                        Text(
                            text = (index + 1).toString(),
                            color = if (isActive) Color.White else Color(0xFF9CA3AF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = pair.second,
                    fontSize = 10.sp,
                    color = if (isActive) Color(0xFF4B5563) else Color(0xFF9CA3AF),
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                )
            }
            
            if (index < steps.size - 1) {
                // Line connector
                val isLineActive = index < currentIndex
                val weight by animateFloatAsState(targetValue = if (isLineActive) 1f else 0f)
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .padding(bottom = 14.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                        drawLine(
                            color = Color(0xFFE5E7EB),
                            start = Offset(0f, size.height / 2),
                            end = Offset(size.width, size.height / 2),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color(0xFF7C3AED),
                            start = Offset(0f, size.height / 2),
                            end = Offset(size.width * weight, size.height / 2),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}
