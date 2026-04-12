package com.om.timely.shared.notifications.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.om.timely.shared.notifications.domain.model.NotificationType
import com.om.timely.shared.notifications.presentation.NotificationAction
import com.om.timely.shared.notifications.presentation.SendNotificationFormState
import com.om.timely.ui.theme.BrandColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendNotificationBottomSheet(
    formState: SendNotificationFormState,
    onAction: (NotificationAction) -> Unit
) {
    var isTypeMenuExpanded by remember { mutableStateOf(false) }
    var isScopeMenuExpanded by remember { mutableStateOf(false) }

    val scopeOptions = listOf("All Students and Faculty", "Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5", "Semester 6", "Semester 7", "Semester 8")

    // skipPartiallyExpanded forces the sheet to open fully, preventing mid-way collapses
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isFormComplete = formState.title.isNotBlank() &&
            formState.message.isNotBlank() &&
            formState.selectedType != null

    ModalBottomSheet(
        onDismissRequest = { onAction(NotificationAction.OnToggleSendDialog(false)) },
        sheetState = sheetState,
        // Restoring the default drag handle helps the Compose gesture system differentiate
        // between dragging the sheet and scrolling the content inside.
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = BrandColors.BgWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        // Adding imePadding ensures the sheet behaves properly when the keyboard pops up
        modifier = Modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f) // Allows a slight gap at the top to see the screen behind
        ) {

            // 1. Neat, Minimalist Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "New Announcement",
                        style = MaterialTheme.typography.titleLarge,
                        color = BrandColors.TextDark,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Notify students and faculty",
                        style = MaterialTheme.typography.bodySmall,
                        color = BrandColors.TextGray
                    )
                }

                IconButton(
                    onClick = { onAction(NotificationAction.OnToggleSendDialog(false)) },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(BrandColors.CardGrayLight)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = BrandColors.TextDark,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Divider(color = BrandColors.BorderStandard, thickness = 1.dp, modifier = Modifier.padding(bottom = 8.dp))

            // 2. Scrollable Form Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // Title Field
                Column {
                    Text("Announcement Title", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = BrandColors.TextDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = formState.title,
                        onValueChange = { onAction(NotificationAction.OnTitleChanged(it)) },
                        placeholder = { Text("Enter title", color = BrandColors.TextLightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = BrandColors.CardGrayLight,
                            focusedContainerColor = BrandColors.CardGrayLight,
                            unfocusedBorderColor = Color.Transparent,
                        )
                    )
                }

                // Message Field
                Column {
                    Text("Message", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = BrandColors.TextDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = formState.message,
                        onValueChange = {
                            if (it.length <= 500) onAction(NotificationAction.OnMessageChanged(it))
                        },
                        placeholder = { Text("Write your message here...", color = BrandColors.TextLightGray) },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = BrandColors.CardGrayLight,
                            focusedContainerColor = BrandColors.CardGrayLight,
                            unfocusedBorderColor = Color.Transparent,
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${formState.message.length}/500 characters",
                        style = MaterialTheme.typography.bodySmall,
                        color = BrandColors.TextLightGray,
                        modifier = Modifier.align(Alignment.End)
                    )
                }

                // Type Dropdown
                Column {
                    Text("Announcement Type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = BrandColors.TextDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = isTypeMenuExpanded,
                        onExpandedChange = { isTypeMenuExpanded = !isTypeMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = formState.selectedType?.name?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Select Type",
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTypeMenuExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = BrandColors.CardGrayLight,
                                focusedContainerColor = BrandColors.CardGrayLight,
                                unfocusedBorderColor = Color.Transparent,
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = isTypeMenuExpanded,
                            onDismissRequest = { isTypeMenuExpanded = false },
                            modifier = Modifier.background(BrandColors.BgWhite)
                        ) {
                            NotificationType.values().forEach { type ->
                                val formattedText = type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                                DropdownMenuItem(
                                    text = { Text(formattedText) },
                                    onClick = {
                                        onAction(NotificationAction.OnTypeSelected(type))
                                        isTypeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Scope Dropdown
                Column {
                    Text("Send To", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = BrandColors.TextDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = isScopeMenuExpanded,
                        onExpandedChange = { isScopeMenuExpanded = !isScopeMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = formState.targetScope,
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isScopeMenuExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = BrandColors.CardGrayLight,
                                focusedContainerColor = BrandColors.CardGrayLight,
                                unfocusedBorderColor = Color.Transparent,
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = isScopeMenuExpanded,
                            onDismissRequest = { isScopeMenuExpanded = false },
                            modifier = Modifier.background(BrandColors.BgWhite)
                        ) {
                            scopeOptions.forEach { scope ->
                                DropdownMenuItem(
                                    text = { Text(scope) },
                                    onClick = {
                                        onAction(NotificationAction.OnTargetScopeSelected(scope))
                                        isScopeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 3. Conditional Live Preview Card
                if (isFormComplete) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BrandColors.CardGrayLight.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandColors.BorderStandard),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("LIVE PREVIEW", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BrandColors.PrimaryPurple)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = formState.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandColors.TextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = BrandColors.TextMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = BrandColors.CardPurpleLight
                                ) {
                                    Text(
                                        text = formState.selectedType?.name?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "",
                                        color = BrandColors.PrimaryPurple,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("•", color = BrandColors.TextGray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(formState.targetScope, style = MaterialTheme.typography.labelSmall, color = BrandColors.TextGray)
                            }
                        }
                    }
                }
            } // End of scroll

            // 4. Sticky Bottom Send Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandColors.BgWhite)
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp)
            ) {
                Button(
                    onClick = { onAction(NotificationAction.OnSendNotificationClicked) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandColors.PrimaryPurple,
                        disabledContainerColor = BrandColors.BorderStandard
                    ),
                    enabled = isFormComplete
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Announcement", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}