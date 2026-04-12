package com.om.timely.faculty.edit.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.timely.core.domain.util.Result
import com.om.timely.core.presentation.UiText
import com.om.timely.core.presentation.asUiText
import com.om.timely.faculty.home.domain.error.asUiText
import com.om.timely.faculty.home.domain.model.schedule.EmptySlot
import com.om.timely.faculty.home.domain.model.schedule.LectureSlot
import com.om.timely.faculty.home.domain.model.schedule.PracticalSlot
import com.om.timely.faculty.home.domain.model.schedule.RecessSlot
import com.om.timely.faculty.home.domain.model.schedule.ScheduleSlot
import com.om.timely.faculty.home.domain.repository.FacultyHomeRepository
import com.om.timely.faculty.subjects.domain.repository.SubjectRepository
import com.om.timely.profile.domain.error.asUiText
import com.om.timely.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import com.om.timely.faculty.edit.domain.model.EditBatchState
import com.om.timely.faculty.edit.domain.model.EditSlotState
import com.om.timely.faculty.edit.domain.model.Faculty


sealed interface FacultyEditEvent {
    data class ShowError(val error: com.om.timely.core.presentation.UiText) : FacultyEditEvent
    object NavigateToSyllabus : FacultyEditEvent
    object NavigateToSubjects : FacultyEditEvent
}

class FacultyEditViewModel(
    private val profileRepository: ProfileRepository,
    private val homeRepository: FacultyHomeRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FacultyEditState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<FacultyEditEvent>()
    val events = _events.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            // Fetch profile
            when (val profileResult = profileRepository.getProfile()) {
                is Result.Success -> {
                    val assignedIds = profileResult.data.facultyDetails?.subjects?.map { it.id }?.toSet() ?: emptySet()
                    _state.update { 
                        it.copy(
                            profile = profileResult.data,
                            selectedSubjectIds = assignedIds 
                        ) 
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, error = profileResult.error.asUiText()) }
                    _events.emit(FacultyEditEvent.ShowError(profileResult.error.asUiText()))
                    return@launch
                }
            }
            
            // Background observe all available subjects for this faculty's dept
            val dept = _state.value.profile?.dept
                ?.takeIf { it.isNotBlank() }
                ?: "Computer Science and Engineering" // safe fallback

            viewModelScope.launch {
                subjectRepository.getAllSubjects(dept).collect { subjectsList ->
                    _state.update { it.copy(allAvailableSubjects = subjectsList) }
                }
            }

            // Trigger network sync for subjects into Room
            viewModelScope.launch {
                subjectRepository.fetchAllSubjects(dept)
            }

            // Background fetch all available faculties globally
            when (val facultiesResult = profileRepository.getAllFaculties()) {
                is Result.Success -> {
                    Log.d("faculty details", facultiesResult.toString())
                    val mappedFaculties = facultiesResult.data.map {
                        Faculty(
                            id = it.id,
                            name = it.name,
                            department = it.facultyDetails?.designation ?: "Faculty"
                        )
                    }
                    Log.d("list of faculties", mappedFaculties.toString())
                    _state.update { 
                        it.copy(
                            editSlotState = it.editSlotState.copy(
                                availableFaculties = mappedFaculties
                            )
                        ) 
                    }
                }
                is Result.Error -> {
                    // Log out failure to terminal explicitly to help user locate routing drops
                    println("API Fetch Faculties Error -> ${facultiesResult.error}")
                }
            }
            
            fetchSchedule(_state.value.selectedDay, _state.value.selectedSemester)
        }
    }

    private fun fetchSchedule(day: String, semester: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val scheduleResult = homeRepository.getScheduleForDay(day, semester)) {
                is Result.Success -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            selectedDay = day,
                            selectedSemester = semester,
                            timeline = scheduleResult.data.timeline,
                            originalTimeline = scheduleResult.data.timeline
                        ) 
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, error = scheduleResult.error.asUiText()) }
                    _events.emit(FacultyEditEvent.ShowError(scheduleResult.error.asUiText()))
                }
            }
        }
    }

    fun onAction(action: FacultyEditAction) {
        when (action) {
            is FacultyEditAction.OnDaySelected -> {
                fetchSchedule(action.day, _state.value.selectedSemester)
            }
            is FacultyEditAction.OnSemesterSelected -> {
                _state.update { it.copy(selectedSemester = action.semester) }
                fetchSchedule(_state.value.selectedDay, action.semester)
            }
            is FacultyEditAction.OnSlotsReordered -> {
                val recalcedList = recalculateTimelineStrings(_state.value.timeline, action.slots)
                _state.update { it.copy(timeline = recalcedList) }
            }
            FacultyEditAction.OnAddSlotClicked -> {
                val currentTimeline = _state.value.timeline
                val lastTimeStr = currentTimeline.lastOrNull()?.endTime ?: "9:00 AM"
                
                val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
                val lastLocalTime = LocalTime.parse(lastTimeStr.uppercase(), formatter)
                val maxLimitTime = LocalTime.of(17, 0) // 5:00 PM
                
                if (lastLocalTime.isBefore(maxLimitTime)) {
                    val nextEndTime = lastLocalTime.plusHours(1)
                    val formattedNext = nextEndTime.format(formatter).replace("AM", "AM").replace("PM", "PM") // Quick format
                    
                    val newSlot = EmptySlot(
                        id = "new_${UUID.randomUUID().toString().take(6)}",
                        startTime = lastTimeStr,
                        endTime = formattedNext
                    )
                    _state.update { it.copy(timeline = currentTimeline + newSlot) } // No full recalculation needed for addition
                } else {
                    viewModelScope.launch {
                        _events.emit(FacultyEditEvent.ShowError(UiText.DynamicString("Cannot schedule beyond 5:00 PM")))
                    }
                }
            }
            FacultyEditAction.OnToggleLockClicked -> {
                _state.update { currentState ->
                    val willLock = !currentState.isLocked
                    if (willLock) {
                        // Moving from Unlocked -> Locked. Time to sync bulk changes!
                        val addedSlots = currentState.timeline.filter { slot -> currentState.originalTimeline.none { it.id == slot.id } }
                        val removedSlots = currentState.originalTimeline.filter { slot -> currentState.timeline.none { it.id == slot.id } }
                        val modifiedSlots = currentState.timeline.filter { slot -> 
                            val orig = currentState.originalTimeline.find { it.id == slot.id }
                            orig != null && orig != slot
                        }

                        viewModelScope.launch {
                            val msg = "Syncing... Added: ${addedSlots.size}, Removed: ${removedSlots.size}, Modified: ${modifiedSlots.size}"
                            _events.emit(FacultyEditEvent.ShowError(com.om.timely.core.presentation.UiText.DynamicString(msg)))
                        }
                    }
                    currentState.copy(isLocked = willLock, originalTimeline = if(willLock) currentState.timeline else currentState.originalTimeline)
                }
            }
            is FacultyEditAction.OnSlotEditClicked -> {
                val targetSlot = _state.value.timeline.find { it.id == action.slotId }
                if (targetSlot != null && targetSlot !is RecessSlot && targetSlot !is EmptySlot) {
                    val isLecture = targetSlot is LectureSlot
                    val currentState = _state.value.editSlotState
                    val initialEditState = if (isLecture) {
                        val lec = targetSlot as LectureSlot
                        currentState.copy(
                            isVisible = true,
                            slotId = lec.id,
                            isLecture = true,
                            lectureSubjectId = null,
                            lectureFacultyId = null,
                            lectureRoomId = null,
                            practicalBatches = emptyList()
                        )
                    } else {
                        val prac = targetSlot as PracticalSlot
                        currentState.copy(
                            isVisible = true,
                            slotId = prac.id,
                            isLecture = false,
                            lectureSubjectId = null,
                            lectureFacultyId = null,
                            lectureRoomId = null,
                            practicalBatches = prac.batches.map { 
                                EditBatchState(id = it.id, name = it.batchName)
                            }
                        )
                    }
                    _state.update { it.copy(editSlotState = initialEditState) }
                }
            }
            FacultyEditAction.OnGenerateClicked -> {
                // Future Navigation handling
            }
            FacultyEditAction.OnSubjectsClicked -> {
                viewModelScope.launch {
                    _events.emit(FacultyEditEvent.NavigateToSubjects)
                }
            }
            FacultyEditAction.OnSyllabusClicked -> {
                viewModelScope.launch {
                    _events.emit(FacultyEditEvent.NavigateToSyllabus)
                }
            }
            FacultyEditAction.OnManageSubjectsClicked -> {
                _state.update { it.copy(isAssignSheetVisible = true) }
            }
            FacultyEditAction.OnDismissManageSubjectsSheet -> {
                // Reset selections back to whatever the profile natively says
                val assignedIds = _state.value.profile?.facultyDetails?.subjects?.map { it.id }?.toSet() ?: emptySet()
                _state.update { it.copy(isAssignSheetVisible = false, selectedSubjectIds = assignedIds) }
            }
            is FacultyEditAction.OnSubjectSelectionToggled -> {
                _state.update { currentState ->
                    val currentSelection = currentState.selectedSubjectIds.toMutableSet()
                    if (currentSelection.contains(action.subjectId)) {
                        currentSelection.remove(action.subjectId)
                    } else {
                        currentSelection.add(action.subjectId)
                    }
                    currentState.copy(selectedSubjectIds = currentSelection)
                }
            }
            FacultyEditAction.OnSaveSubjectAssignments -> {
                saveSubjectAssignments()
            }
            // --- Slot Edit Bottom Sheet Actions ---
            FacultyEditAction.OnDismissEditSlotSheet -> {
                _state.update { 
                    it.copy(editSlotState = EditSlotState(
                        availableFaculties = it.editSlotState.availableFaculties, 
                        availableRooms = it.editSlotState.availableRooms
                    )) 
                }
            }
            is FacultyEditAction.OnSlotTypeChanged -> {
                _state.update { 
                    it.copy(editSlotState = it.editSlotState.copy(isLecture = action.isLecture)) 
                }
            }
            is FacultyEditAction.OnLectureSubjectChanged -> {
                _state.update { 
                    it.copy(editSlotState = it.editSlotState.copy(lectureSubjectId = action.subject.id.toInt()))
                }
            }
            is FacultyEditAction.OnLectureFacultyChanged -> {
                _state.update { 
                    it.copy(editSlotState = it.editSlotState.copy(lectureFacultyId = action.faculty.id)) 
                }
            }
            is FacultyEditAction.OnLectureRoomChanged -> {
                _state.update { 
                    it.copy(editSlotState = it.editSlotState.copy(lectureRoomId = action.room.id)) 
                }
            }
            is FacultyEditAction.OnBatchSubjectChanged -> {
                _state.update { state ->
                    val updatedBatches = state.editSlotState.practicalBatches.map {
                        if (it.id == action.batchId) it.copy(subjectId = action.subject.id.toInt()) else it
                    }
                    state.copy(editSlotState = state.editSlotState.copy(practicalBatches = updatedBatches))
                }
            }
            is FacultyEditAction.OnBatchFacultyChanged -> {
                _state.update { state ->
                    val updatedBatches = state.editSlotState.practicalBatches.map {
                        if (it.id == action.batchId) it.copy(facultyId = action.faculty.id) else it
                    }
                    state.copy(editSlotState = state.editSlotState.copy(practicalBatches = updatedBatches))
                }
            }
            is FacultyEditAction.OnBatchRoomChanged -> {
                _state.update { state ->
                    val updatedBatches = state.editSlotState.practicalBatches.map {
                        if (it.id == action.batchId) it.copy(roomId = action.room.id) else it
                    }
                    state.copy(editSlotState = state.editSlotState.copy(practicalBatches = updatedBatches))
                }
            }
            FacultyEditAction.OnSaveSlotChanges -> {
                // Apply the buffered edits from the Bottom Sheet to the working timeline array
                // For now, just simulate applying changes and closing sheet
                _state.update { 
                    it.copy(editSlotState = EditSlotState(
                        availableFaculties = it.editSlotState.availableFaculties, 
                        availableRooms = it.editSlotState.availableRooms
                    )) 
                }
                viewModelScope.launch {
                    _events.emit(FacultyEditEvent.ShowError(com.om.timely.core.presentation.UiText.DynamicString("Mock: Slot Changes Applied to Buffer! Hit Lock to Sync.")))
                }
            }
            is FacultyEditAction.OnDeleteSlot -> {
                _state.update { currentState ->
                    val filteredTimeline = currentState.timeline.filter { it.id != action.slotId }
                    currentState.copy(
                        timeline = recalculateTimelineStrings(filteredTimeline, filteredTimeline),
                        editSlotState = EditSlotState(
                            availableFaculties = currentState.editSlotState.availableFaculties, 
                            availableRooms = currentState.editSlotState.availableRooms
                        )
                    )
                }
            }
            FacultyEditAction.OnSubjectsClicked -> {
                viewModelScope.launch {
                    _events.emit(FacultyEditEvent.NavigateToSubjects)
                }
            }
            FacultyEditAction.OnSyllabusClicked -> {
                viewModelScope.launch {
                    _events.emit(FacultyEditEvent.NavigateToSyllabus)
                }
            }
        }
    }

    private fun saveSubjectAssignments() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isAssignSheetVisible = false) }
            
            val originalAssignedIds = _state.value.profile?.facultyDetails?.subjects?.map { it.id }?.toSet() ?: emptySet()
            val newSelectedIds = _state.value.selectedSubjectIds
            
            // Calculate Diffs
            val idsToAssign = newSelectedIds - originalAssignedIds
            val idsToUnassign = originalAssignedIds - newSelectedIds
            
            // Execute operations sequentially
            var hasError = false
            for (id in idsToAssign) {
                if (profileRepository.assignSubject(id) is Result.Error) hasError = true
            }
            for (id in idsToUnassign) {
                if (profileRepository.unassignSubject(id) is Result.Error) hasError = true
            }
            
            if (hasError) {
                _events.emit(FacultyEditEvent.ShowError(com.om.timely.core.presentation.UiText.DynamicString("Some assignments failed. Refetching latest state.")))
            }
            
            // Reload the profile to ensure exact parity with backend logic
            loadData() 
        }
    }

    private fun recalculateTimelineStrings(originalList: List<ScheduleSlot>, reorderedList: List<ScheduleSlot>): List<ScheduleSlot> {
        if (reorderedList.isEmpty() || originalList.size != reorderedList.size) return reorderedList

        // To fix the Recess slot and layout without mess, we just lock the original 
        // time boundaries to their list indices and map them to the newly reordered items.
        return reorderedList.mapIndexed { index, slot ->
            val originalSlot = originalList[index]
            when (slot) {
                is LectureSlot -> slot.copy(startTime = originalSlot.startTime, endTime = originalSlot.endTime)
                is PracticalSlot -> slot.copy(startTime = originalSlot.startTime, endTime = originalSlot.endTime)
                is RecessSlot -> slot.copy(startTime = originalSlot.startTime, endTime = originalSlot.endTime)
                is EmptySlot -> slot.copy(startTime = originalSlot.startTime, endTime = originalSlot.endTime)
            }
        }
    }
}
