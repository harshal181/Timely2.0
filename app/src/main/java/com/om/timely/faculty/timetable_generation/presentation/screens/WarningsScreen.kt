package com.om.timely.faculty.timetable_generation.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.faculty.timetable_generation.presentation.TimetableGenerationAction
import com.om.timely.faculty.timetable_generation.presentation.TimetableGenerationState
import com.om.timely.ui.theme.BrandColors

@Composable
fun WarningsScreen(
    state: TimetableGenerationState,
    onAction: (TimetableGenerationAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = "Warning",
                tint = BrandColors.CardYellowLight,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Solver Warnings",
                color = BrandColors.TextDark,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "The generation was successful, but the CP Solver encountered some rule relaxations.",
            color = BrandColors.TextGray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.warnings) { warning ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandColors.BgWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "•",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandColors.PrimaryPurple
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = warning,
                            fontSize = 15.sp,
                            color = BrandColors.TextMedium,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


    }
}
