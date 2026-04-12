package com.om.timely.auth.presentation.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.timely.auth.domain.AuthRepository
import com.om.timely.auth.domain.UserRole
import com.om.timely.auth.domain.error.asUiText
import com.om.timely.core.domain.util.Result
import com.om.timely.core.service.FcmTopicManager
import com.om.timely.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.OnEmailOrIdChanged -> _state.update {
                it.copy(emailOrId = action.value, isEmailOrIdValid = action.value.isNotBlank())
            }
            is LoginAction.OnPasswordChanged -> _state.update {
                it.copy(password = action.value, isPasswordValid = action.value.length >= 6)
            }
            LoginAction.OnTogglePasswordVisibility -> _state.update {
                it.copy(isPasswordVisible = !it.isPasswordVisible)
            }
            LoginAction.OnLoginClick -> login()
            LoginAction.OnSignupClick -> viewModelScope.launch {
                _events.send(LoginEvent.NavigateToSignup)
            }
        }
    }

    private fun login() {
        val emailOrId = state.value.emailOrId
        val password  = state.value.password

        val isEmailOrIdValid = emailOrId.isNotBlank()
        val isPasswordValid  = password.length >= 6

        if (!isEmailOrIdValid || !isPasswordValid) {
            _state.update { it.copy(isEmailOrIdValid = isEmailOrIdValid, isPasswordValid = isPasswordValid) }
            return
        }

        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            when (val result = authRepository.login(identifier = emailOrId, password = password)) {
                is Result.Success -> {
                    val route = when (result.data.role) {
                        UserRole.STUDENT -> "student_graph"
                        UserRole.FACULTY -> "faculty_graph"
                    }
                    // Subscribe to FCM topics immediately — covers the current session.
                    // SplashViewModel handles cold starts when already logged in.
                    subscribeToFcmTopics()
                    _state.update { it.copy(isLoading = false) }
                    _events.send(LoginEvent.LoginSuccess(route))
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(LoginEvent.LoginError(result.error.asUiText()))
                }
            }
        }
    }

    /**
     * Fetches the profile and subscribes to FCM topics.
     * The real dept string is logged; FcmTopicManager.buildTopicsForProfile reads profile.dept.
     */
    private suspend fun subscribeToFcmTopics() {
        when (val profileResult = profileRepository.getProfile()) {
            is Result.Success -> {
                val dept = profileResult.data.dept
                Log.d("FCM_TOPICS", "Login: profile loaded, dept=\"$dept\" — subscribing to topics")
                FcmTopicManager.subscribeForProfile(profileResult.data)
            }
            is Result.Error -> Log.w(
                "FCM_TOPICS",
                "Login: profile fetch failed — FCM subscription skipped for this session. ${profileResult.error}"
            )
        }
    }
}
