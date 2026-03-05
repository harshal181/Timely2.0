package com.om.timely.auth.presentation.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.om.timely.auth.presentation.components.AuthButton
import com.om.timely.auth.presentation.components.AuthHeader
import com.om.timely.auth.presentation.components.AuthTextField
import kotlinx.coroutines.flow.Flow

@Composable
fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
    events: Flow<LoginEvent>,
    onLoginSuccess: (String) -> Unit,
    onNavigateSignup: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = true) {
        events.collect { event ->
            when (event) {
                is LoginEvent.LoginSuccess -> onLoginSuccess(event.route)
                is LoginEvent.LoginError -> {
                    Toast.makeText(context, event.error.asString(context), Toast.LENGTH_LONG).show()
                }
                LoginEvent.NavigateToSignup -> onNavigateSignup()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF9FAFB),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            AuthHeader(
                title = "Welcome back!",
                subtitle = "Sign in to your Timely account"
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 32.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                AuthTextField(
                    value = state.emailOrId,
                    onValueChange = { onAction(LoginAction.OnEmailOrIdChanged(it)) },
                    label = "Email / ID",
                    hint = "Email or ID",
                    leadingIcon = Icons.Outlined.Email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    isError = !state.isEmailOrIdValid,
                    errorMessage = "Email or ID cannot be empty"
                )

                AuthTextField(
                    value = state.password,
                    onValueChange = { onAction(LoginAction.OnPasswordChanged(it)) },
                    label = "Password",
                    hint = "Enter your password",
                    leadingIcon = Icons.Outlined.Lock,
                    trailingIcon = {
                        IconButton(onClick = { onAction(LoginAction.OnTogglePasswordVisibility) }) {
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
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onAction(LoginAction.OnLoginClick)
                        }
                    ),
                    isError = !state.isPasswordValid,
                    errorMessage = "Password must be at least 6 characters"
                )

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = { /* TODO: Forgot Password */ }) {
                        Text(
                            text = "Forgot Password?",
                            color = Color(0xFF7C3AED),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }

                AuthButton(
                    text = "Sign In",
                    isLoading = state.isLoading,
                    onClick = {
                        focusManager.clearFocus()
                        onAction(LoginAction.OnLoginClick)
                    },
                    modifier = Modifier.padding(top = 8.dp)
                )


            }
            if (state.error != null) {
                Text(
                    text = state.error.asString(context),
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontSize = 14.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New here?",
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp
                )
                TextButton(onClick = {
                    onAction(LoginAction.OnSignupClick)
                    onNavigateSignup()
                }) {
                    Text(
                        text = "Create an account",
                        color = Color(0xFF7C3AED),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
