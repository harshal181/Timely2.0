package com.om.timely.student.home.presentation

sealed interface StudentHomeAction {
    data class OnDaySelected(val day: String) : StudentHomeAction
    data class OnSemesterSelected(val semester: Int) : StudentHomeAction
    object OnNotificationClick : StudentHomeAction
}
