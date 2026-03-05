package com.om.timely.faculty.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.faculty.home.presentation.getTodayDayOfWeek
import kotlinx.coroutines.launch
import java.util.Calendar

private val DAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

private fun getCurrentWeekDates(): Map<String, String> {
    val calendar = Calendar.getInstance()
    // Find the Monday of the current week
    while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
        calendar.add(Calendar.DAY_OF_MONTH, -1)
    }
    
    val dates = mutableMapOf<String, String>()
    for (day in DAYS) {
        dates[day] = calendar.get(Calendar.DAY_OF_MONTH).toString()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }
    return dates
}

@Composable
fun DaySelector(
    selectedDay: String,
    onDayChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val currentIndex = DAYS.indexOf(selectedDay)

    val todayName = remember { getTodayDayOfWeek() }
    val currentWeekDates = remember { getCurrentWeekDates() }

    LaunchedEffect(selectedDay) {
        val index = DAYS.indexOf(selectedDay)
        if (index != -1) {
            listState.animateScrollToItem(index)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Arrow
        ArrowButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            enabled = currentIndex > 0,
            onClick = {
                if (currentIndex > 0) onDayChange(DAYS[currentIndex - 1])
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Days Scroll List
        LazyRow(
            state = listState,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            itemsIndexed(DAYS) { index, day ->
                val isSelected = day == selectedDay
                val isToday = day == todayName
                
                DayItem(
                    dayInfo = day,
                    dateInfo = currentWeekDates[day] ?: "",
                    isSelected = isSelected,
                    isToday = isToday,
                    onClick = { onDayChange(day) }
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right Arrow
        ArrowButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            enabled = currentIndex < DAYS.size - 1,
            onClick = {
                if (currentIndex < DAYS.size - 1) onDayChange(DAYS[currentIndex + 1])
            }
        )
    }
}

@Composable
private fun ArrowButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val bgColor = if (enabled) Color(0xFFEDE9FE) else Color(0xFFF3F4F6)
    val iconColor = if (enabled) Color(0xFF7C3AED) else Color(0xFFD1D5DB)

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun DayItem(
    dayInfo: String,
    dateInfo: String,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val backgroundModifier = if (isSelected) {
        Modifier
            .shadow(12.dp, RoundedCornerShape(14.dp), spotColor = Color(0xFF7C3AED).copy(alpha = 0.4f))
            .background(
                brush = Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF6366F1))),
                shape = RoundedCornerShape(14.dp)
            )
    } else if (isToday) {
        Modifier.background(Color(0xFFF5F3FF).copy(alpha = 0.8f), RoundedCornerShape(14.dp))
    } else {
        Modifier.background(Color.Transparent, RoundedCornerShape(14.dp))
    }

    Column(
        modifier = Modifier
            .width(62.dp)
            .then(backgroundModifier)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayInfo.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color(0xFF9CA3AF),
            letterSpacing = 0.5.sp
        )
        Text(
            text = dateInfo,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else if (isToday) Color(0xFF7C3AED) else Color(0xFF374151),
            modifier = Modifier.padding(top = 4.dp, bottom = if (isToday && !isSelected) 2.dp else 4.dp)
        )
        if (isToday && !isSelected) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF7C3AED))
            )
        }
    }
}
