package com.om.timely.faculty.timetable_management.presentation

import com.om.timely.core.presentation.UiText

sealed interface TimetableManagementEvent {
    data class ShowSnackbar(val message: UiText) : TimetableManagementEvent
    data object NavigateBack : TimetableManagementEvent
}
