package com.om.timely.faculty.subjects.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun DeleteSubjectDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = Color.White,
        titleContentColor = Color(0xFF1F2937),
        textContentColor = Color(0xFF4B5563),
        title = {
            Text(
                text = "Delete Subject?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete this subject? This action cannot be undone."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC2626), // Red
                    contentColor = Color.White
                )
            ) {
                Text(text = "Delete", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
            ) {
                Text(text = "Cancel", color = Color(0xFF1F2937))
            }
        }
    )
}
