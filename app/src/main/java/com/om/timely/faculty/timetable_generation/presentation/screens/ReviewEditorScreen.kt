package com.om.timely.faculty.timetable_generation.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import com.om.timely.faculty.timetable_generation.presentation.TimetableGenerationAction
import com.om.timely.faculty.timetable_generation.presentation.TimetableGenerationState
import com.om.timely.faculty.timetable_generation.presentation.WizardStep
import com.om.timely.faculty.timetable_generation.presentation.components.DragDropProvider
import com.om.timely.faculty.timetable_generation.presentation.components.DraggableTimetableGrid
import com.om.timely.faculty.timetable_generation.presentation.components.ReviewEditorListView
import com.om.timely.faculty.timetable_generation.presentation.components.WizardStepper
import com.om.timely.ui.theme.BrandColors
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewEditorScreen(
    state: TimetableGenerationState,
    onAction: (TimetableGenerationAction) -> Unit
) {
    var showDraftDialog by remember { mutableStateOf(false) }
    var draftLabel by remember { mutableStateOf("") }
    val context = LocalContext.current

    // ── Nested Scroll Header Height ──────────────────────────────────────────
    // Slightly increased to fully cover the toolbar elements
    val headerHeight = 170.dp
    val headerHeightPx = with(LocalDensity.current) { headerHeight.toPx() }
    var headerOffsetHeightPx by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            // 1. onPreScroll fires BEFORE the list scrolls.
            // We use this to collapse the header when scrolling UP.
            override fun onPreScroll(available: Offset, source: NestedScrollSource): androidx.compose.ui.geometry.Offset {
                val delta = available.y
                if (delta < 0f) { // User is scrolling UP
                    val previousOffset = headerOffsetHeightPx
                    headerOffsetHeightPx = (previousOffset + delta).coerceIn(-headerHeightPx, 0f)
                    // Tell Compose we consumed this much scroll so the list doesn't double-scroll
                    return androidx.compose.ui.geometry.Offset(0f, headerOffsetHeightPx - previousOffset)
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }

            // 2. onPostScroll fires AFTER the list finishes its own scrolling.
            // We use this to expand the header only when the list hits the very top.
            override fun onPostScroll(
                consumed: androidx.compose.ui.geometry.Offset,
                available: androidx.compose.ui.geometry.Offset,
                source: NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                val delta = available.y
                if (delta > 0f) { // User is scrolling DOWN and the list is already at the top
                    val previousOffset = headerOffsetHeightPx
                    headerOffsetHeightPx = (previousOffset + delta).coerceIn(-headerHeightPx, 0f)
                    return androidx.compose.ui.geometry.Offset(0f, headerOffsetHeightPx - previousOffset)
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    // ── Save Draft Dialog ──────────────────────────────────────────────────────
    if (showDraftDialog) {
        AlertDialog(
            onDismissRequest = { showDraftDialog = false },
            title = { Text("Save Draft", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Give this draft a name (optional):", fontSize = 13.sp, color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = draftLabel,
                        onValueChange = { draftLabel = it },
                        placeholder = { Text("e.g. Sem 2 Draft v1", fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDraftDialog = false
                        onAction(TimetableGenerationAction.SaveDraftLocally(draftLabel))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showDraftDialog = false }) { Text("Cancel") }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandColors.BgFaculty)
            .nestedScroll(nestedScrollConnection)
    ) {
        // ── Timetable Content (Shifted by header offset) ──────────────────────
        val topPadding = headerHeight + with(LocalDensity.current) { headerOffsetHeightPx.toDp() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                // Prevent the content from getting sliced off by clamping it to at least 16.dp
                .padding(top = topPadding.coerceAtLeast(16.dp))
        ) {
            if (state.activeTemplate != null) {
                if (state.isListView) {
                    ReviewEditorListView(
                        days = state.activeTemplate.days,
                        onSlotDropped = { sourceDayId, sourceSlotIndex, targetDayId, targetSlotIndex ->
                            onAction(TimetableGenerationAction.SwapSlots(sourceDayId, sourceSlotIndex, targetDayId, targetSlotIndex))
                        }
                    )
                } else {
                    DragDropProvider {
                        DraggableTimetableGrid(
                            days = state.activeTemplate.days,
                            onSlotDropped = { sourceDayId, sourceSlotIndex, targetDayId, targetSlotIndex ->
                                onAction(TimetableGenerationAction.SwapSlots(sourceDayId, sourceSlotIndex, targetDayId, targetSlotIndex))
                            }
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No data available to review.", color = BrandColors.TextGray)
                }
            }
        }

        // ── Collapsing Header Section ─────────────────────────────────────────
        Column(
            modifier = Modifier
                .offset { IntOffset(x = 0, y = headerOffsetHeightPx.roundToInt()) }
                .fillMaxWidth()
                .background(BrandColors.BgFaculty)
        ) {
            // 1. Back + Nav + Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onAction(TimetableGenerationAction.ToggleViewType) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (state.isListView) Icons.Default.GridView else Icons.Default.ViewList,
                        contentDescription = "Toggle View",
                        tint = BrandColors.PrimaryPurple,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // 2. Semester Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.generatedTemplates.forEach { template ->
                    val isSelected = state.activeTemplate?.semester == template.semester
                    FilterChip(
                        selected = isSelected,
                        onClick = { onAction(TimetableGenerationAction.SelectSemesterTab(template.semester)) },
                        label = {
                            Text(
                                "Semester ${template.semester}",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandColors.PrimaryPurple,
                            selectedLabelColor = BrandColors.BgWhite
                        )
                    )
                }
            }

                // 3. Action Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onAction(TimetableGenerationAction.Undo) },
                        enabled = state.undoStack.isNotEmpty(),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Undo", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Undo", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { onAction(TimetableGenerationAction.Redo) },
                        enabled = state.redoStack.isNotEmpty(),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Redo", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Redo", fontSize = 12.sp)
                    }

                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color(0xFFE5E7EB)))

                    OutlinedButton(
                        onClick = {
                            draftLabel = ""
                            showDraftDialog = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF374151))
                    ) {
                        if (state.isDraftSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Save, contentDescription = "Draft", modifier = Modifier.size(15.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (state.activeDraftId != null) "Update Draft" else "Draft", fontSize = 12.sp)
                    }

                    // Export PDF Button
                    OutlinedButton(
                        onClick = { onAction(TimetableGenerationAction.ExportToPdf) },
                        enabled = !state.isPdfExporting,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFB45309),
                            disabledContentColor = Color(0xFFD97706).copy(alpha = 0.5f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (!state.isPdfExporting) Color(0xFFB45309) else Color(0xFFB45309).copy(alpha = 0.4f)
                        )
                    ) {
                        if (state.isPdfExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFFB45309)
                            )
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", modifier = Modifier.size(15.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (state.isPdfExporting) "Exporting…" else "Export PDF", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onAction(TimetableGenerationAction.SaveToDatabase) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Publish", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Publish", fontSize = 12.sp)
                    }
                }

            // 4. Compact Hint
            Text(
                text = "💡 Drag slots to swap within the same day",
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            )

            HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
        }
    }

    // ── PDF Export Bottom Sheet ───────────────────────────────────────────────
    if (state.showPdfSheet) {
        ModalBottomSheet(
            onDismissRequest = { onAction(TimetableGenerationAction.DismissPdfSheet) },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "Export Complete!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF111827)
                        )
                        Text(
                            "${state.pdfAssets.size} PDF(s) ready to download",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFE5E7EB))
                Spacer(modifier = Modifier.height(12.dp))

                // Asset list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(state.pdfAssets) { asset ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF7ED),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = asset.label,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF92400E)
                                    )
                                    if (asset.semester != null) {
                                        Text(
                                            text = "Semester ${asset.semester}",
                                            fontSize = 11.sp,
                                            color = Color(0xFFB45309)
                                        )
                                    } else {
                                        Text(
                                            text = "Combined",
                                            fontSize = 11.sp,
                                            color = Color(0xFFB45309)
                                        )
                                    }
                                    Text(
                                        text = asset.filename,
                                        fontSize = 10.sp,
                                        color = Color(0xFF9CA3AF),
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                // Download button: opens PDF URL directly in Chrome
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(asset.url)).apply {
                                            setPackage("com.android.chrome")
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            intent.setPackage(null)
                                            context.startActivity(intent)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(
                                            color = Color(0xFFFEF3C7),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Download ${asset.label}",
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Done button
                Button(
                    onClick = { onAction(TimetableGenerationAction.DismissPdfSheet) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309))
                ) {
                    Text("Done", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}