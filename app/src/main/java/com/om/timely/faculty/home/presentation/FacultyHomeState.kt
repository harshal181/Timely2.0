package com.om.timely.faculty.home.presentation

import com.om.timely.core.presentation.UiText
import com.om.timely.faculty.home.domain.model.DayScheduleInfo
import com.om.timely.profile.domain.model.FullProfile
import java.util.Calendar

fun getTodayDayOfWeek(): String {
    val calendar = Calendar.getInstance()
    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Mon"
        Calendar.TUESDAY -> "Tue"
        Calendar.WEDNESDAY -> "Wed"
        Calendar.THURSDAY -> "Thu"
        Calendar.FRIDAY -> "Fri"
        Calendar.SATURDAY -> "Sat"
        else -> "Mon"
    }
}

data class FacultyHomeState(
    val isLoading: Boolean = false,
    val profile: FullProfile? = null,
    val selectedDay: String = getTodayDayOfWeek(),
    val selectedSemester: Int = 6, // Default to sem 4
    val scheduleInfo: DayScheduleInfo? = null,
    val error: UiText? = null
)
