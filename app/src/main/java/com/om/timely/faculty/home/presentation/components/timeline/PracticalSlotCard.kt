package com.om.timely.faculty.home.presentation.components.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.R
import com.om.timely.faculty.home.domain.model.schedule.PracticalSlot

@Composable
fun PracticalSlotCard(
    slot: PracticalSlot,
    isEditable: Boolean = false,
    onEditClick: (() -> Unit)? = null
) {
    val accentColor = Color(slot.colorArgb)
    val bgColor = accentColor.copy(alpha = 0.1f) // Slightly darker than lecture
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
//            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f))
            .clip(RoundedCornerShape(16.dp))
            .drawBehind {
                drawRect(color = bgColor)
                drawRect(
                    color = accentColor,
                    size = androidx.compose.ui.geometry.Size(6.dp.toPx(), size.height)
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            // Header: Icon, Title, Time, Edit
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(accentColor, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_code), // Or Beaker placeholder
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = slot.title.uppercase(),
                        color = accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${slot.startTime} \u2013 ${slot.endTime}",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                }

                if (isEditable && onEditClick != null) {
                    val interactionSource = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(50))
                            .clickable(interactionSource = interactionSource, indication = null) {
                                onEditClick()
                            }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Edit",
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }


//            // Dashed Divider exactly like Figma
//            DashedDivider(color = accentColor.copy(alpha = 0.4f))

//            Spacer(modifier = Modifier.height(12.dp))

            // Batches Column
            slot.batches.forEachIndexed { index, batch ->
                Column(modifier = Modifier.padding(start = 4.dp)) {
                    // Batch Chip
                    Box(
                        modifier = Modifier
                            .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = batch.batchName,
                            color = accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = batch.subjectName,
                        color = Color(0xFF1F2937),
                        fontSize = if (batch.subjectName.length > 20) 12.sp else 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        lineHeight = if (batch.subjectName.length > 20) 16.sp else 18.sp,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Outlined.PersonOutline,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(12.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = batch.facultyName,
                                color = Color(0xFF475569),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(12.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = batch.roomNo,
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                if (index < slot.batches.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                    DashedDivider(color = accentColor.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun DashedDivider(
    color: Color = Color.Gray,
    thickness: androidx.compose.ui.unit.Dp = 1.dp,
    dashLength: Float = 10f,
    spacingLength: Float = 10f
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(thickness)
            .drawBehind {
                drawLine(
                    color = color,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = thickness.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, spacingLength), 0f)
                )
            }
    )
}
