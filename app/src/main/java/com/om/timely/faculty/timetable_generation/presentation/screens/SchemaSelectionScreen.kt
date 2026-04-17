package com.om.timely.faculty.timetable_generation.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.faculty.timetable_generation.presentation.TimetableGenerationAction
import com.om.timely.faculty.timetable_generation.presentation.TimetableGenerationState
import com.om.timely.ui.theme.BrandColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchemaSelectionScreen(
    state: TimetableGenerationState,
    onAction: (TimetableGenerationAction) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Generate Timetable",
            color = BrandColors.TextDark,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Select a syllabus to auto-generate the timetable based on current workloads.",
            color = BrandColors.TextGray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (state.globalError != null) {
            Text(
                text = state.globalError.asString(context),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Text(text = "Available Syllabus Copies", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))

        if (state.availableSyllabi.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No syllabuses found for your department.", color = BrandColors.TextGray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.availableSyllabi) { syllabus ->
                    val isSelected = state.selectedSyllabus?.id == syllabus.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onAction(TimetableGenerationAction.SelectSyllabus(syllabus)) }
                            .then(
                                if (isSelected) Modifier.border(2.dp, BrandColors.PrimaryPurple, RoundedCornerShape(12.dp))
                                else Modifier
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) BrandColors.PrimaryPurple.copy(alpha = 0.05f) else BrandColors.BgWhite
                        ),
                        elevation = CardDefaults.cardElevation(if (isSelected) 0.dp else 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = syllabus.name,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandColors.TextDark,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${syllabus.dept} • ${syllabus.termType}",
                                color = BrandColors.TextGray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = state.selectedDivision,
            onValueChange = { onAction(TimetableGenerationAction.ChangeDivision(it.uppercase().take(2))) },
            label = { Text("Division") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandColors.PrimaryPurple,
                focusedLabelColor = BrandColors.PrimaryPurple
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onAction(TimetableGenerationAction.GenerateTimetable)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandColors.PrimaryPurple),
            enabled = state.selectedSyllabus != null
        ) {
            Text("Proceed to Generate", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}
