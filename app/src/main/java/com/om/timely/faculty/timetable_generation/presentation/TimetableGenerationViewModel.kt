package com.om.timely.faculty.timetable_generation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.timely.core.domain.AuthPreferences
import com.om.timely.core.domain.util.Result
import com.om.timely.core.presentation.UiText
import com.om.timely.faculty.subjects.domain.repository.SubjectRepository
import com.om.timely.faculty.timetable_generation.domain.error.TimetableError
import com.om.timely.faculty.timetable_generation.domain.error.asUiText
import com.om.timely.faculty.timetable_generation.domain.repository.TimetableRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TimetableGenerationViewModel(
    private val timetableRepository: TimetableRepository,
    private val subjectRepository: SubjectRepository,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(TimetableGenerationState())
    val state = _state.asStateFlow()

    private val _eventChannel = Channel<TimetableGenerationEvent>()
    val events = _eventChannel.receiveAsFlow()

    private val currentDept: String = authPreferences.getDept()
        ?: "Computer Science and Engineering" // default fallback

    init {
        // Load available syllabuses on init
        viewModelScope.launch {
            subjectRepository.getSyllabuses(currentDept).collect { syllabusList ->
                _state.update { it.copy(availableSyllabi = syllabusList) }
                if (_state.value.selectedSyllabus == null && syllabusList.isNotEmpty()) {
                    onAction(TimetableGenerationAction.SelectSyllabus(syllabusList.first()))
                }
            }
        }
        
        viewModelScope.launch {
            subjectRepository.fetchSyllabuses(currentDept)
        }
    }

    fun onAction(action: TimetableGenerationAction) {
        when (action) {
            is TimetableGenerationAction.SelectSyllabus -> {
                _state.update { it.copy(selectedSyllabus = action.syllabus) }
            }
            is TimetableGenerationAction.ChangeDivision -> {
                _state.update { it.copy(selectedDivision = action.division) }
            }
            TimetableGenerationAction.ProceedToElectives -> {
                _state.update { it.copy(currentStep = WizardStep.ELECTIVE_SELECTION) }
            }
            TimetableGenerationAction.GenerateTimetable -> {
                _state.update { it.copy(currentStep = WizardStep.PROCESSING, isLoading = true, globalError = null) }
                generateTimetable()
            }
            TimetableGenerationAction.AcknowledgeWarnings -> {
                _state.update { it.copy(currentStep = WizardStep.REVIEW) }
            }
            is TimetableGenerationAction.SelectSemesterTab -> {
                _state.update { currentState ->
                    val selected = currentState.generatedTemplates.find { it.semester == action.semester }
                    currentState.copy(activeTemplate = selected ?: currentState.activeTemplate)
                }
            }
            TimetableGenerationAction.DismissError -> {
                _state.update { it.copy(globalError = null) }
            }
            TimetableGenerationAction.Undo -> undo()
            TimetableGenerationAction.Redo -> redo()
            is TimetableGenerationAction.SwapSlots -> {
                swapSlots(action.sourceDayId, action.sourceSlotIndex, action.targetDayId, action.targetSlotIndex)
            }
            TimetableGenerationAction.SaveTimetable -> {
                viewModelScope.launch {
                    _eventChannel.send(TimetableGenerationEvent.ShowSnackbar(UiText.DynamicString("Timetable Saved Successfully!")))
                    _eventChannel.send(TimetableGenerationEvent.NavigateToDashboard)
                }
            }
        }
    }



    private fun generateTimetable() {
        viewModelScope.launch {
            val syllabus = _state.value.selectedSyllabus
            if (syllabus == null) {
                _state.update { it.copy(currentStep = WizardStep.SCHEMA_SELECTION, isLoading = false, globalError = UiText.DynamicString("Please select a syllabus first.")) }
                return@launch
            }

            val division = _state.value.selectedDivision
            val result = timetableRepository.generateTimetable(
                syllabusId = syllabus.id,
                department = syllabus.dept,
                division = division
            )
            
            when (result) {
                is Result.Success -> {
                    val nextStep = if (result.data.warnings.isNotEmpty()) WizardStep.WARNINGS else WizardStep.REVIEW
                    _state.update { it.copy(
                        currentStep = nextStep,
                        isLoading = false,
                        generatedTemplates = result.data.templates,
                        warnings = result.data.warnings,
                        activeTemplate = result.data.templates.firstOrNull(),
                        undoStack = emptyList(),
                        redoStack = emptyList()
                    ) }
                }
                is Result.Error -> {
                    _state.update { it.copy(
                        currentStep = WizardStep.SCHEMA_SELECTION,
                        isLoading = false,
                        globalError = result.error.asUiText()
                    ) }
                }
            }
        }
    }

    private fun swapSlots(
        sourceDayId: Int, sourceSlotIndex: Int,
        targetDayId: Int, targetSlotIndex: Int
    ) {
        val currentTemplate = _state.value.activeTemplate ?: return
        if (sourceDayId == targetDayId && sourceSlotIndex == targetSlotIndex) return

        // Push current state for undo
        _state.update { it.copy(undoStack = it.undoStack + currentTemplate, redoStack = emptyList()) }

        val sDay = currentTemplate.days.find { it.id == sourceDayId } ?: return
        val tDay = currentTemplate.days.find { it.id == targetDayId } ?: return

        val sSlot = sDay.slots.getOrNull(sourceSlotIndex) ?: return
        val tSlot = tDay.slots.getOrNull(targetSlotIndex) ?: return

        // Swap only the *content* (kind + assignments).
        // Each slot keeps its own id, startTime, endTime, slotIndex (position).
        val newSSlot = sSlot.copy(slotKind = tSlot.slotKind, title = tSlot.title, assignments = tSlot.assignments)
        val newTSlot = tSlot.copy(slotKind = sSlot.slotKind, title = sSlot.title, assignments = sSlot.assignments)

        val newDays = currentTemplate.days.map { day ->
            when (day.id) {
                sourceDayId -> {
                    val slots = day.slots.toMutableList()
                    slots[sourceSlotIndex] = newSSlot
                    if (sourceDayId == targetDayId) slots[targetSlotIndex] = newTSlot
                    day.copy(slots = slots)
                }
                targetDayId -> {
                    val slots = day.slots.toMutableList()
                    slots[targetSlotIndex] = newTSlot
                    day.copy(slots = slots)
                }
                else -> day
            }
        }

        _state.update { it.copy(activeTemplate = currentTemplate.copy(days = newDays)) }
    }

    private fun undo() {
        val st = _state.value
        if (st.undoStack.isNotEmpty()) {
            val prev = st.undoStack.last()
            val current = st.activeTemplate
            if (current != null) {
                _state.update { it.copy(
                    activeTemplate = prev,
                    undoStack = it.undoStack.dropLast(1),
                    redoStack = it.redoStack + current
                ) }
            }
        }
    }

    private fun redo() {
        val st = _state.value
        if (st.redoStack.isNotEmpty()) {
            val next = st.redoStack.last()
            val current = st.activeTemplate
            if (current != null) {
                _state.update { it.copy(
                    activeTemplate = next,
                    undoStack = it.undoStack + current,
                    redoStack = it.redoStack.dropLast(1)
                ) }
            }
        }
    }
}
