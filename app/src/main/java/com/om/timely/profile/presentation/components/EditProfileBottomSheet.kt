package com.om.timely.profile.presentation.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.auth.domain.UserRole
import com.om.timely.profile.presentation.EditProfileState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileBottomSheet(
    role: UserRole, // Pass the role here
    editState: EditProfileState,
    onEditStateChange: (EditProfileState) -> Unit,
    onDismiss: () -> Unit,
    onSave: (EditProfileState) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray.copy(alpha = 0.5f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header (Remains the same)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Edit Profile", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1F2937))
                    Text("Modify your profile details below", fontSize = 14.sp, color = Color.Gray)
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(Color(0xFFF3F4F6), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Common Field: Full Name
            EditField(label = "Full Name", value = editState.name, icon = Icons.Outlined.Person) {
                onEditStateChange(editState.copy(name = it))
            }

            // Role-Based Fields
            if (role == UserRole.STUDENT) {
                EditField(label = "Department", value = editState.department, icon = Icons.Outlined.Apartment) {
                    onEditStateChange(editState.copy(department = it))
                }

                // Fixed: Added spacedBy(12.dp) for the gap between Semester and Division
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        EditField(label = "Semester", value = editState.semester, icon = Icons.Outlined.School) {
                            onEditStateChange(editState.copy(semester = it))
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        EditField(label = "Division", value = editState.division, icon = Icons.Outlined.Description) {
                            onEditStateChange(editState.copy(division = it))
                        }
                    }
                }
            } else {
                // Faculty specific fields
                EditField(label = "Office", value = editState.office, icon = Icons.Outlined.Apartment) {
                    onEditStateChange(editState.copy(office = it))
                }

                EditField(label = "Designation", value = editState.designation, icon = Icons.Outlined.WorkOutline) {
                    onEditStateChange(editState.copy(designation = it))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = { onSave(editState) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Outlined.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
@Composable
fun EditField(label: String, value: String, icon: ImageVector, onValueChange: (String) -> Unit) {
    Column (modifier = Modifier.padding(bottom = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF9FAFB),
                focusedContainerColor = Color.White,
                unfocusedIndicatorColor = Color(0xFFE5E7EB)
            )
        )
    }
}