package com.om.timely.faculty.subjects.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.faculty.subjects.presentation.components.AddEditSubjectSheet
import com.om.timely.faculty.subjects.presentation.components.DeleteSubjectDialog
import com.om.timely.faculty.subjects.presentation.components.SubjectCard
import com.om.timely.shared.presentation.components.EmptyState
import kotlinx.coroutines.flow.SharedFlow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SubjectManagementScreen(
    state: SubjectManagementState,
    events: SharedFlow<SubjectManagementEvent>,
    onAction: (SubjectManagementAction) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    // Extracted background color to keep it consistent
    val backgroundColor = Color(0xFFF9FAFB)

    LaunchedEffect(key1 = state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error.asString(context))
            onAction(SubjectManagementAction.OnDismissError)
        }
    }

    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                is SubjectManagementEvent.ShowSuccess -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                SubjectManagementEvent.NavigateBack -> {
                    // Handled by parent Navigation
                }
                else -> {}
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "All Subject's",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color(0xFF111827),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(SubjectManagementAction.NavigateBack) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                actions = {
                    if (state.syllabuses.isNotEmpty()) {
                        var expanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.padding(end = 16.dp)) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFFECFDF5))
                                    .clickable { expanded = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = state.selectedSyllabus?.name ?: "Syllabus",
                                    color = Color(0xFF059669),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 100.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Syllabus",
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                state.syllabuses.forEach { syllabus ->
                                    val isSelected = state.selectedSyllabus == syllabus
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = syllabus.name,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color(0xFF059669) else Color(0xFF4B5563)
                                            )
                                        },
                                        onClick = {
                                            onAction(SubjectManagementAction.OnSyllabusSelected(syllabus))
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    // FIX 1: Prevent black flash by explicitly setting to screen background color instead of Transparent
                    containerColor = backgroundColor,
                    scrolledContainerColor = Color.White
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAction(SubjectManagementAction.OnAddSubjectClick) },
                containerColor = Color(0xFF8B5CF6),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Subject"
                )
            }
        }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // FIX 2: Wrapped the Search and Chips inside a stickyHeader block
                // Gave the Column a solid background so the subjects slide cleanly underneath
                stickyHeader {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(backgroundColor) // Solid background prevents transparency overlapping
                            .padding(bottom = 8.dp) // Optional padding before subjects start
                    ) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { onAction(SubjectManagementAction.OnSearchQueryChange(it)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = { Text("Search subjects...") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFF9CA3AF)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                focusedBorderColor = Color(0xFF7C3AED)
                            )
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                SemesterFilterChip(
                                    label = "All Semesters",
                                    selected = state.selectedSemester == null,
                                    onClick = { onAction(SubjectManagementAction.OnSemesterFilterSelected(null)) }
                                )
                            }
                            items(8) { index ->
                                val sem = index + 1
                                SemesterFilterChip(
                                    label = "Sem $sem",
                                    selected = state.selectedSemester == sem,
                                    onClick = { onAction(SubjectManagementAction.OnSemesterFilterSelected(sem)) }
                                )
                            }
                        }
                    }
                }

                // 3. The Subjects / Empty States
                if (state.isLoading && state.subjects.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (state.subjects.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyState(
                                title = "No Subjects Found",
                                message = "Click the + button to add a new subject",
                                actionText = "Retry",
                                onActionClick = {}
                            )
                        }
                    }
                } else if (state.filteredSubjects.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyState(
                                title = "No Matches found",
                                message = "Try adjusting your search or semester filter",
                                actionText = "Clear applied filters",
                                onActionClick = {
                                    onAction(SubjectManagementAction.OnSearchQueryChange(""))
                                    onAction(SubjectManagementAction.OnSemesterFilterSelected(null))
                                }
                            )
                        }
                    }
                } else {
                    items(state.filteredSubjects, key = { it.id }) { subject ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            SubjectCard(
                                subject = subject,
                                onEditClick = {
                                    onAction(SubjectManagementAction.OnEditSubjectClick(subject))
                                },
                                onDeleteClick = {
                                    onAction(SubjectManagementAction.OnDeleteSubjectClick(subject))
                                }
                            )
                        }
                    }
                }
            }

            // Conditional Overlays
            if (state.isAddEditSheetVisible) {
                AddEditSubjectSheet(
                    subject = state.subjectToEdit,
                    onDismissRequest = { onAction(SubjectManagementAction.OnDismissSheet) },
                    onSave = { onAction(SubjectManagementAction.OnSaveSubject(it)) }
                )
            }

            if (state.subjectToDelete != null) {
                DeleteSubjectDialog(
                    visible = true,
                    onDismissRequest = { onAction(SubjectManagementAction.OnDismissDialog) },
                    onConfirm = { onAction(SubjectManagementAction.OnConfirmDelete) }
                )
            }
        }
    }
}

@Composable
fun SemesterFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (selected) Color(0xFFF3E8FF) else Color.White,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = if (selected) Color(0xFF8B5CF6) else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color(0xFF6D28D9) else Color(0xFF4B5563),
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}