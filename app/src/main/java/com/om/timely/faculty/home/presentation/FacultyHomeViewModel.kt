package com.om.timely.faculty.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.timely.core.domain.util.Result
import com.om.timely.core.presentation.asUiText
import com.om.timely.faculty.home.domain.repository.FacultyHomeRepository
import com.om.timely.profile.domain.error.asUiText
import com.om.timely.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FacultyHomeViewModel(
    private val profileRepository: ProfileRepository,
    private val homeRepository: FacultyHomeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FacultyHomeState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<FacultyHomeEvent>()
    val events = _events.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            // Fetch profile
            when (val profileResult = profileRepository.getProfile()) {
                is Result.Success -> {
                    _state.update { it.copy(profile = profileResult.data) }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, error = profileResult.error.asUiText()) }
                    _events.emit(FacultyHomeEvent.ShowError(profileResult.error.asUiText()))
                    return@launch
                }
            }
            
            // Fetch Schedule Data for the default selected day ("Wed") and semester
            fetchSchedule(_state.value.selectedDay, _state.value.selectedSemester)
        }
    }

    private fun fetchSchedule(day: String, semester: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val scheduleResult = homeRepository.getScheduleForDay(day, semester)) {
                is Result.Success -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            selectedDay = day,
                            scheduleInfo = scheduleResult.data
                        ) 
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, error = scheduleResult.error.asUiText()) }
                    _events.emit(FacultyHomeEvent.ShowError(scheduleResult.error.asUiText()))
                }
            }
        }
    }

    fun onAction(action: FacultyHomeAction) {
        when (action) {
            is FacultyHomeAction.OnDaySelected -> {
                _state.update { it.copy(selectedDay = action.day) }
                fetchSchedule(action.day, _state.value.selectedSemester)
            }
            is FacultyHomeAction.OnSemesterSelected -> {
                _state.update { it.copy(selectedSemester = action.semester) }
                fetchSchedule(_state.value.selectedDay, action.semester)
            }
            FacultyHomeAction.OnNotificationClick -> {
                // Future Implementation
            }
        }
    }
}
