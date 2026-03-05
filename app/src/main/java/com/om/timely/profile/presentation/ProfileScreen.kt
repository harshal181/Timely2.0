package com.om.timely.profile.presentation

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.GeneratingTokens
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.auth.domain.UserRole
import com.om.timely.profile.domain.model.FullProfile
import com.om.timely.profile.presentation.components.ChangePasswordBottomSheet
import com.om.timely.profile.presentation.components.EditProfileBottomSheet
import com.om.timely.profile.presentation.components.ProfileActionItem
import com.om.timely.profile.presentation.components.ProfileCard
import com.om.timely.profile.presentation.components.ProfileHeader
import com.om.timely.profile.presentation.components.ProfileInfoItem

@Composable
fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
    onNavigateToAuth: () -> Unit
) {


    val studentGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF4C1D95), Color(0xFF7C3AED))
    )
    val facultyGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF064E3B), Color(0xFF059669))
    )
    val screenBackground = Color(0xFFF8F7FF)


    Scaffold(
        containerColor = screenBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (state.profile?.role == UserRole.FACULTY && !state.isLoading) {
                FloatingActionButton(
                    onClick = { /* Action */ },
                    containerColor = Color(0xFF059669),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.GeneratingTokens, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Generate", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->

        if (state.isEditSheetVisible) {
            EditProfileBottomSheet(
                role = state.profile?.role ?: UserRole.STUDENT,
                editState = state.editProfileState, // State held in ViewModel
                onEditStateChange = { onAction(ProfileAction.OnEditProfileDataChange(it)) },
                onDismiss = { onAction(ProfileAction.ToggleEditSheet(false)) },
                onSave = { onAction(ProfileAction.OnSaveProfile) }
            )
        }
        if (state.isChangePasswordSheetVisible) {
            ChangePasswordBottomSheet(
                state = state.changePasswordState,
                onPasswordChange = { new, confirm ->
                    onAction(ProfileAction.OnPasswordDataChange(new, confirm))
                },
                onDismiss = { onAction(ProfileAction.TogglePasswordSheet(false)) },
                onConfirm = { onAction(ProfileAction.OnUpdatePassword) }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.profile != null -> {
                    ProfileContent(
                        profile = state.profile,
                        isNotificationsEnabled = state.isNotificationsEnabled,
                        backgroundGradient = if (state.profile.role == UserRole.STUDENT) studentGradient else facultyGradient,
                        onAction = onAction,
                        onSignOutClick = onNavigateToAuth,
                        // Pass paddingValues here if you want the bottom content to respect the Nav Bar height
                        modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    profile: FullProfile,
    isNotificationsEnabled: Boolean,
    backgroundGradient: Brush,
    onAction: (ProfileAction) -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val themeColor = if (profile.role == UserRole.STUDENT) Color(0xFF7C3AED) else Color(0xFF059669)


    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Header starts from the very top
        val initials = profile.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()
        val roleBadge = if (profile.role == UserRole.STUDENT) "Student" else "Faculty. CS Dept."
        val roleIcon = if (profile.role == UserRole.STUDENT) Icons.Outlined.School else Icons.Outlined.Badge

        ProfileHeader(
            name = profile.name,
            initials = initials,
            roleBadgeText = roleBadge,
            roleIcon = roleIcon,
            backgroundGradient = backgroundGradient
        )

        // Main Body Padding
        Column(modifier = Modifier.padding(16.dp)) {

            // Personal Information
            ProfileCard(title = "Personal Information") {
                ProfileInfoItem(Icons.Outlined.Email, "Email", profile.email, themeColor)

                if (profile.role == UserRole.STUDENT && profile.studentDetails != null) {
                    val details = profile.studentDetails
                    ProfileInfoItem(Icons.Outlined.Apartment, "Department", details.course, themeColor)
                    ProfileInfoItem(Icons.Outlined.AccountCircle, "Student ID", details.studId, themeColor)
                    ProfileInfoItem(Icons.Outlined.Description, "Semester", "Semester ${details.semester} \u00B7 Div ${details.division}", themeColor)
                } else if (profile.role == UserRole.FACULTY && profile.facultyDetails != null) {
                    val details = profile.facultyDetails
                    ProfileInfoItem(Icons.Outlined.Apartment, "Office", details.office, themeColor)
                    ProfileInfoItem(Icons.Outlined.Badge, "Faculty ID", details.facId, themeColor)
                    ProfileInfoItem(Icons.Outlined.WorkOutline, "Designation", details.designation, themeColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Account Settings
            ProfileCard(title = "Account Settings") {
                ProfileActionItem(
                    icon = Icons.Outlined.Edit,
                    label = "Edit Profile",
                    iconTint = themeColor,
                    onClick = { onAction(ProfileAction.OnEditProfileClick) },
                    trailingContent = { Icon(Icons.Outlined.ChevronRight, null, tint = Color.LightGray) }
                )
                ProfileActionItem(
                    icon = Icons.Outlined.Lock,
                    label = "Change Password",
                    iconTint = Color(0xFF2563EB),
                    showDivider = false,
                    onClick = { onAction(ProfileAction.OnChangePasswordClick) },
                    trailingContent = { Icon(Icons.Outlined.ChevronRight, null, tint = Color.LightGray) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preferences
            ProfileCard(title = "Preferences") {
                ProfileActionItem(
                    icon = Icons.Outlined.Notifications,
                    label = "Notifications",
                    iconTint = themeColor,
                    showDivider = false,
                    trailingContent = {
                        Switch(
                            checked = isNotificationsEnabled,
                            onCheckedChange = { onAction(ProfileAction.OnToggleNotifications(it)) },
                            colors = SwitchDefaults.colors(checkedTrackColor = themeColor)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Red Sign Out Button
            Button(
                onClick = { onSignOutClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, Color(0xFFFECACA))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Outlined.ExitToApp, null, tint = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", color = Color(0xFFEF4444), fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
        }
    }
}