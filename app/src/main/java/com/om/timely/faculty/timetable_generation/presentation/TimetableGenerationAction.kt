package com.om.timely.faculty.timetable_generation.presentation

import com.om.timely.faculty.subjects.domain.model.Syllabus

sealed interface TimetableGenerationAction {
    data class SelectSyllabus(val syllabus: Syllabus) : TimetableGenerationAction
    data class ChangeDivision(val division: String) : TimetableGenerationAction
    object GenerateTimetable : TimetableGenerationAction
    object CheckJobStatus : TimetableGenerationAction
    object ClearActiveJob : TimetableGenerationAction
    object StopPollingAndLeave : TimetableGenerationAction
    object ViewActiveJob : TimetableGenerationAction
    data class SelectSemesterTab(val semester: Int) : TimetableGenerationAction
    
    // Editor Intents
    data class SwapSlots(
        val sourceDayId: Int, val sourceSlotIndex: Int,
        val targetDayId: Int, val targetSlotIndex: Int
    ) : TimetableGenerationAction
    
    object Undo : TimetableGenerationAction
    object Redo : TimetableGenerationAction
    
    // UI toggles
    object ToggleViewType : TimetableGenerationAction
    
    // Save Actions
    data class SaveDraftLocally(val label: String) : TimetableGenerationAction
    object SaveToDatabase : TimetableGenerationAction
    data class LoadDraft(val draftId: Int) : TimetableGenerationAction
    data class DeleteDraft(val draftId: Int) : TimetableGenerationAction
    
    // Navigation
    object NavigateBack : TimetableGenerationAction

    // Error Handling
    object DismissError : TimetableGenerationAction
}
