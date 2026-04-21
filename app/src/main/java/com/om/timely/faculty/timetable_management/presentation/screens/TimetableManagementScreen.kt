package com.om.timely.faculty.timetable_management.presentation.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.faculty.timetable_management.presentation.TimetableManagementAction
import com.om.timely.faculty.timetable_management.presentation.TimetableManagementEvent
import com.om.timely.faculty.timetable_management.presentation.TimetableManagementState
import com.om.timely.faculty.timetable_management.presentation.components.TimetableGroupCard
import com.om.timely.ui.theme.BrandColors
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableManagementScreen(
    state: TimetableManagementState,
    events: Flow<TimetableManagementEvent>,
    onAction: (TimetableManagementAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                is TimetableManagementEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message.asString(context))
                }
                TimetableManagementEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Manage Timetables",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = BrandColors.BgFaculty
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading && state.groups.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.groups.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No timetable versions found.", color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onAction(TimetableManagementAction.LoadGroups) }) {
                        Text("Retry")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.groups) { group ->
                        TimetableGroupCard(
                            group = group,
                            onToggleActive = { isCurrentlyActive ->
                                onAction(TimetableManagementAction.ToggleActive(group.id, isCurrentlyActive))
                            },
                            onExportPdf = {
                                onAction(TimetableManagementAction.ExportPdf(group.id))
                            }
                        )
                    }
                }
            }

            if (state.isActivating || state.isExporting || state.isDeleting) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.3f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        }
    }

    // PDF Export Bottom Sheet
    if (state.showPdfSheet) {
        ModalBottomSheet(
            onDismissRequest = { onAction(TimetableManagementAction.DismissPdfSheet) },
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
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
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
                                    }
                                    Text(
                                        text = asset.filename,
                                        fontSize = 10.sp,
                                        color = Color(0xFF9CA3AF),
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
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
                                        contentDescription = "Download",
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onAction(TimetableManagementAction.DismissPdfSheet) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                ) {
                    Text("Done", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
