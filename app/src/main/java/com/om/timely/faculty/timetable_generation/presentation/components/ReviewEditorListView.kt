package com.om.timely.faculty.timetable_generation.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.faculty.edit.presentation.components.EditDaySelector
import com.om.timely.faculty.home.presentation.components.timeline.TimelineNode
import com.om.timely.faculty.timetable_generation.domain.model.SlotKind
import com.om.timely.faculty.timetable_generation.domain.model.TimetableDay
import com.om.timely.faculty.timetable_generation.domain.model.TimetableSlot

// --- Custom Components for List View to ensure they are compact and wrap content ---

@Composable
private fun ListViewSlotContent(slot: TimetableSlot, palette: SlotPalette) {
    val isPractical = slot.slotKind == SlotKind.PRACTICAL || slot.slotKind == SlotKind.WHOLE_CLASS_PRACTICAL
    val assignments = slot.assignments

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        // Tag Header
        Box(
            modifier = Modifier
                .background(palette.border.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text       = if (isPractical) "PRACTICAL" else "LECTURE",
                color      = palette.accent,
                fontSize   = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (assignments.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                assignments.forEachIndexed { index, a ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = palette.border.copy(alpha = 0.3f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (assignments.size > 1 || a.batchLabel != null) {
                            Text(
                                text = "BATCH: ${a.batchLabel ?: "ALL"}",
                                color = palette.sub,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = "${a.subject.abbr} • ${a.subject.code}",
                            color = palette.accent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1
                        )

                        Text(
                            text = a.subject.name,
                            color = palette.accent,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp
                        )

                        Text(
                            text = a.faculty.name,
                            color = palette.sub,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.CenterStart) {
                Text(text = "Empty Slot", color = palette.sub.copy(alpha = 0.4f), fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ListViewCard(
    slot: TimetableSlot,
    isDragging: Boolean = false,
    modifier: Modifier = Modifier
) {
    val palette = slotPalette(slot.slotKind)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight() // Key change: dynamic height, NOT fixed
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDragging) palette.bg.copy(alpha = 0.8f) else palette.bg)
            .border(
                width = if (isDragging) 2.dp else 1.dp,
                color = if (isDragging) Color(0xFF6366F1) else palette.border,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        ListViewSlotContent(slot, palette)
    }
}

// -----------------------------------------------------------------------------

@Composable
fun ReviewEditorListView(
    days: List<TimetableDay>,
    onSlotDropped: (sourceDayId: Int, sourceSlotIndex: Int, targetDayId: Int, targetSlotIndex: Int) -> Unit
) {
    if (days.isEmpty()) return

    var selectedDayId by remember { mutableIntStateOf(days.first().id) }
    val selectedDay = days.firstOrNull { it.id == selectedDayId } ?: days.first()

    // Wrap the entire list view in DragDropProvider to enable 1:1 swapping just like the Grid
    DragDropProvider {
        Column(modifier = Modifier.fillMaxSize()) {

            EditDaySelector(
                selectedDay = selectedDay.dayOfWeek,
                activeDayCount = selectedDay.slots.filter { !it.isEmpty }.size,
                onDayChange = { dayStr ->
                    days.firstOrNull { it.dayOfWeek == dayStr }?.let { selectedDayId = it.id }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            val listState = androidx.compose.foundation.lazy.rememberLazyListState()
            val dragState = LocalDragDropState.current
            
            // Identify absorbed rows for practicals
            val absorbedIndices = remember(selectedDay.slots) {
                val set = mutableSetOf<Int>()
                for (i in selectedDay.slots.indices) {
                    val slot = selectedDay.slots[i]
                    if (slot.slotKind == SlotKind.PRACTICAL || slot.slotKind == SlotKind.WHOLE_CLASS_PRACTICAL) {
                        set.add(i + 1)
                    }
                }
                set
            }

            val visibleSlots = selectedDay.slots.filterIndexed { index, _ -> index !in absorbedIndices }

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(items = visibleSlots, key = { _, item -> item.id }) { idx, slot ->
                    val isLast = idx == visibleSlots.size - 1
                    
                    val absSlotIndex = slot.slotIndex
                    val targetId = "target_${selectedDay.id}_${absSlotIndex}"
                    val isHovered = dragState.isDragging && dragState.currentDropTargetId == targetId

                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                        TimelineNode(
                            timeString = slot.startTime,
                            dotColor = when (slot.slotKind) {
                                SlotKind.LECTURE -> Color(0xFF3B82F6)
                                SlotKind.PRACTICAL, SlotKind.WHOLE_CLASS_PRACTICAL -> Color(0xFF7C3AED)
                                SlotKind.BREAK -> Color(0xFFFBBF24)
                                else -> Color(0xFFE5E7EB)
                            },
                            isLast = isLast,
                            // Keep timeline narrow, no extra bulky drag indicator
                            extraDragHandle = null
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    // Make this slot a drop target (unless Break)
                                    .then(
                                        if (slot.slotKind != SlotKind.BREAK) {
                                            Modifier.dropTarget(
                                                dayId = selectedDay.id,
                                                slotIndex = absSlotIndex,
                                                onDropped = { srcDay, srcSlot ->
                                                    onSlotDropped(srcDay, srcSlot, selectedDay.id, absSlotIndex)
                                                }
                                            )
                                        } else Modifier
                                    )
                            ) {
                                when (slot.slotKind) {
                                    SlotKind.BREAK -> {
                                        RecessBanner()
                                    }
                                    SlotKind.EMPTY -> {
                                        EmptySlotCard(isHovered = isHovered)
                                    }
                                    else -> {
                                        val isDragged = dragState.isDragging && dragState.draggedSlot?.id == slot.id

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .dragTarget(
                                                    slot = slot,
                                                    dayId = selectedDay.id,
                                                    slotIndex = absSlotIndex,
                                                    composable = { ListViewCard(slot = slot, isDragging = true) }
                                                )
                                                .alpha(if (isDragged) 0f else 1f)
                                        ) {
                                            ListViewCard(
                                                slot = slot,
                                                modifier = if (isHovered)
                                                    Modifier.border(2.dp, Color(0xFF818CF8), RoundedCornerShape(12.dp))
                                                else Modifier
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}