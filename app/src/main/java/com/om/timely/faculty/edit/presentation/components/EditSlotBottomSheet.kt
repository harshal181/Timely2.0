package com.om.timely.faculty.edit.presentation.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.outlined.AssignmentInd
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.faculty.edit.domain.model.EditSlotState
import com.om.timely.faculty.edit.domain.model.EditBatchState
import com.om.timely.faculty.edit.domain.model.Faculty
import com.om.timely.faculty.edit.domain.model.Room
import com.om.timely.faculty.subjects.domain.model.Subject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSlotBottomSheet(
    state: EditSlotState,
    allSubjects: List<Subject>,
    onDismissRequest: () -> Unit,
    onSlotTypeChanged: (isLecture: Boolean) -> Unit,
    onLectureSubjectChanged: (Subject) -> Unit,
    onLectureFacultyChanged: (Faculty) -> Unit,
    onLectureRoomChanged: (Room) -> Unit,
    onBatchSubjectChanged: (batchId: String, Subject) -> Unit,
    onBatchFacultyChanged: (batchId: String, Faculty) -> Unit,
    onBatchRoomChanged: (batchId: String, Room) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    if (!state.isVisible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color(0xFFE5E7EB), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Edit Class",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = if (state.isLecture) "Duration: 1 hour" else "Duration: 2 hours",
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(18.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF4B5563),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
            ) {
                item {
                    // Slot Type Toggle
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.AssignmentInd, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Slot Type", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2937))
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TypeButton(
                            text = "Lecture (1 Hr)",
                            isSelected = state.isLecture,
                            onClick = { onSlotTypeChanged(true) },
                            modifier = Modifier.weight(1f)
                        )
                        TypeButton(
                            text = "Practical (2 Hrs)",
                            isSelected = !state.isLecture,
                            onClick = { onSlotTypeChanged(false) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (state.isLecture) {
                    item {
                        // LECTURE FORM
                        FieldLabel(Icons.Outlined.Book, "Subject")
                        SearchableComboBox(
                            items = allSubjects,
                            selectedItem = allSubjects.find { it.id.toInt() == state.lectureSubjectId },
                            itemLabel = { it.name },
                            onItemSelected = onLectureSubjectChanged,
                            placeholder = "Select subject",
                            leadingIcon = Icons.Outlined.Book
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        FieldLabel(Icons.Outlined.Person, "Faculty")
                        SearchableComboBox(
                            items = state.availableFaculties,
                            selectedItem = state.availableFaculties.find { it.id == state.lectureFacultyId },
                            itemLabel = { it.name },
                            onItemSelected = onLectureFacultyChanged,
                            placeholder = "Select faculty",
                            leadingIcon = Icons.Outlined.Person
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        FieldLabel(Icons.Outlined.LocationOn, "Room")
                        SearchableComboBox(
                            items = state.availableRooms,
                            selectedItem = state.availableRooms.find { it.id == state.lectureRoomId },
                            itemLabel = { it.roomNumber },
                            onItemSelected = onLectureRoomChanged,
                            placeholder = "Select room",
                            leadingIcon = Icons.Outlined.LocationOn
                        )
                    }
                } else {
                    // PRACTICAL FORM (Dynamically render batches)
                    items(state.practicalBatches.size) { index ->
                        val batch = state.practicalBatches[index]
                        val borderColor = if (index % 2 == 0) Color(0xFF10B981) else Color(0xFF3B82F6)
                        val bgColor = if (index % 2 == 0) Color(0xFFECFDF5) else Color(0xFFEFF6FF)

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = bgColor,
                            border = BorderStroke(1.5.dp, borderColor.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = batch.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = borderColor,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                FieldLabel(Icons.Outlined.Book, "Subject")
                                SearchableComboBox(
                                    items = allSubjects,
                                    selectedItem = allSubjects.find { it.id.toInt() == batch.subjectId },
                                    itemLabel = { it.name },
                                    onItemSelected = { onBatchSubjectChanged(batch.id, it) },
                                    placeholder = "Select subject",
                                    leadingIcon = Icons.Outlined.Book
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                FieldLabel(Icons.Outlined.Person, "Faculty")

                                SearchableComboBox(
                                    items = state.availableFaculties,
                                    selectedItem = state.availableFaculties.find { it.id == batch.facultyId },
                                    itemLabel = { it.name },
                                    onItemSelected = { onBatchFacultyChanged(batch.id, it) },
                                    placeholder = "Select faculty",
                                    leadingIcon = Icons.Outlined.Person
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                FieldLabel(Icons.Outlined.LocationOn, "Lab / Room")
                                SearchableComboBox(
                                    items = state.availableRooms,
                                    selectedItem = state.availableRooms.find { it.id == batch.roomId },
                                    itemLabel = { it.roomNumber },
                                    onItemSelected = { onBatchRoomChanged(batch.id, it) },
                                    placeholder = "Select lab",
                                    leadingIcon = Icons.Outlined.LocationOn
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Actions Layer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Delete Icon Button
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Slot",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Save Button
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Text(
                        text = "Save Changes",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun TypeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) Color(0xFF7C3AED) else Color(0xFFF3F4F6))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF6B7280)
        )
    }
}

@Composable
private fun FieldLabel(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
    }
}
