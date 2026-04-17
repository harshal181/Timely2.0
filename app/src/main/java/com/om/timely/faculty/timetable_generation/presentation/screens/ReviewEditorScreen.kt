package com.om.timely.faculty.timetable_generation.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add // Added for zoom
import androidx.compose.material.icons.filled.Remove // Added for zoom
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.faculty.timetable_generation.presentation.TimetableGenerationAction
import com.om.timely.faculty.timetable_generation.presentation.TimetableGenerationState
import com.om.timely.faculty.timetable_generation.presentation.components.DraggableTimetableGrid
import com.om.timely.faculty.timetable_generation.presentation.components.ZoomControls
import com.om.timely.ui.theme.BrandColors

@Composable
fun ReviewEditorScreen(
    state: TimetableGenerationState,
    onAction: (TimetableGenerationAction) -> Unit
) {
    // 1. Create a state for the zoom scale
    var zoomScale by remember { mutableFloatStateOf(1f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandColors.BgFaculty)
            .padding(16.dp)
    ) {
        // App Bar / Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Timetable Editor",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = BrandColors.TextDark
            )
            Spacer(modifier = Modifier.weight(1f))

        }

        Spacer(modifier = Modifier.height(16.dp))

        // Semester Tabs
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.generatedTemplates.forEach { template ->
                val isSelected = state.activeTemplate?.semester == template.semester
                FilterChip(
                    selected = isSelected,
                    onClick = { onAction(TimetableGenerationAction.SelectSemesterTab(template.semester)) },
                    label = { Text("Semester ${template.semester}", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandColors.PrimaryPurple,
                        selectedLabelColor = BrandColors.BgWhite
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Undo / Redo / Save Toolbar
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { onAction(TimetableGenerationAction.Undo) },
                enabled = state.undoStack.isNotEmpty(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Undo")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Undo")
            }
            OutlinedButton(
                onClick = { onAction(TimetableGenerationAction.Redo) },
                enabled = state.redoStack.isNotEmpty(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Redo")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Redo")
            }
            ZoomControls(
                currentScale = zoomScale,
                onScaleChange = { zoomScale = it }
            )

            Button(
                onClick = { onAction(TimetableGenerationAction.SaveTimetable) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandColors.Success)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dynamic Legend Map & Tip
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandColors.CardYellowLight.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "💡 Drag & drop to swap slots within the same day. 2-hour practical blocks move together.",
                    fontSize = 12.sp,
                    color = BrandColors.TextMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Complex Timetable Layout Box
        if (state.activeTemplate != null) {
            com.om.timely.faculty.timetable_generation.presentation.components.DragDropProvider {
                DraggableTimetableGrid(
                    days = state.activeTemplate.days,
                    scale = zoomScale, // Pass scale down
                    onScaleChange = { zoomScale = it }, // Update scale on pinch
                    onSlotDropped = { sourceDayId, sourceSlotIndex, targetDayId, targetSlotIndex ->
                        onAction(TimetableGenerationAction.SwapSlots(sourceDayId, sourceSlotIndex, targetDayId, targetSlotIndex))
                    }
                )
            }
        } else {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No data available to review.", color = BrandColors.TextGray)
            }
        }
    }
}