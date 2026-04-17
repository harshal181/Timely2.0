package com.om.timely.faculty.timetable_generation.domain.error

import com.om.timely.core.presentation.UiText
import com.om.timely.core.presentation.asUiText
import com.om.timely.core.domain.util.NetworkError

fun TimetableError.asUiText(): UiText {
    return when(this) {
        TimetableError.Local.SOLVER_FAILED -> UiText.DynamicString("Failed to solve the timetable. Adjust constraints and try again.")
        TimetableError.Local.NO_SYLLABUS_SELECTED -> UiText.DynamicString("Please select a syllabus first.")
        TimetableError.Local.UNKNOWN_ERROR -> UiText.DynamicString("An unknown error occurred.")
        is TimetableError.BackendError -> UiText.DynamicString(this.message)
        is TimetableError.Network -> this.networkError.asUiText()
    }
}
