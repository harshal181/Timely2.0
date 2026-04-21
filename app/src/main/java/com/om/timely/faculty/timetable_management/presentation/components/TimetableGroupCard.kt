package com.om.timely.faculty.timetable_management.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.faculty.timetable_generation.domain.model.TimetableGroup
import com.om.timely.ui.theme.BrandColors

@Composable
fun TimetableGroupCard(
    group: TimetableGroup,
    onToggleActive: (Boolean) -> Unit,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = group.isActive
    val cardShape = RoundedCornerShape(24.dp)
    
    // Gradient border for active card
    val borderModifier = if (isActive) {
        Modifier.border(
            width = 2.dp,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF8B5CF6), Color(0xFFC084FC))
            ),
            shape = cardShape
        )
    } else Modifier

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(borderModifier),
        shape = cardShape,
        color = Color.White,
        shadowElevation = if (isActive) 8.dp else 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            // Header: Badge + Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isActive) {
                    Surface(
                        color = Color(0xFFF3E8FF),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF8B5CF6), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "ACTIVE",
                                color = Color(0xFF8B5CF6),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Switch(
                    checked = isActive,
                    onCheckedChange = onToggleActive,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF8B5CF6),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFE5E7EB),
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Name
            Text(
                text = group.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF111827)
            )

            // Department
            Text(
                text = group.dept,
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Semesters Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF6B7280)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Semesters",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4B5563)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Semester Chips
            val semesterList = group.semesters.sorted()
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(semesterList) { sem ->
                    Surface(
                        color = Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "Sem $sem",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(12.dp))

            // Metadata: Created & Updated Dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Created:", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    Text(group.createdAt.take(12), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B5563))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Updated:", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    Text(group.updatedAt.take(12), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B5563))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Export Button
            Button(
                onClick = onExportPdf,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Export as PDF",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
