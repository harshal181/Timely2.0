package com.om.timely.auth.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.auth.domain.UserRole

@Composable
fun RoleToggleSegment(
    modifier: Modifier = Modifier,
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    // Background animation value based on selection
    val sliderOffset by animateFloatAsState(
        targetValue = if (selectedRole == UserRole.STUDENT) 0f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "slider_offset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFFF3F4F6), RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        // Slider background
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val segmentWidth = maxWidth / 2
            
            Box(
                modifier = Modifier
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .offset(x = segmentWidth * sliderOffset)
                    .background(Color(0xFF7C3AED), RoundedCornerShape(12.dp))
            )

            Row(modifier = Modifier.fillMaxSize()) {
                // Student Option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onRoleSelected(UserRole.STUDENT) },
                    contentAlignment = Alignment.Center
                ) {
                    val isSelected = selectedRole == UserRole.STUDENT
                    Text(
                        text = "Student",
                        color = if (isSelected) Color.White else Color(0xFF6B7280),
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Faculty Option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onRoleSelected(UserRole.FACULTY) },
                    contentAlignment = Alignment.Center
                ) {
                    val isSelected = selectedRole == UserRole.FACULTY
                    Text(
                        text = "Faculty",
                        color = if (isSelected) Color.White else Color(0xFF6B7280),
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
