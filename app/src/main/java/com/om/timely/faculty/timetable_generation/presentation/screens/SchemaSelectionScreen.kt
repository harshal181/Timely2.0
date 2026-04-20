package com.om.timely.faculty.timetable_generation.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
    ) {

        // ── Error Banner ───────────────────────────────────────────────────────
        AnimatedVisibility(visible = state.globalError != null) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                    Text(
                        text = state.globalError?.asString(context) ?: "",
                        color = Color(0xFFDC2626),
                        fontSize = 13.sp
                    )
                }
            }
        }

        // ── Active Job Banner ──────────────────────────────────────────────────
        AnimatedVisibility(visible = state.activeJobId != null) {
            val stage = state.jobStage
            val (bgColor, textColor, borderColor, stageLabel) = when (stage) {
                "solving"   -> listOf(Color(0xFFEFF6FF), Color(0xFF1E40AF), Color(0xFF93C5FD), "Solving…")
                "queued"    -> listOf(Color(0xFFFFFBEB), Color(0xFF92400E), Color(0xFFFCD34D), "Queued")
                "completed" -> listOf(Color(0xFFF0FDF4), Color(0xFF065F46), Color(0xFF6EE7B7), "Completed")
                else        -> listOf(Color(0xFFF3F4F6), Color(0xFF374151), Color(0xFFD1D5DB), "Pending")
            }
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = bgColor as Color),
                border = BorderStroke(1.dp, borderColor as Color),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { onAction(TimetableGenerationAction.ViewActiveJob) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Generation In Progress",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = textColor as Color
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Stage: $stageLabel  •  Tap to track",
                            fontSize = 12.sp,
                            color = (textColor as Color).copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = textColor as Color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ── Local Drafts Section ──────────────────────────────────────────────
        AnimatedVisibility(visible = state.localDrafts.isNotEmpty()) {
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                Text(
                    text = "Saved Drafts",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    items(state.localDrafts) { draft ->
                        val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                            .format(Date(draft.savedAt))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                            border = BorderStroke(1.dp, Color(0xFFDDD6FE)),
                            modifier = Modifier.width(180.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Description,
                                        contentDescription = null,
                                        tint = Color(0xFF7C3AED),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = draft.label,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF4C1D95),
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Div ${draft.division}  •  $dateStr",
                                    fontSize = 10.sp,
                                    color = Color(0xFF7C3AED).copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = { onAction(TimetableGenerationAction.LoadDraft(draft.id)) },
                                        modifier = Modifier.weight(1f).height(28.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                                    ) { Text("Open", fontSize = 11.sp) }
                                    IconButton(
                                        onClick = { onAction(TimetableGenerationAction.DeleteDraft(draft.id)) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Syllabus Section header ───────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF6366F1)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Select a Syllabus Schema",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color(0xFF111827)
            )
        }

        // ── Syllabus List ─────────────────────────────────────────────────────
        if (state.availableSyllabi.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = BrandColors.PrimaryPurple, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loading syllabuses…", color = Color(0xFF9CA3AF), fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.availableSyllabi) { syllabus ->
                    val isSelected = state.selectedSyllabus?.id == syllabus.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAction(TimetableGenerationAction.SelectSyllabus(syllabus)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFF5F3FF) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 0.dp else 1.dp),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) BrandColors.PrimaryPurple else Color(0xFFE5E7EB)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = syllabus.name,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) BrandColors.PrimaryPurple else Color(0xFF111827),
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${syllabus.dept} • ${syllabus.termType.uppercase()}",
                                    color = Color(0xFF6B7280),
                                    fontSize = 12.sp
                                )
                            }
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(BrandColors.PrimaryPurple),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Division field ─────────────────────────────────────────────────────
        OutlinedTextField(
            value = state.selectedDivision,
            onValueChange = { onAction(TimetableGenerationAction.ChangeDivision(it.uppercase().take(2))) },
            label = { Text("Division (e.g. A)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandColors.PrimaryPurple,
                focusedLabelColor = BrandColors.PrimaryPurple
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Generate Button ────────────────────────────────────────────────────
        Button(
            onClick = { onAction(TimetableGenerationAction.GenerateTimetable) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandColors.PrimaryPurple),
            enabled = state.selectedSyllabus != null && !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = if (state.isLoading) "Submitting…" else "Generate Timetable",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}