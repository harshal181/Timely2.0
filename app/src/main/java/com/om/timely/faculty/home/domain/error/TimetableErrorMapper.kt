package com.om.timely.faculty.home.domain.error

import com.om.timely.R
import com.om.timely.core.presentation.UiText
import com.om.timely.core.presentation.asUiText

fun TimeTableError.asUiText(): UiText {
    return when (this) {
        TimeTableError.NO_SCHEDULE_FOUND -> {
            UiText.StringResource(R.string.no_schedule_found)
        }
        is TimeTableError.Network -> this.networkError.asUiText()
    }
}