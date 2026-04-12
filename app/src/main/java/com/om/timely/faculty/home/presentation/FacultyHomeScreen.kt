package com.om.timely.faculty.home.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
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
import com.om.timely.faculty.home.presentation.components.QuickStatsRow
import com.om.timely.faculty.home.presentation.components.TodayLoadBanner
import com.om.timely.faculty.home.presentation.components.timeline.ScheduleTimeline
import com.om.timely.shared.presentation.components.EmptyState
import com.om.timely.shared.presentation.components.ErrorState
import com.om.timely.shared.presentation.components.HomeHeader
import kotlinx.coroutines.flow.SharedFlow
import java.util.Calendar

// 1. ADDED OPT-IN FOR STICKY HEADER
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FacultyHomeScreen(
    state: FacultyHomeState,
    onAction: (FacultyHomeAction) -> Unit,
    events: SharedFlow<FacultyHomeEvent>
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(true) {
        events.collect { event ->
            when (event) {
                is FacultyHomeEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.error.asString(context)
                    )
                }
            }
        }
    }

    // Determine time-based greeting locally instead of passing through VM
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
                    HomeHeader(
                        profile = state.profile,
                        greeting = greeting,
                        onNotificationClick = { onAction(FacultyHomeAction.OnNotificationClick) }
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
                        // 2. FIXED LAZY COLUMN STRUCTURE - Removed the giant wrapping 'item' block

                        item {
                            state.scheduleInfo?.dailyStats?.let { dailyStats ->
                                TodayLoadBanner(stats = dailyStats)
                            }
                        }

                        stickyHeader {
                            Box(modifier = Modifier.background(Color.White)) {
                                DaySelector(
                                    selectedDay = state.selectedDay,
                                    onDayChange = { onAction(FacultyHomeAction.OnDaySelected(it)) }
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
                                    text = "My Schedule",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )

                                var expanded by remember { mutableStateOf(false) }

                                Box {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(0xFFECFDF5))
                                            .clickable { expanded = true }
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Semester ${state.selectedSemester}",
                                            color = Color(0xFF059669),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Select Semester",
                                            tint = Color(0xFF059669),
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
                                                        color = if (state.selectedSemester == sem) Color(
                                                            0xFF059669
                                                        ) else Color(0xFF4B5563)
                                                    )
                                                },
                                                onClick = {
                                                    onAction(
                                                        FacultyHomeAction.OnSemesterSelected(
                                                            sem
                                                        )
                                                    )
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
                            when {
                                // 1. Still loading data
                                state.isLoading && (state.scheduleInfo == null || state.scheduleInfo.timeline.isNullOrEmpty()) -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = Color(0xFF059669))
                                    }
                                }

                                // 2. Error occurred while fetching
                                state.error != null -> {
                                    ErrorState(
                                        error = state.error.asString(context),
                                        onRetry = {
                                            onAction(
                                                FacultyHomeAction.OnDaySelected(
                                                    state.selectedDay
                                                )
                                            )
                                        }
                                    )
                                }

                                // 3. Successfully fetched, but the timeline is empty or null (No Schedule)
                                state.scheduleInfo == null || state.scheduleInfo.timeline.isNullOrEmpty() -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "No schedule found for ${state.selectedDay}",
                                                color = Color(0xFF4B5563),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Tap to retry",
                                                color = Color(0xFF059669),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .clickable {
                                                        onAction(
                                                            FacultyHomeAction.OnDaySelected(
                                                                state.selectedDay
                                                            )
                                                        )
                                                    }
                                                    .padding(8.dp)
                                            )
                                        }
                                    }
                                }

                                else -> {
                                    AnimatedVisibility(
                                        visible = !state.isLoading,
                                        enter = fadeIn() + slideInHorizontally { it / 4 }
                                    ) {
                                        ScheduleTimeline(
                                            timeline = state.scheduleInfo.timeline,
                                            isEditable = false, // Read-only on Home Screen
                                            modifier = Modifier.padding(top = 16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(100.dp)) // padding for bottom nav
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberTimeBasedGreeting(): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        h < 12 -> "Good morning"
        h < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}