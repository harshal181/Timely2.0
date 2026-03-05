package com.om.timely.faculty.home.presentation

sealed interface FacultyHomeAction {
    data class OnDaySelected(val day: String) : FacultyHomeAction
    data class OnSemesterSelected(val semester: Int) : FacultyHomeAction
    object OnNotificationClick : FacultyHomeAction
}
