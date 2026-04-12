package com.om.timely.splash.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.timely.auth.domain.AuthRepository
import com.om.timely.auth.domain.UserRole
import com.om.timely.core.domain.AuthPreferences
import com.om.timely.core.domain.util.Result
import com.om.timely.core.service.FcmTopicManager
import com.om.timely.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private val _navigateToNext = MutableStateFlow<String?>(null)
    val navigateToNext = _navigateToNext.asStateFlow()

    init {
        checkAuth()
    }

    private fun checkAuth() {
        viewModelScope.launch {
            Log.d("FCM_TOPICS", "── SplashViewModel: checking auth status ──")
            val result = authRepository.checkAuthStatus()

            if (result is Result.Success) {
                val role = result.data.role
                Log.i("FCM_TOPICS", "Auth OK — role: $role — fetching profile for FCM subscriptions")

                val profileResult = profileRepository.getProfile()
                if (profileResult is Result.Success) {
                    val dept = profileResult.data.dept
                    if (!dept.isNullOrBlank()) {
                        authPreferences.saveDept(dept)
                    }

                    FcmTopicManager.subscribeForProfile(profileResult.data)
                } else {
                    Log.w("FCM_TOPICS", "Could not load profile — skipping FCM subscription")
                }

                _navigateToNext.value = when (role) {
                    UserRole.STUDENT -> "student_graph"
                    UserRole.FACULTY -> "faculty_graph"
                }
            } else {
                Log.i("FCM_TOPICS", "Auth failed — routing to auth screen")
                _navigateToNext.value = "auth_graph"
            }

            _isReady.value = true
        }
    }
}
