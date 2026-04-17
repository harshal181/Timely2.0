package com.om.timely.faculty.timetable_generation.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures // Added
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput // Added
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp // Added
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.faculty.timetable_generation.domain.model.SlotKind
import com.om.timely.faculty.timetable_generation.domain.model.TimetableDay
import com.om.timely.faculty.timetable_generation.domain.model.TimetableSlot

private val TIME_COL_WIDTH = 40.dp
data class TimeRow(val label: String, val startTime: String, val isRecess: Boolean = false)

val TIME_ROWS = listOf(
    TimeRow("10:30", "10:30 AM"),
    TimeRow("11:30", "11:30 AM"),
    TimeRow("12:30", "12:30 PM"),
    TimeRow("1:30",  "1:30 PM",  isRecess = true),
    TimeRow("2:00",  "2:00 PM"),
    TimeRow("3:00",  "3:00 PM"),
    TimeRow("4:00",  "4:00 PM"),
)

@Composable
fun DraggableTimetableGrid(
    days: List<TimetableDay>,
    scale: Float = 1f, // 1. Accept scale state
    onScaleChange: (Float) -> Unit = {}, // 2. Accept callback for pinch-to-zoom
    modifier: Modifier = Modifier,
    onSlotDropped: (sourceDayId: Int, sourceSlotIndex: Int, targetDayId: Int, targetSlotIndex: Int) -> Unit
) {
    // 3. Attach gesture detector to capture pinch actions
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    // Limit zoom between 50% and 250%
                    onScaleChange((scale * zoom).coerceIn(0.5f, 2.5f))
                }
            }
    ) {
        // 4. Multiply core dimensions by the active scale
        val dynamicDayColWidth = (maxWidth * 0.85f) * scale
        val scaledTimeColWidth = TIME_COL_WIDTH * scale
        val scaledSlotRowHeight = SLOT_ROW_HEIGHT * scale
        val scaledRowGap = ROW_GAP * scale
        val scaledFontSize = (13 * scale).coerceAtLeast(8f).sp // Prevents text from getting too small

        val hScroll = rememberScrollState()
        val vScroll = rememberScrollState()
        val dragState = LocalDragDropState.current

        val absorbedRows: Set<Pair<Int, Int>> = buildSet {
            for (day in days) {
                for (slot in day.slots) {
                    if (slot.slotKind == SlotKind.PRACTICAL || slot.slotKind == SlotKind.WHOLE_CLASS_PRACTICAL) {
                        val row = TIME_ROWS.indexOfFirst { it.label == slot.startTime }
                        if (row != -1) add(Pair(day.id, row + 1))
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Sticky day-name header ────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.width(scaledTimeColWidth)) // Scaled spacer
                Row(modifier = Modifier.horizontalScroll(hScroll)) {
                    days.forEach { day ->
                        Box(
                            modifier = Modifier
                                .width(dynamicDayColWidth) // Scaled width
                                .padding(end = scaledRowGap) // Scaled padding
                                .height(40.dp * scale) // Scaled height
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF4F46E5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(day.dayOfWeek, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = scaledFontSize)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp * scale))

            // ── Scrollable rows ───────────────────────────────────────────────────
            Column(modifier = Modifier.verticalScroll(vScroll)) {

                TIME_ROWS.forEachIndexed { rowIndex, timeRow ->

                    if (timeRow.isRecess) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = scaledRowGap),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TimeLabel(timeRow.label, scaledTimeColWidth, scaledSlotRowHeight, scale)
                            Row(modifier = Modifier.horizontalScroll(hScroll)) {
                                val totalWidth = dynamicDayColWidth * days.size + scaledRowGap * (days.size - 1)
                                RecessBanner(modifier = Modifier.width(totalWidth).height(scaledSlotRowHeight))
                            }
                        }
                        return@forEachIndexed
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = scaledRowGap),
                        verticalAlignment = Alignment.Top
                    ) {
                        TimeLabel(timeRow.label, scaledTimeColWidth, scaledSlotRowHeight, scale)

                        Row(modifier = Modifier.horizontalScroll(hScroll)) {
                            days.forEach { day ->
                                val cellMod = Modifier
                                    .width(dynamicDayColWidth)
                                    .padding(end = scaledRowGap)

                                if (Pair(day.id, rowIndex) in absorbedRows) {
                                    Box(modifier = cellMod.height(scaledSlotRowHeight))
                                    return@forEach
                                }

                                val slot: TimetableSlot? = day.slots.firstOrNull { it.startTime == timeRow.label }
                                val absSlotIndex = rowIndex

                                val targetId = "target_${day.id}_${absSlotIndex}"
                                val isHovered = dragState.isDragging && dragState.currentDropTargetId == targetId

                                Box(
                                    modifier = cellMod
                                        .height(if (slot?.slotKind == SlotKind.PRACTICAL) scaledSlotRowHeight * 2 + scaledRowGap else scaledSlotRowHeight)
                                        .dropTarget(
                                            dayId = day.id,
                                            slotIndex = absSlotIndex,
                                            onDropped = { srcDay, srcSlot ->
                                                onSlotDropped(srcDay, srcSlot, day.id, absSlotIndex)
                                            }
                                        )
                                ) {
                                    when {
                                        slot == null || slot.slotKind == SlotKind.EMPTY || slot.slotKind == SlotKind.BREAK ->
                                            EmptySlotCard(isHovered = isHovered)

                                        slot.slotKind == SlotKind.LECTURE || slot.slotKind == SlotKind.TUTORIAL -> {
                                            val isDragged = dragState.isDragging && dragState.draggedSlot?.id == slot.id
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize() // Fill scaled parent Box
                                                    .dragTarget(
                                                        slot = slot,
                                                        dayId = day.id,
                                                        slotIndex = absSlotIndex,
                                                        composable = { LectureSlotCard(slot = slot, scale = scale, isDragging = true) }
                                                    )
                                                    .alpha(if (isDragged) 0f else 1f)
                                            ) {
                                                LectureSlotCard(
                                                    slot = slot,
                                                    scale = scale,
                                                    modifier = if (isHovered)
                                                        Modifier.border(2.dp, Color(0xFF818CF8), RoundedCornerShape(10.dp))
                                                    else Modifier
                                                )
                                            }
                                        }

                                        else -> {
                                            val isDragged = dragState.isDragging && dragState.draggedSlot?.id == slot.id
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize() // Fill scaled parent Box
                                                    .dragTarget(
                                                        slot = slot,
                                                        dayId = day.id,
                                                        slotIndex = absSlotIndex,
                                                        composable = { PracticalSlotCard(slot = slot, scale = scale, isDragging = true) }
                                                    )
                                                    .alpha(if (isDragged) 0f else 1f)
                                            ) {
                                                PracticalSlotCard(
                                                    slot = slot,
                                                    scale = scale,
                                                    modifier = if (isHovered)
                                                        Modifier.border(2.dp, Color(0xFF818CF8), RoundedCornerShape(10.dp))
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

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ── Time label ────────────────────────────────────────────────────────────────
@Composable
private fun TimeLabel(label: String, width: Dp, height: Dp, scale: Float) {
    Box(
        modifier = Modifier.width(width).height(height),
        contentAlignment = Alignment.TopStart
    ) {
        val fontSize = (10 * scale).coerceAtLeast(8f).sp
        Text(
            text = label,
            color = Color(0xFF9CA3AF),
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp * scale, end = 4.dp * scale)
        )
    }
}