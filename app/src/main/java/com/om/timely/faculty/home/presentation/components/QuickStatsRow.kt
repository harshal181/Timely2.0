package com.om.timely.faculty.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.faculty.home.domain.model.QuickStats
import com.om.timely.R

@Composable
fun QuickStatsRow(
    stats: QuickStats,
    onSubjectClick: () -> Unit,
    onBatchClick: () -> Unit = {},
    showValue: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickStatItem(
            modifier = Modifier
                .weight(1f)
                .clickable { onSubjectClick() },
            icon = Icons.Outlined.Book,
            label = "Subjects",
            value = stats.subjects.toString(),
            showValue = showValue,
            iconBgColor = Color(0xFFF5F3FF),
            iconColor = Color(0xFF7C3AED)
        )
        QuickStatItem(
            modifier = Modifier
                .weight(1f)
                .clickable { onBatchClick() },
            icon = ImageVector.vectorResource(R.drawable.ic_add_syllabus),
            label = "Syllabus",
            value = stats.batches.toString(),
            showValue = showValue,
            iconBgColor = Color(0xFFECFDF5),
            iconColor = Color(0xFF059669)
        )
    }
}

@Composable
private fun QuickStatItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    showValue: Boolean,
    iconBgColor: Color,
    iconColor: Color
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconBgColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                if (showValue) {
                    Text(
                        text = value,
                        color = Color(0xFF1F2937),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = label,
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp
                )
            }
        }
    }
}
