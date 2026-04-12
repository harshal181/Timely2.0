package com.om.timely.faculty.subjects.presentation

import com.om.timely.core.presentation.UiText

sealed interface SubjectManagementEvent {
    data class ShowError(val error: UiText) : SubjectManagementEvent
    data class ShowSuccess(val message: String) : SubjectManagementEvent
    object NavigateBack : SubjectManagementEvent
}
