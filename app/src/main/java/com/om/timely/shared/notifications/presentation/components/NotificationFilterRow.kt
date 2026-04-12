package com.om.timely.shared.notifications.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.om.timely.shared.notifications.presentation.NotificationFilter
import com.om.timely.ui.theme.BrandColors

/**
 * A horizontally scrollable row of filter chip buttons.
 *
 * The [filters] list is passed from the parent so that students receive only
 * [StudentNotificationFilter] values and faculty receive [FacultyNotificationFilter] values —
 * both satisfy the [NotificationFilter] sealed interface.
 */
@Composable
fun NotificationFilterRow(
    filters: List<NotificationFilter>,
    activeFilter: NotificationFilter,
    onFilterSelected: (NotificationFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = filter == activeFilter
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = if (isSelected) BrandColors.PrimaryPurple else Color.Transparent,
                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, BrandColors.BorderStandard) else null,
                modifier = Modifier.clickable { onFilterSelected(filter) }
            ) {
                Text(
                    text = filter.label,
                    color = if (isSelected) Color.White else BrandColors.TextGray,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
    }
}
