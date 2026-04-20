package com.om.timely.faculty.timetable_generation.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.faculty.timetable_generation.domain.model.SlotKind
import com.om.timely.faculty.timetable_generation.domain.model.TimetableSlot

// ── Dimensions ──────────────────────────────────────────────────────────────
val SLOT_ROW_HEIGHT: Dp = 140.dp
val ROW_GAP: Dp = 6.dp
val PRACTICAL_CARD_HEIGHT: Dp = SLOT_ROW_HEIGHT * 2 + ROW_GAP * 2

// ── Color palette ────────────────────────────────────────────────────────────
data class SlotPalette(val bg: Color, val border: Color, val accent: Color, val sub: Color)

fun slotPalette(kind: SlotKind): SlotPalette = when (kind) {
    SlotKind.LECTURE, SlotKind.TUTORIAL -> SlotPalette(
        bg     = Color(0xFFEFF6FF),
        border = Color(0xFF93C5FD),
        accent = Color(0xFF1E3A8A),
        sub    = Color(0xFF3B82F6)
    )
    else -> SlotPalette(                        // PRACTICAL / WHOLE_CLASS_PRACTICAL
        bg     = Color(0xFFFAF5FF),
        border = Color(0xFFD8B4FE),
        accent = Color(0xFF581C87),
        sub    = Color(0xFF7C3AED)
    )
}

// ── Shared Content Builder ────────────────────────────────────────────────────
@Composable
private fun SlotContent(
    slot: TimetableSlot,
    palette: SlotPalette
) {
    val isPractical = slot.slotKind == SlotKind.PRACTICAL || slot.slotKind == SlotKind.WHOLE_CLASS_PRACTICAL
    val assignments = slot.assignments
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        // 1. Header (Tag + Drag Handle)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Move",
                tint = palette.accent.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Assignments Area
        if (assignments.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                assignments.forEachIndexed { index, a ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = palette.border.copy(alpha = 0.3f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        // Batch Tag (if multiple)
                        if (assignments.size > 1 || a.batchLabel != null) {
                            Text(
                                text = "BATCH: ${a.batchLabel ?: "ALL"}",
                                color = palette.sub,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        // Subject Abbr + Code
                        Text(
                            text = "${a.subject.abbr} • ${a.subject.code}",
                            color = palette.accent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1
                        )

                        // Subject Full Name
                        Text(
                            text = a.subject.name,
                            color = palette.accent,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            maxLines = if (assignments.size > 1) 1 else 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp
                        )

                        // Faculty Name
                        Text(
                            text = a.faculty.name,
                            color = palette.sub,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                Text(text = "Empty Slot", color = palette.sub.copy(alpha = 0.4f), fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun LectureSlotCard(
    slot: TimetableSlot,
    scale: Float = 1f, 
    isDragging: Boolean = false,
    modifier: Modifier = Modifier
) {
    val palette = slotPalette(slot.slotKind)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(SLOT_ROW_HEIGHT)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDragging) palette.bg.copy(alpha = 0.8f) else palette.bg)
            .border(
                width = if (isDragging) 2.dp else 1.dp,
                color = if (isDragging) Color(0xFF6366F1) else palette.border,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        SlotContent(slot, palette)
    }
}

@Composable
fun PracticalSlotCard(
    slot: TimetableSlot,
    scale: Float = 1f, 
    isDragging: Boolean = false,
    modifier: Modifier = Modifier
) {
    val palette = slotPalette(slot.slotKind)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(PRACTICAL_CARD_HEIGHT)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDragging) palette.bg.copy(alpha = 0.8f) else palette.bg)
            .border(
                width = if (isDragging) 2.dp else 1.dp,
                color = if (isDragging) Color(0xFF6366F1) else palette.border,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        SlotContent(slot, palette)
    }
}

@Composable
fun RecessBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF9FAFB))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text          = "LUNCH BREAK  •  1:30 – 2:00 PM",
            color         = Color(0xFF9CA3AF),
            fontWeight    = FontWeight.Bold,
            fontSize      = 10.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun EmptySlotCard(isHovered: Boolean, modifier: Modifier = Modifier) {
    val bg     = if (isHovered) Color(0xFFF5F3FF) else Color.Transparent
    val stroke = if (isHovered) Color(0xFF8B5CF6) else Color(0xFFE5E7EB)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(SLOT_ROW_HEIGHT)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
    ) {
        val pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color        = stroke,
                style        = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f, pathEffect = pathEffect),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
            )
        }
        
        if (isHovered) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Drop here", color = Color(0xFF8B5CF6), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
