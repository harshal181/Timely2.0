package com.om.timely.faculty.syllabus.presentation

import SyllabusManagementAction
import SyllabusManagementEvent
import SyllabusManagementState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.faculty.subjects.domain.model.Syllabus
import com.om.timely.faculty.syllabus_extractor.domain.model.SyllabusDraft
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun SyllabusManagementScreen(
    state: SyllabusManagementState,
    onAction: (SyllabusManagementAction) -> Unit,
    events: SharedFlow<SyllabusManagementEvent>,
    onBackClick: () -> Unit,
    onNavigateToExtractor: (Int?) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error.asString(context))
        }
    }

    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                SyllabusManagementEvent.NavigateToSyllabusExtractor -> onNavigateToExtractor(null)
                is SyllabusManagementEvent.NavigateToResumeDraft -> onNavigateToExtractor(event.draftId)
                else -> {}
            }
        }
    }
    
    if (state.showDeleteConfirmationDialog || state.deletingDraft != null) {
        val isDraft = state.deletingDraft != null
        AlertDialog(
            onDismissRequest = { onAction(SyllabusManagementAction.OnDismissDeleteDialog) },
            title = { Text(if (isDraft) "Delete Draft?" else "Delete Syllabus?") },
            text = { Text(if (isDraft) "Are you sure you want to delete this draft? This cannot be undone." else "This will permanently remove the syllabus and all its associated subjects. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        if (isDraft) onAction(SyllabusManagementAction.OnConfirmDeleteDraft)
                        else onAction(SyllabusManagementAction.OnConfirmDelete)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(SyllabusManagementAction.OnDismissDeleteDialog) }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            SyllabusManagementTopBar(onBackClick)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F7FF))
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF7C3AED)
                )
            } else if (state.syllabuses.isEmpty() && !state.isLoading) {
                EmptySyllabusState(
                    onExtractClick = { onAction(SyllabusManagementAction.OnExtractSyllabusClicked) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ExtractSyllabusCard(
                            onClick = { onAction(SyllabusManagementAction.OnExtractSyllabusClicked) }
                        )
                    }

                    if (state.drafts.isNotEmpty()) {
                        item {
                            Text(
                                text = "Drafts (${state.drafts.size})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(state.drafts, key = { "draft_${it.id}" }) { draft ->
                            SyllabusDraftItem(
                                draft = draft,
                                onResumeClick = { onAction(SyllabusManagementAction.OnResumeDraftClick(draft)) },
                                onDeleteClick = { onAction(SyllabusManagementAction.OnDeleteDraftClick(draft)) }
                            )
                        }
                    }

                    if (state.syllabuses.isNotEmpty()) {
                        item {
                            Text(
                                text = "Existing Syllabuses",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(state.syllabuses, key = { "syllabus_${it.id}" }) { syllabus ->
                            SyllabusItem(
                                syllabus = syllabus,
                                onDeleteClick = { onAction(SyllabusManagementAction.OnDeleteSyllabusClick(syllabus)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusManagementTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                "Syllabus Management",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF1F2937)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

@Composable
fun ExtractSyllabusCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF7C3AED), Color(0xFFC084FC))
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(14.dp)
                            .size(28.dp),
                        tint = Color.White
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Extract New Syllabus",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "AI-powered scanner for your documents",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SyllabusItem(
    syllabus: Syllabus,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFF3F4F6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = syllabus.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = "ID: ${syllabus.id} • ${syllabus.termType.uppercase()}",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
fun SyllabusDraftItem(
    draft: SyllabusDraft,
    onResumeClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onResumeClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFFEF3C7), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = draft.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937),
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "DRAFT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "RESUME EDITING • ${draft.termType.uppercase()}",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            Row {
                IconButton(onClick = onResumeClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Resume",
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptySyllabusState(onExtractClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Book,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFFD1D5DB)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No syllabuses found",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4B5563)
        )
        Text(
            text = "Extract one to get started",
            fontSize = 14.sp,
            color = Color(0xFF9CA3AF)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onExtractClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Extract Syllabus")
        }
    }
}
