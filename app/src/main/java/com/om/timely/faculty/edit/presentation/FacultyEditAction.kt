package com.om.timely.faculty.edit.presentation

import com.om.timely.faculty.home.domain.model.schedule.ScheduleSlot
import com.om.timely.faculty.edit.domain.model.Faculty
import com.om.timely.faculty.edit.domain.model.Room
import com.om.timely.faculty.subjects.domain.model.Subject

sealed interface FacultyEditAction {
    data class OnDaySelected(val day: String) : FacultyEditAction
    data class OnSemesterSelected(val semester: Int) : FacultyEditAction
    data class OnSlotsReordered(val slots: List<ScheduleSlot>) : FacultyEditAction
    object OnDragEnded : FacultyEditAction
    object OnAddSlotClicked : FacultyEditAction
    data class OnSlotEditClicked(val slotId: String) : FacultyEditAction
    object OnGenerateClicked : FacultyEditAction
    
    // AI Syllabus Extraction
    object OnSubjectsClicked : FacultyEditAction
    object OnSyllabusClicked : FacultyEditAction
    
    // Subject Mapping Action handlers
    object OnManageSubjectsClicked : FacultyEditAction
    object OnDismissManageSubjectsSheet : FacultyEditAction
    data class OnSubjectSelectionToggled(val subjectId: Int) : FacultyEditAction
    object OnSaveSubjectAssignments : FacultyEditAction
    
    // Edit Slot Action Handlers
    object OnDismissEditSlotSheet : FacultyEditAction
    data class OnSlotTypeChanged(val isLecture: Boolean) : FacultyEditAction
    
    // Lecture form edits
    data class OnLectureSubjectChanged(val subject: Subject) : FacultyEditAction
    data class OnLectureFacultyChanged(val faculty: Faculty) : FacultyEditAction
    data class OnLectureRoomChanged(val room: Room) : FacultyEditAction
    
    // Practical form edits
    data class OnBatchSubjectChanged(val batchId: String, val subject: Subject) : FacultyEditAction
    data class OnBatchFacultyChanged(val batchId: String, val faculty: Faculty) : FacultyEditAction
    data class OnBatchRoomChanged(val batchId: String, val room: Room) : FacultyEditAction
    
    // Edit Slot Save / Delete
    object OnSaveSlotChanges : FacultyEditAction
    data class OnDeleteSlot(val slotId: String) : FacultyEditAction
}
