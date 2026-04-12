package com.om.timely.faculty.syllabus_extractor.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.faculty.syllabus_extractor.presentation.SubjectExtractionState
import com.om.timely.faculty.syllabus_extractor.presentation.SubjectExtractionState.Step

@Composable
fun SyllabusStepper(
    currentStep: Step,
    modifier: Modifier = Modifier
) {
    val steps = listOf("Upload", "Extract", "Review")
    val currentStepIndex = currentStep.ordinal

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, title ->
            val isActive = index == currentStepIndex
            val isCompleted = index < currentStepIndex
            val color = if (isActive || isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            val contentColor = if (isActive || isCompleted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = "Completed", tint = contentColor)
                    } else {
                        Text(
                            text = (index + 1).toString(),
                            color = contentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
            }
            
            if (index < steps.size - 1) {
                val lineColor = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                Canvas(modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .height(2.dp)) {
                    drawLine(
                        color = lineColor,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = size.height
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SyllabusStepperPreview1() {
    SyllabusStepper(
        currentStep = SubjectExtractionState.Step.UPLOAD
    )
}

@Preview(showBackground = true)
@Composable
private fun SyllabusStepperPreview2() {
    SyllabusStepper(
        currentStep = SubjectExtractionState.Step.PROCESSING
    )
}


@Preview(showBackground = true)
@Composable
private fun SyllabusStepperPreview3() {
    SyllabusStepper(
        currentStep = SubjectExtractionState.Step.REVIEW
    )
}
