package com.om.timely.faculty.timetable_generation.presentation

import com.om.timely.faculty.subjects.domain.model.Syllabus

sealed interface TimetableGenerationAction {
    data class SelectSyllabus(val syllabus: Syllabus) : TimetableGenerationAction
    data class ChangeDivision(val division: String) : TimetableGenerationAction
    object ProceedToElectives : TimetableGenerationAction
    object GenerateTimetable : TimetableGenerationAction
    object AcknowledgeWarnings : TimetableGenerationAction
    data class SelectSemesterTab(val semester: Int) : TimetableGenerationAction
    
    // Editor Intents
    data class SwapSlots(
        val sourceDayId: Int, val sourceSlotIndex: Int,
        val targetDayId: Int, val targetSlotIndex: Int
    ) : TimetableGenerationAction
    
    object Undo : TimetableGenerationAction
    object Redo : TimetableGenerationAction
    object SaveTimetable : TimetableGenerationAction
    
    // Error Handling
    object DismissError : TimetableGenerationAction
}
