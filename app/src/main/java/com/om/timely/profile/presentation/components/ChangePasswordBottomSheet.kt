package com.om.timely.profile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.profile.presentation.ChangePasswordState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordBottomSheet(
    state: ChangePasswordState,
    onPasswordChange: (String, String) -> Unit, // Passes new and confirm for validation
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    // Local state for the confirmation field specifically
    var confirmPassword by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray.copy(alpha = 0.5f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Security", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1F2937))
                    Text("Update your account password", fontSize = 14.sp, color = Color.Gray)
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(Color(0xFFF3F4F6), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Current Password
            EditField(
                label = "Current Password",
                value = state.oldPassword,
                icon = Icons.Outlined.Lock
            ) {
                onPasswordChange(it, confirmPassword)
            }

            // New Password
            EditField(
                label = "New Password",
                value = state.newPassword,
                icon = Icons.Outlined.LockOpen
            ) {
                onPasswordChange(it, confirmPassword)
            }

            // Confirm New Password (UI only check)
            EditField(
                label = "Confirm New Password",
                value = confirmPassword,
                icon = Icons.Outlined.Shield
            ) {
                confirmPassword = it
                onPasswordChange(state.newPassword, it)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Update Button
            val isMatching = state.newPassword == confirmPassword && state.newPassword.isNotEmpty()

            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isMatching,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB), // Blue for Security
                    disabledContainerColor = Color(0xFFE5E7EB)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Update Password", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}