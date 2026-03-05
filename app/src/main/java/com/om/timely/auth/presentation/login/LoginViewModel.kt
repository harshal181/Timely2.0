package com.om.timely.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.timely.auth.domain.AuthRepository
import com.om.timely.auth.domain.UserRole
import com.om.timely.auth.domain.error.asUiText
import com.om.timely.core.domain.util.Result
import com.om.timely.core.presentation.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()


    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.OnEmailOrIdChanged -> {
                _state.update { it.copy(
                    emailOrId = action.value,
                    isEmailOrIdValid = action.value.isNotBlank(),
                    error = null
                ) }
            }
            is LoginAction.OnPasswordChanged -> {
                _state.update { it.copy(
                    password = action.value,
                    isPasswordValid = action.value.length >= 6,
                    error = null
                ) }
            }
            LoginAction.OnTogglePasswordVisibility -> {
                _state.update { it.copy(
                    isPasswordVisible = !it.isPasswordVisible
                ) }
            }
            LoginAction.OnLoginClick -> {
                login()
            }

            LoginAction.OnSignupClick -> {
                viewModelScope.launch {
                    _events.send(LoginEvent.NavigateToSignup)
                }
            }
        }
    }

    private fun login() {
        val emailOrId = state.value.emailOrId
        val password = state.value.password

        val isEmailOrIdValid = emailOrId.isNotBlank()
        val isPasswordValid = password.length >= 6

        if (!isEmailOrIdValid || !isPasswordValid) {
            _state.update { it.copy(
                isEmailOrIdValid = isEmailOrIdValid,
                isPasswordValid = isPasswordValid,
                error = UiText.DynamicString("Please fill in all fields correctly")
            ) }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            
            when (val result = authRepository.login(identifier = emailOrId, password = password)) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    val route = when (result.data.role) {
                        UserRole.STUDENT -> "student_graph"
                        UserRole.FACULTY -> "faculty_graph"
                    }
                    _events.send(LoginEvent.LoginSuccess(route))
                }
                is Result.Error -> {
                    val errorMessage = result.error.asUiText()
                    _state.update { it.copy(isLoading = false, error = errorMessage) }
                    _events.send(LoginEvent.LoginError(errorMessage))
                }
            }
        }
    }
}
