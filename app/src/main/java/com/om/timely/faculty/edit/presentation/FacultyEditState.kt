package com.om.timely.faculty.edit.presentation

import com.om.timely.core.presentation.UiText
import com.om.timely.faculty.home.domain.model.schedule.ScheduleSlot
import com.om.timely.profile.domain.model.FullProfile
import com.om.timely.faculty.edit.domain.model.EditSlotState

import com.om.timely.faculty.home.presentation.getTodayDayOfWeek
import com.om.timely.faculty.subjects.domain.model.Subject

data class FacultyEditState(
    val isLoading: Boolean = false,
    val profile: FullProfile? = null,
    val selectedDay: String = getTodayDayOfWeek(),
    val selectedDayId: Int? = null,
    val selectedSemester: Int = 6,
    val timeline: List<ScheduleSlot> = emptyList(),
    val originalTimeline: List<ScheduleSlot> = emptyList(), // Cached for bulk-lock logic
    
    // Subject Mapping Architecture State
    val isAssignSheetVisible: Boolean = false,
    val allAvailableSubjects: List<Subject> = emptyList(),
    val selectedSubjectIds: Set<Int> = emptySet(), // Checkbox state buffer
    
    // Lock / Sync State
    val isLocked: Boolean = false,
    
    // Slot Edit State
    val editSlotState: EditSlotState = EditSlotState(),

    val error: UiText? = null
)
