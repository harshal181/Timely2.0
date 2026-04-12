package com.om.timely.faculty.edit.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.profile.domain.model.ProfileSubject

@Composable
fun AssignedSubjectChip(subject: ProfileSubject) {
    Surface(
        color = Color(subject.colorArgb).copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Color(subject.colorArgb).copy(alpha = 0.2f)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Round Color Pip
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(subject.colorArgb)),
                contentAlignment = Alignment.Center
            ) {
                // Abbreviation usually pulled from Code or name logic—in absence we use abbreviation logic
                val abbrev = subject.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2).uppercase()
                Text(
                    text = abbrev,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "${subject.code} • ${subject.name}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}
