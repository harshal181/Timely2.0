package com.om.timely.faculty.home.presentation

import com.om.timely.core.presentation.UiText

sealed interface FacultyHomeEvent {
    data class ShowError(val error: UiText) : FacultyHomeEvent
    object NavigateToSubjects : FacultyHomeEvent
    object NavigateToSchemes : FacultyHomeEvent
    object NavigateToTimetableManagement : FacultyHomeEvent
    object NavigateToNotifications : FacultyHomeEvent
}
