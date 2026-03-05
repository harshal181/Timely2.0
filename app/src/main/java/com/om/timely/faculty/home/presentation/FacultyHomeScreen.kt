package com.om.timely.faculty.home.presentation

import android.widget.Toast
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import com.om.timely.faculty.home.presentation.components.FacultyHomeHeader
import com.om.timely.faculty.home.presentation.components.QuickStatsRow
import com.om.timely.faculty.home.presentation.components.TodayLoadBanner
import com.om.timely.faculty.home.presentation.components.timeline.ScheduleTimeline
import kotlinx.coroutines.flow.Flow

@Composable
fun FacultyHomeScreen(
    state: FacultyHomeState,
    onAction: (FacultyHomeAction) -> Unit,
    events: Flow<FacultyHomeEvent>
) {
    val context = LocalContext.current
    
    LaunchedEffect(key1 = true) {
        events.collect { event ->
            when (event) {
                is FacultyHomeEvent.ShowError -> {
                    Toast.makeText(context, event.error.asString(context), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Determine time-based greeting locally instead of passing through VM
    val greeting = rememberTimeBasedGreeting()

    Scaffold(
        containerColor = Color(0xFFF0FDF4),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Fixed Header
                Box(modifier = Modifier.padding(top = 44.dp)) {
                    FacultyHomeHeader(
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
                        item {
                            state.scheduleInfo?.dailyStats?.let { dailyStats ->
                                TodayLoadBanner(stats = dailyStats)
                            }
                        }

                        item {
                            state.scheduleInfo?.quickStats?.let { quickStats ->
                                QuickStatsRow(stats = quickStats)
                            }
                        }

                        @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                        stickyHeader {
                            Box(modifier = Modifier.background(Color(0xFFF0FDF4))) {
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
                                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowDropDown,
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
                                                        color = if (state.selectedSemester == sem) Color(0xFF059669) else Color(0xFF4B5563)
                                                    ) 
                                                },
                                                onClick = {
                                                    onAction(FacultyHomeAction.OnSemesterSelected(sem))
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
                                AnimatedVisibility(
                                    visible = !state.isLoading,
                                    enter = fadeIn() + slideInHorizontally { it / 4 }
                                ) {
                                    ScheduleTimeline(
                                        timeline = timeline,
                                        isEditable = true, // Read-only on Home Screen
                                        modifier = Modifier.padding(top = 16.dp)
                                    )
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
    val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        h < 12 -> "Good morning"
        h < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}
