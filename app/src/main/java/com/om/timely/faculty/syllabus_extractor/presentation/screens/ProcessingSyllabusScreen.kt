package com.om.timely.faculty.syllabus_extractor.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.ui.tooling.preview.Preview
import com.om.timely.faculty.syllabus_extractor.presentation.SubjectExtractionState

@Composable
fun ProcessingSyllabusScreen(
    state: SubjectExtractionState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(80.dp),
            strokeWidth = 6.dp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Extracting Syllabus",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "DIGITAL CURATION IN PROGRESS",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.5.coerceAtLeast(0.1).dp.value.sp
        )
        Spacer(modifier = Modifier.height(48.dp))
        
        // Progress steps simulation based on state
        if (state.sseStatus == "failed") {
            Spacer(modifier = Modifier.height(24.dp))
            androidx.compose.material3.Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Job Failed",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.processingMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.Button(onClick = { /* ViewModel triggers back automatically via Snackbar anyway or user clicks back */ }) {
                        Text("Please Go Back")
                    }
                }
            }
        } else {
            val statusList = listOf("uploading", "pending", "processing", "parsing", "completed")
            val currentIndex = if (state.sseStatus.isEmpty()) 0 else statusList.indexOf(state.sseStatus).coerceAtLeast(1)

            ProcessingStep(text = "Uploading Images...", isDone = currentIndex > 0, isLoading = currentIndex == 0)
            Spacer(modifier = Modifier.height(16.dp))
            
            ProcessingStep(text = "Initializing AI Job...", isDone = currentIndex > 1, isLoading = currentIndex == 1, isPending = currentIndex < 1)
            Spacer(modifier = Modifier.height(16.dp))

            ProcessingStep(text = "AI is Reading Syllabus...", isDone = currentIndex > 2, isLoading = currentIndex == 2, isPending = currentIndex < 2)
            Spacer(modifier = Modifier.height(16.dp))

            ProcessingStep(text = "Structuring Results...", isDone = currentIndex > 3, isLoading = currentIndex == 3, isPending = currentIndex < 3)
            
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "This may take a minute, please don't close the app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private val Number.sp get() = androidx.compose.ui.unit.TextUnit(this.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp)

@Composable
fun ProcessingStep(
    text: String,
    isDone: Boolean = false,
    isLoading: Boolean = false,
    isPending: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        val color = if (isDone || isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = color)
        } else if (isDone) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Box(modifier = Modifier.size(24.dp))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = color,
            fontWeight = if (isLoading || isDone) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProcessingSyllabusScreenPreview() {
    ProcessingSyllabusScreen(
        state = SubjectExtractionState(
            currentStep = SubjectExtractionState.Step.UPLOAD,
            processingMessage = "Performing OCR..."
        )
    )
}
