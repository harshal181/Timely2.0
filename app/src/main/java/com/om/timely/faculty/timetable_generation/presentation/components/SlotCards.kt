package com.om.timely.faculty.timetable_generation.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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

// ── Dimensions (single source of truth) ──────────────────────────────────────
val SLOT_ROW_HEIGHT: Dp = 95.dp
val ROW_GAP: Dp = 4.dp
// Practical card visually spans 2 contiguous rows + the gap between them
// Practical card must span: row1_content + row1_gap + row2_content + row2_gap
val PRACTICAL_CARD_HEIGHT: Dp = SLOT_ROW_HEIGHT * 2 + ROW_GAP * 2

// ── Colour palette ────────────────────────────────────────────────────────────
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

// ── Shared inner content builder ──────────────────────────────────────────────
@Composable
private fun SlotContent(
    slot: TimetableSlot,
    palette: SlotPalette,
    scale: Float
) {
    val isPractical = slot.slotKind == SlotKind.PRACTICAL || slot.slotKind == SlotKind.WHOLE_CLASS_PRACTICAL
    val paddingHorizontal = (10f * scale).coerceAtLeast(4f).dp
    val paddingVertical = (8f * scale).coerceAtLeast(2f).dp
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = paddingHorizontal, vertical = paddingVertical),
           verticalArrangement = Arrangement.spacedBy((3f * scale).dp)) {

        // Kind pill
        Box(
            modifier = Modifier
                .background(palette.border.copy(alpha = 0.3f), RoundedCornerShape((4f * scale).dp))
                .padding(horizontal = (6f * scale).dp, vertical = (1f * scale).dp)
        ) {
            Text(
                text       = if (isPractical) "Practical  •  2 hrs" else "Lecture",
                color      = palette.accent,
                fontSize   = (11f * scale).coerceAtLeast(8f).sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height((4f * scale).dp))

        if (slot.assignments.size <= 1) {
            val a = slot.assignments.firstOrNull()
            Text(
                text       = a?.subject?.name ?: slot.title ?: "—",
                color      = palette.accent,
                fontWeight = FontWeight.SemiBold,
                fontSize   = (15f * scale).coerceAtLeast(10f).sp,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis,
                lineHeight = (19f * scale).coerceAtLeast(12f).sp
            )
            Text(
                text     = a?.faculty?.name ?: "",
                color    = palette.sub,
                fontSize = (13f * scale).coerceAtLeast(9f).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (a?.batchLabel != null) {
                Text(text = a.batchLabel, color = palette.sub, fontSize = (11f * scale).coerceAtLeast(8f).sp, fontWeight = FontWeight.Medium)
            }
        } else {
            // Multiple batches — list them one after another, simple and clean
            slot.assignments.forEachIndexed { i, a ->
                if (i > 0) Spacer(modifier = Modifier.height((8f * scale).dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy((4f * scale).dp)) {
                    if (a.batchLabel != null) {
                        Text(text = a.batchLabel, color = palette.sub, fontSize = (11f * scale).coerceAtLeast(8f).sp, fontWeight = FontWeight.Bold)
                        Text(text = "•", color = palette.sub, fontSize = (11f * scale).coerceAtLeast(8f).sp)
                    }
                    Text(
                        text       = a.subject.name,
                        color      = palette.accent,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = (13f * scale).coerceAtLeast(9f).sp,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f)
                    )
                }
                Text(
                    text     = a.faculty.name,
                    color    = palette.sub,
                    fontSize = (11f * scale).coerceAtLeast(8f).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ── Lecture card  (1 × SLOT_ROW_HEIGHT) ──────────────────────────────────────
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
            .height(SLOT_ROW_HEIGHT * scale)
            .clip(RoundedCornerShape((10f * scale).dp))
            .background(if (isDragging) palette.bg.copy(alpha = 0.85f) else palette.bg)
            .border(if (isDragging) (2f * scale).dp else (1.5f * scale).dp,
                    if (isDragging) Color(0xFF6366F1) else palette.border,
                    RoundedCornerShape((10f * scale).dp))
    ) {
        SlotContent(slot, palette, scale)
    }
}

// ── Practical card  (2 × SLOT_ROW_HEIGHT + gap) ──────────────────────────────
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
            .height(PRACTICAL_CARD_HEIGHT * scale)
            .clip(RoundedCornerShape((10f * scale).dp))
            .background(if (isDragging) palette.bg.copy(alpha = 0.85f) else palette.bg)
            .border(if (isDragging) (2f * scale).dp else (1.5f * scale).dp,
                    if (isDragging) Color(0xFF6366F1) else palette.border,
                    RoundedCornerShape((10f * scale).dp))
    ) {
        SlotContent(slot, palette, scale)
    }
}

// ── Recess banner ─────────────────────────────────────────────────────────────
@Composable
fun RecessBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF3F4F6))
            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text          = "☕  RECESS  •  1:30 – 2:00 PM",
            color         = Color(0xFF6B7280),
            fontWeight    = FontWeight.SemiBold,
            fontSize      = 11.sp,
            letterSpacing = 0.5.sp
        )
    }
}

// ── Empty slot (dashed box, 1-hour height) ────────────────────────────────────
@Composable
fun EmptySlotCard(isHovered: Boolean, modifier: Modifier = Modifier) {
    val bg     = if (isHovered) Color(0xFFEEF2FF) else Color.Transparent
    val stroke = if (isHovered) Color(0xFF818CF8) else Color(0xFFD1D5DB)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(SLOT_ROW_HEIGHT)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
    ) {
        val pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color        = stroke,
                style        = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f, pathEffect = pathEffect),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
            )
        }
    }
}
