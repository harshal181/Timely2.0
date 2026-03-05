package com.om.timely.splash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.timely.auth.domain.AuthRepository
import com.om.timely.auth.domain.UserRole
import com.om.timely.core.domain.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val authRepository: AuthRepository
): ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private val _navigateToNext = MutableStateFlow<String?>(null)
    val navigateToNext = _navigateToNext.asStateFlow()

    init {
        checkAuth()
    }

    private fun checkAuth() {
        viewModelScope.launch {
            val result = authRepository.checkAuthStatus()
            if (result is Result.Success) {
                // Token valid, route by role
                when (result.data.role) {
                    UserRole.STUDENT -> _navigateToNext.value = "student_graph"
                    UserRole.FACULTY -> _navigateToNext.value = "faculty_graph"
                }
            } else {
                // Show login screen graph
                _navigateToNext.value = "auth_graph"
            }
            _isReady.value = true
        }
    }
}
