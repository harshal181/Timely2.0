package com.om.timely.faculty.edit.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.faculty.edit.presentation.components.EditDaySelector
import com.om.timely.faculty.edit.presentation.components.EditToolBar
import com.om.timely.faculty.home.domain.model.schedule.EmptySlot
import com.om.timely.faculty.home.domain.model.schedule.LectureSlot
import com.om.timely.faculty.home.domain.model.schedule.PracticalSlot
import com.om.timely.faculty.home.domain.model.schedule.RecessSlot
import com.om.timely.faculty.home.presentation.components.timeline.EmptySlotCard
import com.om.timely.faculty.home.presentation.components.timeline.LectureSlotCard
import com.om.timely.faculty.home.presentation.components.timeline.PracticalSlotCard
import com.om.timely.faculty.home.presentation.components.timeline.RecessSlotCard
import com.om.timely.faculty.home.presentation.components.timeline.TimelineNode
import kotlinx.coroutines.flow.Flow
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.LocalContext
import com.om.timely.faculty.edit.presentation.components.EditSlotBottomSheet
import com.om.timely.faculty.home.domain.model.QuickStats
import com.om.timely.faculty.home.presentation.components.QuickStatsRow

@Composable
fun FacultyEditScreen(
    state: FacultyEditState,
    onAction: (FacultyEditAction) -> Unit,
    events: Flow<FacultyEditEvent>,
    onNavigateToSyllabus: () -> Unit,
    onNavigateToSubjects: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
            events.collect { event ->
                when (event) {
                    is FacultyEditEvent.ShowError -> {
                        snackbarHostState.showSnackbar(event.error.asString(context))
                    }
                    is FacultyEditEvent.NavigateToSyllabus -> {
                        onNavigateToSyllabus()
                    }
                    is FacultyEditEvent.NavigateToSubjects -> {
                        onNavigateToSubjects()
                    }
                }
            }
    }

    Scaffold(
        containerColor = Color(0xFFF8F7FF),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAction(FacultyEditAction.OnGenerateClicked) },
                containerColor = Color(0xFF7C3AED),
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Generate", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Generate", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Edit Timetable",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "Tap a card to edit/rearrange slots",
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                var expanded by remember { mutableStateOf(false) }

                // upload syllabus
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(36))
                                .background(Color(0xFFEDE9FE))
                                .clickable { expanded = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sem ${state.selectedSemester}",
                                color = Color(0xFF7C3AED),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Semester",
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            (1..8).forEach { sem ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Semester $sem",
                                            fontWeight = if (state.selectedSemester == sem) FontWeight.Bold else FontWeight.Normal,
                                            color = if (state.selectedSemester == sem) Color(0xFF7C3AED) else Color(0xFF4B5563)
                                        )
                                    },
                                    onClick = {
                                        onAction(FacultyEditAction.OnSemesterSelected(sem))
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Relocated Quick Stats
            val quickStats = QuickStats(
                subjects = 0, batches = 0, hoursPerWeek = 0
            )
            QuickStatsRow(
                stats = quickStats,
                showValue = false,
                onSubjectClick = { onAction(FacultyEditAction.OnSubjectsClicked) },
                onBatchClick = { onAction(FacultyEditAction.OnSyllabusClicked) }
            )

            // Day Selector
            EditDaySelector(
                selectedDay = state.selectedDay,
                activeDayCount = state.timeline.size,
                onDayChange = { onAction(FacultyEditAction.OnDaySelected(it)) }
            )

            // Info bar Config
            EditToolBar(
                slotCount = state.timeline.size,
                isLocked = state.isLocked,
                onAddClick = { onAction(FacultyEditAction.OnAddSlotClicked) },
                onLockClick = { onAction(FacultyEditAction.OnToggleLockClicked) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Loading state
            if (state.isLoading && state.timeline.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF7C3AED))
                }
            } else if (state.timeline.isEmpty()) {
                // NEW: Empty State Text
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No schedule found for this day.", // Feel free to change to your "erty text"
                        color = Color(0xFF9CA3AF),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                val listState = androidx.compose.foundation.lazy.rememberLazyListState()

                val reorderableState = rememberReorderableLazyColumnState(listState) { from, to ->
                    val updatedList = state.timeline.toMutableList().apply {
                        add(to.index, removeAt(from.index))
                    }
                    onAction(FacultyEditAction.OnSlotsReordered(updatedList))
                }


                LaunchedEffect(reorderableState) {
                    // Though sh.calvin.reorderable might not expose canDragOver directly in the remember call in this version,
                    // we handle the visual drag restriction by not adding the modifier to Recess.
                }

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        bottom = 100.dp // Extra space for the FAB
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    itemsIndexed(items = state.timeline, key = { _, item -> item.id }) { index, slot ->
                        val isLast = index == state.timeline.size - 1
                        val isReorderable = slot !is RecessSlot && !state.isLocked

                        ReorderableItem(reorderableState, key = slot.id, enabled = isReorderable) { isDragging ->
                            val elevation = animateDpAsState(if (isDragging) 8.dp else 0.dp)
                            
                            Box() {
                                TimelineNode(
                                    timeString = slot.startTime,
                                    dotColor = when (slot) {
                                        is LectureSlot -> Color(slot.colorArgb)
                                        is PracticalSlot -> Color(slot.colorArgb)
                                        is RecessSlot -> Color(0xFFFBBF24)
                                        is EmptySlot -> Color(0xFFE5E7EB)
                                    },
                                    isLast = isLast,
                                    extraDragHandle = if (isReorderable) {
                                        {
                                            // Specific UI injection: Gray drag container
                                            Box(
                                                modifier = Modifier
                                                    .width(28.dp)
                                                    .height(44.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color(0xFFF3F4F6))
                                                    .draggableHandle(
                                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DragIndicator,
                                                    contentDescription = "Drag",
                                                    tint = Color(0xFFD1D5DB),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                    } else null
                                ) {
                                    // Render respective card based on slot type
                                    when (slot) {
                                        is LectureSlot -> LectureSlotCard(slot = slot, isEditable = !state.isLocked, onEditClick = { onAction(FacultyEditAction.OnSlotEditClicked(slot.id)) })
                                        is PracticalSlot -> PracticalSlotCard(slot = slot, isEditable = !state.isLocked, onEditClick = { onAction(FacultyEditAction.OnSlotEditClicked(slot.id)) })
                                        is RecessSlot -> RecessSlotCard(slot = slot)
                                        is EmptySlot -> EmptySlotCard(slot = slot)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Bottom Sheets
    EditSlotBottomSheet(
        state = state.editSlotState,
        allSubjects = state.allAvailableSubjects.filter { it.semester == state.selectedSemester },
        onDismissRequest = { onAction(FacultyEditAction.OnDismissEditSlotSheet) },
        onSlotTypeChanged = { onAction(FacultyEditAction.OnSlotTypeChanged(it)) },
        onLectureSubjectChanged = { onAction(FacultyEditAction.OnLectureSubjectChanged(it)) },
        onLectureFacultyChanged = { onAction(FacultyEditAction.OnLectureFacultyChanged(it)) },
        onLectureRoomChanged = { onAction(FacultyEditAction.OnLectureRoomChanged(it)) },
        onBatchSubjectChanged = { batchId, subject -> onAction(FacultyEditAction.OnBatchSubjectChanged(batchId, subject)) },
        onBatchFacultyChanged = { batchId, faculty -> onAction(FacultyEditAction.OnBatchFacultyChanged(batchId, faculty)) },
        onBatchRoomChanged = { batchId, room -> onAction(FacultyEditAction.OnBatchRoomChanged(batchId, room)) },
        onSave = { onAction(FacultyEditAction.OnSaveSlotChanges) },
        onDelete = { onAction(FacultyEditAction.OnDeleteSlot(state.editSlotState.slotId)) }
    )
}

