package com.om.timely.faculty.timetable_generation.presentation

import com.om.timely.core.presentation.UiText

sealed interface TimetableGenerationEvent {
    data class ShowSnackbar(val text: UiText) : TimetableGenerationEvent
    object NavigateToDashboard : TimetableGenerationEvent
}
