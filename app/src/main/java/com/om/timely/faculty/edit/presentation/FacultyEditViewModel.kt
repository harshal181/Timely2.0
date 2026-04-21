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
import com.om.timely.faculty.home.data.dto.CreateTimetableSlotDto
import com.om.timely.faculty.home.data.dto.UpdateTimetableSlotDto
import com.om.timely.faculty.home.data.dto.CreateTimetableAssignmentDto
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import com.om.timely.faculty.edit.domain.model.EditBatchState
import com.om.timely.faculty.edit.domain.model.EditSlotState
import com.om.timely.faculty.edit.domain.model.Faculty


sealed interface FacultyEditEvent {
    data class ShowError(val error: UiText) : FacultyEditEvent
    object NavigateToSyllabus : FacultyEditEvent
    object NavigateToSubjects : FacultyEditEvent
    object NavigateToGenerate : FacultyEditEvent
    object NavigateToTT : FacultyEditEvent
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

            viewModelScope.launch {
                subjectRepository.getAllSubjects().collect { subjectsList ->
                    _state.update { it.copy(allAvailableSubjects = subjectsList) }
                }
            }

            // Trigger network sync for subjects into Room
            viewModelScope.launch {
                subjectRepository.fetchAllSubjects()
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
                            selectedDayId = scheduleResult.data.dayId,
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
            FacultyEditAction.OnDragEnded -> {
                val originalList = _state.value.originalTimeline
                val reorderedList = _state.value.timeline
                val dayId = _state.value.selectedDayId ?: return
                
                val currentConfig = originalList.toMutableList()
                val targetConfig = reorderedList
                val swapsToPerform = mutableListOf<Pair<Int, Int>>()

                for (i in targetConfig.indices) {
                    if (currentConfig[i].id != targetConfig[i].id) {
                        val correctItemIdx = currentConfig.indexOfFirst { it.id == targetConfig[i].id }
                        val id1 = currentConfig[i].id.toIntOrNull()
                        val id2 = currentConfig[correctItemIdx].id.toIntOrNull()
                        if (id1 != null && id2 != null) {
                            swapsToPerform.add(Pair(id1, id2))
                        }
                        val temp = currentConfig[i]
                        currentConfig[i] = currentConfig[correctItemIdx]
                        currentConfig[correctItemIdx] = temp
                    }
                }

                if (swapsToPerform.isNotEmpty()) {
                    viewModelScope.launch {
                        _state.update { it.copy(isLoading = true) }
                        var success = true
                        for (swap in swapsToPerform) {
                            val result = homeRepository.swapSlots(dayId, swap.first, swap.second)
                            if (result is Result.Error) {
                                success = false
                                _events.emit(FacultyEditEvent.ShowError(result.error.asUiText()))
                                break
                            }
                        }
                        
                        if (success) {
                            fetchSchedule(_state.value.selectedDay, _state.value.selectedSemester)
                        } else {
                            // If any swap failed, revert UI back to original safely to prevent descync
                            _state.update { it.copy(isLoading = false, timeline = originalList) }
                        }
                    }
                }
            }
            FacultyEditAction.OnAddSlotClicked -> {
                val emptySlot = _state.value.timeline.find { it is EmptySlot }
                if (emptySlot != null) {
                    val currentState = _state.value.editSlotState
                    val initialEditState = currentState.copy(
                        isVisible = true,
                        slotId = emptySlot.id,
                        isLecture = true,
                        lectureSubjectId = null,
                        lectureFacultyId = null,
                        lectureRoomId = null,
                        practicalBatches = emptyList()
                    )
                    _state.update { it.copy(editSlotState = initialEditState) }
                    return
                }

                // If no empty slot exists, append a new empty slot if under the time limit.
                val dayId = _state.value.selectedDayId
                if (dayId == null) {
                    viewModelScope.launch { _events.emit(FacultyEditEvent.ShowError(UiText.DynamicString("Wait for schedule to load."))) }
                    return
                }

                val currentTimeline = _state.value.timeline
                val lastTimeStr = currentTimeline.lastOrNull()?.endTime ?: "9:00 AM"

                val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
                val lastLocalTime = try {
                    LocalTime.parse(lastTimeStr.uppercase(), formatter)
                } catch (e: Exception) {
                    LocalTime.of(9, 0)
                }

                val maxLimitTime = LocalTime.of(17, 0) // 5:00 PM

                if (lastLocalTime.isBefore(maxLimitTime)) {
                    val nextEndTime = lastLocalTime.plusHours(1)
                    val formattedNext = nextEndTime.format(formatter).replace("AM", "AM").replace("PM", "PM")

                    viewModelScope.launch {
                        _state.update { it.copy(isLoading = true) }
                        val createDto = CreateTimetableSlotDto(
                            slotIndex = currentTimeline.size,
                            startTime = lastTimeStr,
                            endTime = formattedNext,
                            slotKind = "empty",
                            title = "No class scheduled",
                            assignments = emptyList()
                        )
                        when (val result = homeRepository.addSlot(dayId, createDto)) {
                            is Result.Success -> fetchSchedule(_state.value.selectedDay, _state.value.selectedSemester)
                            is Result.Error -> {
                                _state.update { it.copy(isLoading = false) }
                                _events.emit(FacultyEditEvent.ShowError(result.error.asUiText()))
                            }
                        }
                    }
                } else {
                    viewModelScope.launch {
                        _events.emit(FacultyEditEvent.ShowError(UiText.DynamicString("Cannot schedule beyond 5:00 PM")))
                    }
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
                            lectureSubjectId = _state.value.allAvailableSubjects.find { it.code == lec.subjectCode || it.name == lec.subjectName }?.id,
                            lectureFacultyId = currentState.availableFaculties.find { it.name == lec.facultyName }?.id,
                            lectureRoomId = currentState.availableRooms.find { it.roomNumber == lec.roomNo }?.id,
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
                            practicalBatches = prac.batches.map { batch ->
                                EditBatchState(
                                    id = batch.id, 
                                    name = batch.batchName,
                                    subjectId = _state.value.allAvailableSubjects.find { it.name == batch.subjectName }?.id,
                                    facultyId = currentState.availableFaculties.find { it.name == batch.facultyName }?.id,
                                    roomId = currentState.availableRooms.find { it.roomNumber == batch.roomNo }?.id
                                )
                            }
                        )
                    }
                    _state.update { it.copy(editSlotState = initialEditState) }
                }
            }
            FacultyEditAction.OnGenerateClicked -> {
                viewModelScope.launch {
                    _events.emit(FacultyEditEvent.NavigateToGenerate)
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
                val editState = _state.value.editSlotState
                val slotId = editState.slotId.toIntOrNull()
                val targetSlot = _state.value.timeline.find { it.id == editState.slotId }
                if (slotId == null || targetSlot == null) return
                
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true, editSlotState = EditSlotState(availableFaculties = editState.availableFaculties, availableRooms = editState.availableRooms)) }
                    
                    val assignments = mutableListOf<CreateTimetableAssignmentDto>()
                    if (editState.isLecture) {
                        val subCode = _state.value.allAvailableSubjects.find { it.id.toInt() == editState.lectureSubjectId }?.code ?: "Unknown"
                        val facName = editState.availableFaculties.find { it.id == editState.lectureFacultyId }?.name ?: "TBD"
                        val roomLabel = "TBD" // or from editState.availableRooms
                        
                        assignments.add(CreateTimetableAssignmentDto(subCode, facName, roomLabel, null))
                    } else {
                        editState.practicalBatches.forEach { batch ->
                            val subCode = _state.value.allAvailableSubjects.find { it.id.toInt() == batch.subjectId }?.code ?: "Unknown"
                            val facName = editState.availableFaculties.find { it.id == batch.facultyId }?.name ?: "TBD"
                            val roomLabel = "TBD"
                            assignments.add(CreateTimetableAssignmentDto(subCode, facName, roomLabel, batch.name))
                        }
                    }

                    val updateDto = UpdateTimetableSlotDto(
                        startTime = targetSlot.startTime,
                        endTime = targetSlot.endTime,
                        slotKind = if (editState.isLecture) "lecture" else "practical",
                        title = targetSlot.let { if (it is PracticalSlot) it.title else null },
                        assignments = assignments
                    )

                    when (val result = homeRepository.updateSlot(slotId, updateDto)) {
                        is Result.Success -> fetchSchedule(_state.value.selectedDay, _state.value.selectedSemester)
                        is Result.Error -> {
                            _state.update { it.copy(isLoading = false) }
                            _events.emit(FacultyEditEvent.ShowError(result.error.asUiText()))
                        }
                    }
                }
            }
            is FacultyEditAction.OnDeleteSlot -> {
                val slotId = action.slotId.toIntOrNull()
                if (slotId != null) {
                    viewModelScope.launch {
                        _state.update { it.copy(isLoading = true) }
                        when (val result = homeRepository.deleteSlot(slotId)) {
                            is Result.Success -> fetchSchedule(_state.value.selectedDay, _state.value.selectedSemester)
                            is Result.Error -> {
                                _state.update { it.copy(isLoading = false) }
                                _events.emit(FacultyEditEvent.ShowError(result.error.asUiText()))
                            }
                        }
                    }
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

            is FacultyEditAction.OnActivateGroup -> {}
            is FacultyEditAction.OnDeleteGroup -> {}
            is FacultyEditAction.OnExportGroup -> {}
            FacultyEditAction.OnTimetableManagementClicked -> {
                viewModelScope.launch {
                    _events.emit(FacultyEditEvent.NavigateToTT)
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
