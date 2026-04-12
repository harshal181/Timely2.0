package com.om.timely.faculty.subjects.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.core.presentation.util.toDepartmentAbbreviation
import com.om.timely.faculty.subjects.domain.model.Subject

@Composable
fun SubjectCard(
    subject: Subject,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subjectColor = Color(subject.colorArgb)
    
    // We derive lighter tints for the background badges and icons
    // For simplicity, we can use a hardcoded alpha tint overlay for backgrounds 
    val badgeBgColor = subjectColor.copy(alpha = 0.15f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            
            // Abbreviation Badge
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(badgeBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subject.abbreviation,
                    color = subjectColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subject.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827) // Dark gray
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = subject.code,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280) // Muted text
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Semester and Department pills
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PillBadge(text = "Sem ${subject.semester}")
                    PillBadge(text = subject.department.toDepartmentAbbreviation())
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Actions Row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Edit Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF3E8FF)) // Light Purple Bg
                        .clickable { onEditClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF7C3AED), // Purple
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFEE2E2)) // Light Red Bg
                        .clickable { onDeleteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444), // Red
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PillBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFF3F4F6)) // Light gray background
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF4B5563),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
