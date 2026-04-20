package com.om.timely.faculty.syllabus_extractor.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.R
import com.om.timely.faculty.syllabus_extractor.presentation.SyllabusExtractorAction
import com.om.timely.faculty.syllabus_extractor.presentation.SubjectExtractionState
import com.om.timely.ui.theme.TimelyTheme

@Composable
fun UploadSyllabusScreen(
    state: SubjectExtractionState,
    onAction: (SyllabusExtractorAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            GettingStartedCard()
        }

        item {
            SemesterTypeSelector(
                category = state.semesterCategory,
                onCategorySelect = { onAction(SyllabusExtractorAction.OnCategoryChanged(it)) }
            )
        }

        items(state.semesterCategory.semesters.size) { index ->
            val semNumber = state.semesterCategory.semesters[index]
            val hasImage = state.selectedImages.containsKey(semNumber)
            
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                uri?.let { onAction(SyllabusExtractorAction.OnImagePicked(semNumber, it)) }
            }

            UploadImageSlot(
                semesterNumber = semNumber,
                hasImage = hasImage,
                onClick = {
                    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )
        }

        item {
            UploadSubmitButton(
                selectedCount = state.selectedImages.size,
                onSubmit = { onAction(SyllabusExtractorAction.OnSubmitUpload) }
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun GettingStartedCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF8)), // Very soft teal/mint background
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBCE1D8))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(Icons.Default.Info, contentDescription = "Info", tint = Color(0xFF0D9488), modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp))
            Column {
                Text(
                    "Getting Started",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F766E),
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Select whether your semesters are odd or even. Then upload one syllabus image for each semester.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF334155),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SemesterTypeSelector(
    category: SubjectExtractionState.SemesterCategory,
    onCategorySelect: (SubjectExtractionState.SemesterCategory) -> Unit
) {
    Column {
        Text(
            text = "Semester Type",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0F172A),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = "${category.name.lowercase().replaceFirstChar { it.uppercase() }} Semesters (${category.semesters.joinToString(", ")})",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFCBD5E1),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color(0xFF0F172A),
                    unfocusedTextColor = Color(0xFF0F172A)
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                SubjectExtractionState.SemesterCategory.values().forEach { cat ->
                    DropdownMenuItem(
                        text = { Text("${cat.name.lowercase().replaceFirstChar { it.uppercase() }} Semesters (${cat.semesters.joinToString(", ")})") },
                        onClick = {
                            onCategorySelect(cat)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun UploadImageSlot(
    semesterNumber: Int,
    hasImage: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (hasImage) Color(0xFF2DD4BF) else Color(0xFFCBD5E1)
    val stroke = Stroke(
        width = 2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .clickable(onClick = onClick)
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    style = stroke,
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (hasImage) Color(0xFFF0FDF4) else Color(0xFFF1F5F9),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.file_upload_24),
                    contentDescription = "Upload",
                    tint = if (hasImage) Color(0xFF2DD4BF) else Color(0xFF475569),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Semester $semesterNumber",
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A),
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (hasImage) "Image successfully selected" else "Tap to select image",
                fontSize = 13.sp,
                color = if (hasImage) Color(0xFF2DD4BF) else Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun UploadSubmitButton(
    selectedCount: Int,
    onSubmit: () -> Unit
) {
    val isEnabled = selectedCount == 4
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = isEnabled,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7C3AED), // Vibrant purple
                disabledContainerColor = Color(0xFF7C3AED).copy(alpha = 0.5f),
                contentColor = Color.White,
                disabledContentColor = Color.White
            )
        ) {
            Icon(Icons.Outlined.Upload, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upload & Extract ($selectedCount/4)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Please upload images for all 4 semesters to continue",
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UploadScreenPreview() {
    TimelyTheme {
        UploadSyllabusScreen(
            state = SubjectExtractionState(
                semesterCategory = SubjectExtractionState.SemesterCategory.ODD
            ),
            onAction = {}
        )
    }
}