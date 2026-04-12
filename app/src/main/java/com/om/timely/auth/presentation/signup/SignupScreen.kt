package com.om.timely.auth.presentation.signup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.auth.domain.UserRole
import com.om.timely.auth.presentation.components.AuthButton
import com.om.timely.auth.presentation.components.AuthDropdown
import com.om.timely.auth.presentation.components.AuthHeader
import com.om.timely.auth.presentation.components.AuthTextField
import com.om.timely.auth.presentation.components.RoleToggleSegment
import kotlinx.coroutines.flow.Flow

@Composable
fun SignupScreen(
    state: SignupState,
    onAction: (SignupAction) -> Unit,
    events: Flow<SignupEvent>,
    onSignupSuccess: (String) -> Unit,
    onNavigateLogin: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        events.collect { event ->
            when (event) {
                is SignupEvent.SignupSuccess -> onSignupSuccess(event.route)
                is SignupEvent.SignupError -> {
                    // Show backend errors (email exists, network failure, etc.) as a Snackbar
                    snackbarHostState.showSnackbar(event.error.asString(context))
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF9FAFB),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            AuthHeader(
                title = "Create Account",
                subtitle = "Join Timely today"
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name Field
                AuthTextField(
                    value = state.fullName,
                    onValueChange = { onAction(SignupAction.OnNameChanged(it)) },
                    label = "Full Name",
                    hint = "Your full name",
                    leadingIcon = Icons.Outlined.AccountCircle,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                // Email Field
                AuthTextField(
                    value = state.email,
                    onValueChange = { onAction(SignupAction.OnEmailChanged(it)) },
                    label = "Email Address",
                    hint = "you@college.edu",
                    leadingIcon = Icons.Outlined.Email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                // Password Field
                AuthTextField(
                    value = state.password,
                    onValueChange = { onAction(SignupAction.OnPasswordChanged(it)) },
                    label = "Password",
                    hint = "Min. 8 characters",
                    leadingIcon = Icons.Outlined.Lock,
                    trailingIcon = {
                        IconButton(onClick = { onAction(SignupAction.OnTogglePasswordVisibility) }) {
                            Icon(
                                imageVector = if (state.isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = "Toggle password visibility",
                                tint = Color(0xFF9CA3AF)
                            )
                        }
                    },
                    visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    )
                )

                // Role Toggle
                Text(
                    text = "I am a...",
                    color = Color(0xFF111827),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                RoleToggleSegment(
                    selectedRole = state.selectedRole,
                    onRoleSelected = { onAction(SignupAction.OnRoleChanged(it)) }
                )

                // Conditional Department / Course Fields
                if (state.selectedRole == UserRole.STUDENT) {
                    AuthTextField(
                        value = state.studentId,
                        onValueChange = { onAction(SignupAction.OnStudentIdChanged(it)) },
                        label = "Student ID",
                        hint = "e.g. 21IT054",
                        leadingIcon = Icons.Outlined.School,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
                    )

                    AuthDropdown(
                        label = "Department",
                        hint = "Select department",
                        selectedValue = state.department,
                        options = listOf("Computer Science", "Information Technology", "Electronics", "Mechanical"),
                        isExpanded = state.isDepartmentDropdownExpanded,
                        onExpandedChange = { onAction(SignupAction.OnToggleDepartmentDropdown(it)) },
                        onOptionSelected = { onAction(SignupAction.OnDepartmentChanged(it)) }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AuthTextField(
                            value = state.semester,
                            onValueChange = { onAction(SignupAction.OnSemesterChanged(it)) },
                            label = "Semester",
                            hint = "1",
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                        )
                        AuthTextField(
                            value = state.division,
                            onValueChange = { onAction(SignupAction.OnDivisionChanged(it)) },
                            label = "Division",
                            hint = "A",
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )
                    }

                    AuthDropdown(
                        label = "Batch",
                        hint = "Select batch",
                        selectedValue = state.batch,
                        options = listOf("Batch 1", "Batch 2"),
                        isExpanded = state.isBatchDropdownExpanded,
                        onExpandedChange = { onAction(SignupAction.OnToggleBatchDropdown(it)) },
                        onOptionSelected = { onAction(SignupAction.OnBatchChanged(it)) }
                    )

                } else {
                    AuthTextField(
                        value = state.facultyId,
                        onValueChange = { onAction(SignupAction.OnFacultyIdChanged(it)) },
                        label = "Faculty ID",
                        hint = "e.g. FAC001",
                        leadingIcon = Icons.Outlined.School,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
                    )
                    AuthTextField(
                        value = state.designation,
                        onValueChange = { onAction(SignupAction.OnDesignationChanged(it)) },
                        label = "Designation",
                        hint = "e.g. Assistant Professor",
                        leadingIcon = Icons.Outlined.School,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
                    )
                    AuthDropdown(
                        label = "Department / Office",
                        hint = "Select department",
                        selectedValue = state.department,
                        options = listOf("Computer Engineering", "Civil Engineering", "Electronics", "Mechanical"),
                        isExpanded = state.isDepartmentDropdownExpanded,
                        onExpandedChange = { onAction(SignupAction.OnToggleDepartmentDropdown(it)) },
                        onOptionSelected = { onAction(SignupAction.OnDepartmentChanged(it)) }
                    )
                }

                // Inline validation error — for local field validation only (name blank, bad format, etc.)
                // Backend errors are shown as Snackbars via the event channel, not here.
                if (state.error != null) {
                    Text(
                        text = state.error.asString(context),
                        color = Color(0xFFDC2626),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                AuthButton(
                    text = "Create Account",
                    isLoading = state.isLoading,
                    onClick = {
                        focusManager.clearFocus()
                        onAction(SignupAction.OnSignupClick)
                    },
                    modifier = Modifier.padding(top = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 32.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account?",
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp
                    )
                    TextButton(onClick = {
                        onAction(SignupAction.OnSignInClick)
                        onNavigateLogin()
                    }) {
                        Text(
                            text = "Sign In",
                            color = Color(0xFF7C3AED),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
