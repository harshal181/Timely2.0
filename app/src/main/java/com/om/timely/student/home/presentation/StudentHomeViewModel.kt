package com.om.timely.student.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.timely.core.domain.util.Result
import com.om.timely.core.presentation.asUiText
import com.om.timely.faculty.home.domain.error.asUiText
import com.om.timely.faculty.home.domain.repository.FacultyHomeRepository
import com.om.timely.profile.domain.error.asUiText
import com.om.timely.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StudentHomeViewModel(
    private val profileRepository: ProfileRepository,
    private val homeRepository: FacultyHomeRepository // Reusing the timetable repository
) : ViewModel() {

    private val _state = MutableStateFlow(StudentHomeState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<StudentHomeEvent>()
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
                    val defaultSem = profileResult.data.studentDetails?.semester ?: 1
                    _state.update { 
                        it.copy(
                            profile = profileResult.data,
                            selectedSemester = defaultSem // Automatically set to enrolled semester
                        ) 
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, error = profileResult.error.asUiText()) }
                    _events.emit(StudentHomeEvent.ShowError(profileResult.error.asUiText()))
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
                    _events.emit(StudentHomeEvent.ShowError(scheduleResult.error.asUiText()))
                }
            }
        }
    }

    fun onAction(action: StudentHomeAction) {
        when (action) {
            is StudentHomeAction.OnDaySelected -> {
                _state.update { it.copy(selectedDay = action.day) }
                fetchSchedule(action.day, _state.value.selectedSemester)
            }
            is StudentHomeAction.OnSemesterSelected -> {
                _state.update { it.copy(selectedSemester = action.semester) }
                fetchSchedule(_state.value.selectedDay, action.semester)
            }
            StudentHomeAction.OnNotificationClick -> {
                viewModelScope.launch {
                    _events.emit(StudentHomeEvent.NavigateToNotifications)
                }
            }
        }
    }
}
