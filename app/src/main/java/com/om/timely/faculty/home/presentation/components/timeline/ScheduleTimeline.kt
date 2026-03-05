package com.om.timely.faculty.home.presentation.components.timeline

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.om.timely.faculty.home.domain.model.schedule.EmptySlot
import com.om.timely.faculty.home.domain.model.schedule.LectureSlot
import com.om.timely.faculty.home.domain.model.schedule.PracticalSlot
import com.om.timely.faculty.home.domain.model.schedule.RecessSlot
import com.om.timely.faculty.home.domain.model.schedule.ScheduleSlot
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ScheduleTimeline(
    timeline: List<ScheduleSlot>,
    isEditable: Boolean = false,
    onEditClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        timeline.forEachIndexed { index, slot ->
            val isLast = index == timeline.size - 1
            
            // Safe time parsing fallback
            val isOngoing = try {
                isSlotOngoing(slot.startTime, slot.endTime)
            } catch (e: Exception) {
                false
            }
            
            when (slot) {
                is LectureSlot -> {
                    TimelineNode(
                        timeString = slot.startTime,
                        dotColor = Color(slot.colorArgb),
                        isOngoing = isOngoing,
                        isLast = isLast
                    ) {
                        LectureSlotCard(
                            slot = slot,
                            isEditable = isEditable,
                            onEditClick = { onEditClick?.invoke(slot.id) }
                        )
                    }
                }
                is PracticalSlot -> {
                    TimelineNode(
                        timeString = slot.startTime,
                        dotColor = Color(slot.colorArgb),
                        isOngoing = isOngoing,
                        isLast = isLast
                    ) {
                        PracticalSlotCard(
                            slot = slot,
                            isEditable = isEditable,
                            onEditClick = { onEditClick?.invoke(slot.id) }
                        )
                    }
                }
                is RecessSlot -> {
                    TimelineNode(
                        timeString = slot.startTime,
                        dotColor = Color(0xFFFBBF24), // Yellow dot
                        isOngoing = isOngoing,
                        isLast = isLast
                    ) {
                        RecessSlotCard(slot = slot)
                    }
                }
                is EmptySlot -> {
                    TimelineNode(
                        timeString = slot.startTime,
                        dotColor = Color(0xFFE5E7EB), // Gray dot
                        isOngoing = isOngoing,
                        isLast = isLast
                    ) {
                        EmptySlotCard(slot = slot)
                    }
                }
            }
        }
        
        // Final dot hook
        if (timeline.isNotEmpty()) {
            val lastSlot = timeline.last()
            TimelineNode(
                timeString = lastSlot.endTime,
                dotColor = Color(0xFFE5E7EB),
                isLast = true
            ) {}
        }
    }
}

private fun isSlotOngoing(startTime: String, endTime: String): Boolean {
    val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
    val start = LocalTime.parse(startTime.uppercase(), formatter)
    val end = LocalTime.parse(endTime.uppercase(), formatter)
    val now = LocalTime.now()

    return if (start.isBefore(end)) {
        // Normal case: e.g. 9:00 AM to 10:00 AM
        !now.isBefore(start) && now.isBefore(end)
    } else {
        // Slot crosses midnight (edge case)
        !now.isBefore(start) || now.isBefore(end)
    }
}
