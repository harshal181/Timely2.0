package com.om.timely.faculty.timetable_generation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.timely.core.domain.AuthPreferences
import com.om.timely.core.domain.util.Result
import com.om.timely.core.presentation.UiText
import com.om.timely.faculty.subjects.domain.repository.SubjectRepository
import com.om.timely.faculty.timetable_generation.domain.error.TimetableError
import com.om.timely.faculty.timetable_generation.domain.error.asUiText
import com.om.timely.faculty.timetable_generation.domain.repository.TimetableDraftRepository
import com.om.timely.faculty.timetable_generation.domain.repository.TimetableRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.messaging.FirebaseMessaging
import com.om.timely.faculty.timetable_generation.domain.model.SlotKind

class TimetableGenerationViewModel(
    private val timetableRepository: TimetableRepository,
    private val subjectRepository: SubjectRepository,
    private val authPreferences: AuthPreferences,
    private val draftRepository: TimetableDraftRepository
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

        // Restore active job if exists
        authPreferences.getActiveJobId()?.let { savedJobId ->
            _state.update { it.copy(activeJobId = savedJobId) }
        }

        // Observe local drafts in real-time
        viewModelScope.launch {
            draftRepository.getAllDrafts().collect { drafts ->
                _state.update { it.copy(localDrafts = drafts) }
            }
        }
    }

    private var pollingJob: Job? = null

    fun onAction(action: TimetableGenerationAction) {
        when (action) {
            is TimetableGenerationAction.SelectSyllabus -> {
                _state.update { it.copy(selectedSyllabus = action.syllabus) }
            }
            is TimetableGenerationAction.ChangeDivision -> {
                _state.update { it.copy(selectedDivision = action.division) }
            }
            TimetableGenerationAction.GenerateTimetable -> {
                _state.update { it.copy(currentStep = WizardStep.PROCESSING, isLoading = true, globalError = null) }
                startTimetableJob()
            }
            TimetableGenerationAction.CheckJobStatus -> {
                pollJobStatus()
            }
            TimetableGenerationAction.ClearActiveJob -> {
                clearJob()
            }
            TimetableGenerationAction.StopPollingAndLeave -> {
                pollingJob?.cancel()
                _state.update { it.copy(currentStep = WizardStep.SCHEMA_SELECTION) }
            }
            TimetableGenerationAction.ViewActiveJob -> {
                _state.update { it.copy(currentStep = WizardStep.PROCESSING) }
                pollJobStatus()
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
            is TimetableGenerationAction.SaveDraftLocally -> saveDraft(action.label)
            TimetableGenerationAction.SaveToDatabase -> publishAllTemplates()
            is TimetableGenerationAction.LoadDraft -> loadDraft(action.draftId)
            is TimetableGenerationAction.DeleteDraft -> deleteDraft(action.draftId)
            TimetableGenerationAction.ToggleViewType -> {
                _state.update { it.copy(isListView = !it.isListView) }
            }
            TimetableGenerationAction.ExportToPdf -> exportToPdf()
            TimetableGenerationAction.DismissPdfSheet -> {
                _state.update { it.copy(showPdfSheet = false) }
            }
            TimetableGenerationAction.NavigateBack -> {
                viewModelScope.launch { _eventChannel.send(TimetableGenerationEvent.NavigateToDashboard) }
            }
        }
    }

    // ── Draft Management ──────────────────────────────────────────────────────

    private fun saveDraft(label: String) {
        val templates = _state.value.generatedTemplates
        if (templates.isEmpty()) return
        val syllabus = _state.value.selectedSyllabus ?: return

        viewModelScope.launch {
            _state.update { it.copy(isDraftSaving = true) }
            try {
                val draftId = draftRepository.saveDraft(
                    label = label.ifBlank { "Draft — ${syllabus.name} Div ${_state.value.selectedDivision}" },
                    dept = syllabus.dept,
                    division = _state.value.selectedDivision,
                    syllabusId = syllabus.id,
                    syllabusName = syllabus.name,
                    templates = templates,
                    warnings = _state.value.warnings
                )
                _state.update { it.copy(isDraftSaving = false, activeDraftId = draftId) }
                _eventChannel.send(TimetableGenerationEvent.ShowSnackbar(
                    UiText.DynamicString("Draft saved successfully!")
                ))
            } catch (e: Exception) {
                _state.update { it.copy(isDraftSaving = false, globalError = UiText.DynamicString("Failed to save draft: ${e.message}")) }
            }
        }
    }

    private fun publishAllTemplates() {
        val currentState = _state.value
        val activeTemplate = currentState.activeTemplate
        
        // Sync activeTemplate changes back to the list before publishing
        val templatesToPublish = currentState.generatedTemplates.map { template ->
            if (activeTemplate != null && template.semester == activeTemplate.semester) {
                activeTemplate
            } else template
        }

        if (templatesToPublish.isEmpty()) {
            viewModelScope.launch { _eventChannel.send(TimetableGenerationEvent.ShowSnackbar(UiText.DynamicString("No timetables to publish."))) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isDraftSaving = true) } 
            try {
                // Publish all templates in a single bulk request
                timetableRepository.saveTimetables(
                    templates = templatesToPublish,
                    groupName = "",
                )
                
                _eventChannel.send(TimetableGenerationEvent.ShowSnackbar(UiText.DynamicString("All timetables published successfully!")))
            } catch (e: Exception) {
                _eventChannel.send(TimetableGenerationEvent.ShowSnackbar(UiText.DynamicString("Failed to publish: ${e.message}")))
            } finally {
                _state.update { it.copy(isDraftSaving = false) }
            }
        }
    }

    private fun exportToPdf() {
        val currentState = _state.value
        val activeTemplate = currentState.activeTemplate

        // Sync active template edits back into the full list before exporting
        val templatesToExport = currentState.generatedTemplates.map { template ->
            if (activeTemplate != null && template.semester == activeTemplate.semester) activeTemplate
            else template
        }

        if (templatesToExport.isEmpty()) {
            viewModelScope.launch {
                _eventChannel.send(TimetableGenerationEvent.ShowSnackbar(UiText.DynamicString("No timetables to export.")))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isPdfExporting = true) }
            when (val result = timetableRepository.exportToPdf(templatesToExport)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isPdfExporting = false,
                            showPdfSheet = true,
                            pdfAssets = result.data
                        )
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(isPdfExporting = false) }
                    _eventChannel.send(
                        TimetableGenerationEvent.ShowSnackbar(result.error.asUiText())
                    )
                }
            }
        }
    }

    private fun loadDraft(draftId: Int) {
        viewModelScope.launch {
            val draft = draftRepository.getDraftById(draftId) ?: return@launch
            _state.update { it.copy(
                currentStep = WizardStep.REVIEW,
                generatedTemplates = draft.templates,
                activeTemplate = draft.templates.firstOrNull(),
                warnings = draft.warnings,
                activeDraftId = draft.id,
                selectedDivision = draft.division,
                undoStack = emptyList(),
                redoStack = emptyList()
            ) }
        }
    }

    private fun deleteDraft(draftId: Int) {
        viewModelScope.launch {
            draftRepository.deleteDraft(draftId)
            if (_state.value.activeDraftId == draftId) {
                _state.update { it.copy(activeDraftId = null) }
            }
        }
    }

    private fun startTimetableJob() {
        viewModelScope.launch {
            val syllabus = _state.value.selectedSyllabus
            if (syllabus == null) {
                _state.update { it.copy(currentStep = WizardStep.SCHEMA_SELECTION, isLoading = false, globalError = UiText.DynamicString("Please select a syllabus first.")) }
                return@launch
            }

            try {
                // Determine FCM token for background delivery
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                
                val division = _state.value.selectedDivision
                val result = timetableRepository.startGenerationJob(
                    syllabusId = syllabus.id,
                    department = syllabus.dept,
                    division = division,
                    fcmToken = fcmToken
                )

                when (result) {
                    is Result.Success -> {
                        val jobId = result.data
                        authPreferences.saveActiveJobId(jobId)
                        _state.update { it.copy(activeJobId = jobId, jobStage = "queued") }
                        pollJobStatus() // Begin observing
                    }
                    is Result.Error -> {
                        _state.update { it.copy(
                            currentStep = WizardStep.SCHEMA_SELECTION,
                            isLoading = false,
                            globalError = result.error.asUiText()
                        ) }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    currentStep = WizardStep.SCHEMA_SELECTION,
                    isLoading = false,
                    globalError = UiText.DynamicString(e.message ?: "Failed to acquire FCM Token")
                ) }
            }
        }
    }

    private fun pollJobStatus() {
        val jobId = _state.value.activeJobId ?: return
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                when (val statusResult = timetableRepository.getJobStatus(jobId)) {
                    is Result.Success -> {
                        val statusData = statusResult.data
                        _state.update { it.copy(jobStage = statusData.stage) }
                        
                        if (statusData.status == "completed") {
                            fetchJobResult(jobId)
                            break
                        } else if (statusData.status == "failed") {
                            _state.update { it.copy(
                                isLoading = false,
                                globalError = UiText.DynamicString(statusData.error ?: "Generation Failed")
                            ) }
                            clearJob()
                            break
                        }
                    }
                    is Result.Error -> {
                        // Just wait and try again on slight network blips
                    }
                }
                delay(3000) // Poll every 3 seconds if active on screen
            }
        }
    }

    private suspend fun fetchJobResult(jobId: String) {
        val result = timetableRepository.getJobResult(jobId)
        when (result) {
            is Result.Success -> {
                _state.update { it.copy(
                    currentStep = WizardStep.REVIEW,
                    isLoading = false,
                    generatedTemplates = result.data.templates,
                    warnings = result.data.warnings,
                    activeTemplate = result.data.templates.firstOrNull(),
                    undoStack = emptyList(),
                    redoStack = emptyList()
                ) }
                clearJob() // Remove the job from memory after full successful fetch
            }
            is Result.Error -> {
                _state.update { it.copy(
                    isLoading = false,
                    globalError = result.error.asUiText()
                ) }
            }
        }
    }

    private fun clearJob() {
        pollingJob?.cancel()
        authPreferences.saveActiveJobId(null)
        _state.update { it.copy(activeJobId = null, jobStage = "idle") }
    }

    private fun swapSlots(
        sourceDayId: Int, sourceSlotIndex: Int,
        targetDayId: Int, targetSlotIndex: Int
    ) {
        val currentTemplate = _state.value.activeTemplate ?: return
        if (sourceDayId == targetDayId && sourceSlotIndex == targetSlotIndex) return

        val sDay = currentTemplate.days.find { it.id == sourceDayId } ?: return
        val tDay = currentTemplate.days.find { it.id == targetDayId } ?: return

        val sPrimary = sDay.slots.getOrNull(sourceSlotIndex) ?: return
        val tPrimary = tDay.slots.getOrNull(targetSlotIndex) ?: return

        // Prevent interacting with BREAK directly
        if (sPrimary.slotKind == SlotKind.BREAK || tPrimary.slotKind == SlotKind.BREAK) return

        val sSpan = if (sPrimary.slotKind == SlotKind.PRACTICAL || sPrimary.slotKind == SlotKind.WHOLE_CLASS_PRACTICAL) 2 else 1
        val tSpan = if (tPrimary.slotKind == SlotKind.PRACTICAL || tPrimary.slotKind == SlotKind.WHOLE_CLASS_PRACTICAL) 2 else 1

        val requiredSpan = maxOf(sSpan, tSpan)

        if (requiredSpan == 2) {
            // Check invalid boundaries for a 2-hour swap.
            // Recess is at index 3. Invalid starts: 2 (overlaps 3), 3 (is recess), 6 (overlaps 7 which is OOB)
            val invalidStarts = listOf(2, 3, 6)
            if (sourceSlotIndex in invalidStarts || targetSlotIndex in invalidStarts) {
                viewModelScope.launch {
                    _eventChannel.send(TimetableGenerationEvent.ShowSnackbar(UiText.DynamicString("Cannot place a 2-hour practical across a break or end of day.")))
                }
                return
            }

            // Enforce practical constraint: can only swap with another practical OR two empty slots
            if (sSpan == 2 && tSpan == 1) {
                val tSecondary = tDay.slots.getOrNull(targetSlotIndex + 1)
                if (tPrimary.slotKind != SlotKind.EMPTY || tSecondary?.slotKind != SlotKind.EMPTY) {
                    viewModelScope.launch {
                        _eventChannel.send(TimetableGenerationEvent.ShowSnackbar(UiText.DynamicString("A practical block can only be swapped with another practical or two consecutive empty slots.")))
                    }
                    return
                }
            } else if (tSpan == 2 && sSpan == 1) {
                 val sSecondary = sDay.slots.getOrNull(sourceSlotIndex + 1)
                 if (sPrimary.slotKind != SlotKind.EMPTY || sSecondary?.slotKind != SlotKind.EMPTY) {
                    viewModelScope.launch {
                        _eventChannel.send(TimetableGenerationEvent.ShowSnackbar(UiText.DynamicString("A practical block can only be swapped with another practical or two consecutive empty slots.")))
                    }
                    return
                }
            }
        }

        // Push current state for undo
        _state.update { it.copy(undoStack = it.undoStack + currentTemplate, redoStack = emptyList()) }

        val sDaySlots = sDay.slots.toMutableList()
        val tDaySlots = if (sourceDayId == targetDayId) sDaySlots else tDay.slots.toMutableList()

        // Perform parallel multi-slot swap
        for (offset in 0 until requiredSpan) {
            val sItem = sDaySlots[sourceSlotIndex + offset]
            val tItem = tDaySlots[targetSlotIndex + offset]
            
            sDaySlots[sourceSlotIndex + offset] = sItem.copy(
                slotKind = tItem.slotKind,
                title = tItem.title,
                assignments = tItem.assignments
            )
            
            tDaySlots[targetSlotIndex + offset] = tItem.copy(
                slotKind = sItem.slotKind,
                title = sItem.title,
                assignments = sItem.assignments
            )
        }
        
        val newDays = currentTemplate.days.map { day ->
            when (day.id) {
                sourceDayId -> day.copy(slots = sDaySlots)
                targetDayId -> day.copy(slots = tDaySlots)
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

