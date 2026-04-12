package com.om.timely.faculty.edit.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EditDaySelector(
    selectedDay: String,
    activeDayCount: Int,
    onDayChange: (String) -> Unit
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        items(days) { day ->
            val isSelected = day == selectedDay
            // Mocking counts: selected day gets real count, others get 4 or 2
            val count = if (isSelected) activeDayCount else if (day == "Sat") 2 else 4
            
            val interactionSource = remember { MutableInteractionSource() }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .size(width = 56.dp, height = 56.dp)
                    .then(
                        if (isSelected) {
                            Modifier
                                .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF7C3AED).copy(alpha = 0.5f))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF7C3AED), Color(0xFF6366F1))
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                        } else {
                            Modifier
                                .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.05f))
                                .background(Color.White, RoundedCornerShape(20.dp))
                        }
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = interactionSource, 
                        indication = null,
                        onClick = { onDayChange(day) }
                    )
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = day,
                    color = if (isSelected) Color.White else Color(0xFF6B7280),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
