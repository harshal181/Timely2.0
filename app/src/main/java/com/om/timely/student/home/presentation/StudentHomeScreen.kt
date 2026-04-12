package com.om.timely.student.home.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.faculty.home.presentation.components.DaySelector
import com.om.timely.faculty.home.presentation.components.timeline.ScheduleTimeline
import com.om.timely.shared.presentation.components.EmptyState
import com.om.timely.shared.presentation.components.ErrorState
import com.om.timely.student.home.presentation.components.StudentHomeHeader
import kotlinx.coroutines.flow.Flow

@Composable
fun StudentHomeScreen(
    state: StudentHomeState,
    onAction: (StudentHomeAction) -> Unit,
    events: Flow<StudentHomeEvent>,
    onMenuClick: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        events.collect { event ->
            when (event) {
                is StudentHomeEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.error.asString(context))
                }
                StudentHomeEvent.NavigateToNotifications -> {
                    onNavigateToNotifications()
                }
            }
        }
    }

    val greeting = rememberTimeBasedGreeting()

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Fixed Header
                Box(modifier = Modifier.padding(top = 44.dp)) {
                    StudentHomeHeader(
                        profile = state.profile,
                        greeting = greeting,
                        onMenuClick = onMenuClick,
                        onSearchClick = { /* TODO: Implement Search */ },
                        onNotificationClick = { onAction(StudentHomeAction.OnNotificationClick) }
                    )
                }

                if (state.isLoading && state.scheduleInfo == null) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                        stickyHeader {
                            Box(modifier = Modifier.background(Color.White)) {
                                DaySelector(
                                    selectedDay = state.selectedDay,
                                    onDayChange = { onAction(StudentHomeAction.OnDaySelected(it)) }
                                )
                            }
                        }

                        item {
                            // Schedule Title & Semester Selector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Schedule",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )

                                var expanded by remember { mutableStateOf(false) }

                                Box {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(0xFFEDE9FE))
                                            .clickable { expanded = true }
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Semester ${state.selectedSemester}",
                                            color = Color(0xFF7C3AED),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Select Semester",
                                            tint = Color(0xFF7C3AED),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.background(Color.White)
                                    ) {
                                        (1..8).forEach { sem ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        "Semester $sem",
                                                        fontWeight = if (state.selectedSemester == sem) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (state.selectedSemester == sem) Color(0xFF7C3AED) else Color(0xFF4B5563)
                                                    )
                                                },
                                                onClick = {
                                                    onAction(StudentHomeAction.OnSemesterSelected(sem))
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Timeline slots
                        item {
                            state.scheduleInfo?.timeline?.let { timeline ->
                                when {
                                    state.isLoading && timeline == null -> {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = Color(0xFF7C3AED))
                                        }
                                    }

                                    timeline.isNullOrEmpty() || state.error != null -> {
                                        if (state.error != null) {
                                            ErrorState(
                                                error = state.error.asString(context),
                                                onRetry = { onAction(StudentHomeAction.OnDaySelected(state.selectedDay)) }
                                            )
                                        } else {
                                            EmptyState(onActionClick = {
                                                onAction(StudentHomeAction.OnDaySelected(state.selectedDay))
                                            })
                                        }
                                    }

                                    else -> {
                                        AnimatedVisibility(
                                            visible = !state.isLoading,
                                            enter = fadeIn() + slideInHorizontally { it / 4 }
                                        ) {
                                            ScheduleTimeline(
                                                timeline = timeline,
                                                isEditable = false, // Read-only for students
                                                modifier = Modifier.padding(top = 16.dp)
                                            )
                                        }
                                    }
                                }

                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(32.dp)) // Normal bottom padding
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberTimeBasedGreeting(): String {
    val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        h < 12 -> "Good morning"
        h < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}
