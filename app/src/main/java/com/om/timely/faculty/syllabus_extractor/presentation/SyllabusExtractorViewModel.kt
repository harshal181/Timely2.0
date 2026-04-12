package com.om.timely.faculty.syllabus_extractor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.timely.core.domain.AuthPreferences
import com.om.timely.core.domain.util.Result
import com.om.timely.core.presentation.UiText
import com.om.timely.faculty.syllabus_extractor.domain.repository.CloudinaryRepository
import com.om.timely.faculty.syllabus_extractor.domain.repository.SyllabusExtractorRepository
import com.om.timely.faculty.syllabus_extractor.domain.model.SyllabusJobUpdate
import com.om.timely.faculty.syllabus_extractor.presentation.model.ExtractedSubjectUi
import com.om.timely.faculty.syllabus_extractor.presentation.model.SemesterUi
import com.om.timely.faculty.syllabus_extractor.domain.model.SubjectData
import com.om.timely.faculty.syllabus_extractor.domain.model.SemesterData
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SyllabusExtractorViewModel(
    private val cloudinaryRepository: CloudinaryRepository,
    private val extractorRepository: SyllabusExtractorRepository,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(SubjectExtractionState())
    val state: StateFlow<SubjectExtractionState> = _state.asStateFlow()

    private val eventChannel = Channel<SyllabusExtractorEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: SyllabusExtractorAction) {
        when (action) {
            is SyllabusExtractorAction.OnCategoryChanged -> {
                _state.update { it.copy(semesterCategory = action.category, selectedImages = emptyMap()) }
            }
            is SyllabusExtractorAction.OnImagePicked -> {
                val newImages = _state.value.selectedImages.toMutableMap()
                newImages[action.semesterNumber] = action.uri
                _state.update { it.copy(selectedImages = newImages) }
            }
            is SyllabusExtractorAction.OnSubmitUpload -> handleUploadAndExtract()
            
            is SyllabusExtractorAction.OnConfirmAndSaveAll -> handleConfirmAndSaveAll()
            
            is SyllabusExtractorAction.OnSyllabusNameChange -> {
                _state.update { it.copy(syllabusName = action.name) }
            }
            
            is SyllabusExtractorAction.OnFilterChanged -> {
                _state.update { it.copy(subjectTypeFilter = action.type) }
            }
            is SyllabusExtractorAction.OnSemesterTabSelected -> {
                _state.update { it.copy(selectedSemesterTabIndex = action.index) }
            }
            is SyllabusExtractorAction.OnEditSubjectClicked -> {
                updateSubjectInState(action.subjectId) { it.copy(isEditing = true) }
            }
            is SyllabusExtractorAction.OnCancelSubjectEdit -> {
                updateSubjectInState(action.subjectId) { it.copy(isEditing = false) }
            }
            is SyllabusExtractorAction.OnSaveSubjectEdit -> {
                updateSubjectInState(action.subjectId) { it.copy(isEditing = false) }
            }
            is SyllabusExtractorAction.OnToggleSaveAsDraft -> {
                _state.update { it.copy(isSaveAsDraftEnabled = action.enabled) }
            }
            is SyllabusExtractorAction.OnLoadDraft -> {
                loadDraft(action.draftId)
            }
            is SyllabusExtractorAction.OnSubjectFieldChanged -> {
                updateSubjectInState(action.subjectId) { subject ->
                    when (action.field) {
                        SubjectField.NAME -> subject.copy(name = action.newValue)
                        SubjectField.CODE -> subject.copy(courseCode = action.newValue)
                        SubjectField.LECTURE -> subject.copy(lecture = action.newValue.filter { char -> char.isDigit() })
                        SubjectField.TUTORIAL -> subject.copy(tutorial = action.newValue.filter { char -> char.isDigit() })
                        SubjectField.PRACTICAL -> subject.copy(practical = action.newValue.filter { char -> char.isDigit() })
                        SubjectField.TYPE -> subject.copy(type = action.newValue)
                        SubjectField.ELECTIVE_GROUP -> subject.copy(electiveGroup = action.newValue)
                    }
                }
            }
            else -> {}
        }
    }

    private fun updateSubjectInState(subjectId: String, transform: (ExtractedSubjectUi) -> ExtractedSubjectUi) {
        _state.update { currentState ->
            val updatedSemesters = currentState.extractedSemesters.map { semester ->
                val updatedSubjects = semester.subjects.map { subject ->
                    if (subject.id == subjectId) transform(subject) else subject
                }
                semester.copy(subjects = updatedSubjects)
            }
            currentState.copy(extractedSemesters = updatedSemesters)
        }
    }

    private fun handleUploadAndExtract() {
        if (!_state.value.canUpload) return

        _state.update { it.copy(
            currentStep = SubjectExtractionState.Step.PROCESSING,
            isProcessing = true,
            processingMessage = "Uploading ${state.value.selectedImages.size} images to Secure Storage..."
        ) }

        viewModelScope.launch {
            val uploadedUrls = mutableMapOf<String, String>()
            val uploadJobs = state.value.selectedImages.map { (sem, uri) ->
                async {
                    sem to cloudinaryRepository.uploadImage(uri)
                }
            }

            val results = uploadJobs.awaitAll()
            var hasUploadError = false

            for ((sem, result) in results) {
                when (result) {
                    is Result.Success -> {
                        uploadedUrls[sem.toString()] = result.data
                    }
                    is Result.Error -> {
                        hasUploadError = true
                        break
                    }
                }
            }

            if (hasUploadError) {
                eventChannel.send(SyllabusExtractorEvent.ShowError(UiText.DynamicString("Failed to upload syllabus images to Cloudinary.")))
                _state.update { it.copy(currentStep = SubjectExtractionState.Step.UPLOAD, isProcessing = false) }
                return@launch
            }

            _state.update { it.copy(processingMessage = "Starting payload job via backend AI...") }

            val startJobResult = extractorRepository.startExtractionJob(
                imageUrls = uploadedUrls.values.toList()
            )

            when (startJobResult) {
                is Result.Success -> {
                    val jobId = startJobResult.data
                    extractorRepository.observeExtractionJob(jobId).collect { update ->
                        when (update) {
                            is SyllabusJobUpdate.Progress -> {
                                _state.update { it.copy(sseStatus = update.status, processingMessage = update.message) }
                            }
                            is SyllabusJobUpdate.Completed -> {
                                val uiSemesters = update.data.semesters.map { semData ->
                                    SemesterUi(
                                        semester = semData.semester,
                                        subjects = semData.subjects.map { subjData ->
                                            ExtractedSubjectUi(
                                                id = subjData.id,
                                                name = subjData.name,
                                                courseCode = subjData.courseCode,
                                                lecture = subjData.lecture.toString(),
                                                tutorial = subjData.tutorial.toString(),
                                                practical = subjData.practical.toString(),
                                                type = subjData.type,
                                                electiveGroup = subjData.electiveGroup
                                             )
                                        }
                                    )
                                }

                                _state.update { it.copy(
                                    currentStep = SubjectExtractionState.Step.REVIEW,
                                    isProcessing = false,
                                    extractedSemesters = uiSemesters,
                                    selectedSemesterTabIndex = 0,
                                    sseStatus = "completed"
                                ) }
                                eventChannel.send(SyllabusExtractorEvent.ExtractSuccess("Extracted ${uiSemesters.size} semesters successfully."))
                            }
                            is SyllabusJobUpdate.Failed -> {
                                eventChannel.send(SyllabusExtractorEvent.ShowError(UiText.DynamicString("Job Failed.")))
                                _state.update { it.copy(
                                    sseStatus = "failed",
                                    processingMessage = update.error.toString(),
                                    isProcessing = false
                                ) }
                            }
                        }
                    }
                }
                is Result.Error -> {
                    eventChannel.send(SyllabusExtractorEvent.ShowError(UiText.DynamicString("Failure starting syllabus extraction job.")))
                    _state.update { it.copy(currentStep = SubjectExtractionState.Step.UPLOAD, isProcessing = false) }
                }
            }
        }
    }

    private fun handleConfirmAndSaveAll() {
        if (_state.value.isSavingFinal) return
        _state.update { it.copy(isSavingFinal = true, error = null) }
        
        viewModelScope.launch {
            val dept =  authPreferences.getDept() ?: "Computer Science and Engineering"
            val semestersData = _state.value.extractedSemesters.map { uiSem ->
                SemesterData(
                    semester = uiSem.semester,
                    subjects = uiSem.subjects.map { uiSubj ->
                        SubjectData(
                            id = uiSubj.id,
                            name = uiSubj.name,
                            courseCode = uiSubj.courseCode ?: "",
                            lecture = uiSubj.lecture.toIntOrNull() ?: 0,
                            tutorial = uiSubj.tutorial.toIntOrNull() ?: 0,
                            practical = uiSubj.practical.toIntOrNull() ?: 0,
                            type = uiSubj.type,
                            electiveGroup = uiSubj.electiveGroup
                        )
                    }
                )
            }
            
            val termType = _state.value.semesterCategory.name.lowercase()
            val finalName = _state.value.syllabusName.takeIf { it.isNotBlank() } ?: "Syllabus $termType"
            
            if (_state.value.isSaveAsDraftEnabled) {
                // Save as Draft
                val result = extractorRepository.saveDraft(
                    id = _state.value.loadedDraftId,
                    name = finalName,
                    dept = dept,
                    termType = termType,
                    semesters = semestersData
                )
                _state.update { it.copy(isSavingFinal = false) }
                when (result) {
                    is Result.Success -> {
                        eventChannel.send(SyllabusExtractorEvent.SaveSuccess("Draft successfully saved locally."))
                    }
                    is Result.Error -> {
                        eventChannel.send(SyllabusExtractorEvent.ShowError(UiText.DynamicString("Failed to save draft locally.")))
                    }
                }
            } else {
                // Push to Server
                when (val result = extractorRepository.confirmAndSaveSubjects(finalName, dept, termType, semestersData)) {
                    is Result.Success -> {
                        // If we were editing a draft, delete it after successful push
                        _state.value.loadedDraftId?.let { draftId ->
                            extractorRepository.deleteDraft(draftId)
                        }
                        
                        _state.update { it.copy(isSavingFinal = false) }
                        eventChannel.send(SyllabusExtractorEvent.SaveSuccess("Syllabus successfully saved to database."))
                    }
                    is Result.Error -> {
                        _state.update { it.copy(isSavingFinal = false) }
                        eventChannel.send(SyllabusExtractorEvent.ShowError(UiText.DynamicString("Failed to save syllabus data.")))
                    }
                }
            }
        }
    }

    private fun loadDraft(draftId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, processingMessage = "Loading draft...") }
            val draft = extractorRepository.getDraftById(draftId)
            if (draft != null) {
                val uiSemesters = draft.semesters.map { semData ->
                    SemesterUi(
                        semester = semData.semester,
                        subjects = semData.subjects.map { subjData ->
                            ExtractedSubjectUi(
                                id = subjData.id,
                                name = subjData.name,
                                courseCode = subjData.courseCode,
                                lecture = subjData.lecture.toString(),
                                tutorial = subjData.tutorial.toString(),
                                practical = subjData.practical.toString(),
                                type = subjData.type,
                                electiveGroup = subjData.electiveGroup
                            )
                        }
                    )
                }
                
                val category = if (draft.termType == "odd") SubjectExtractionState.SemesterCategory.ODD else SubjectExtractionState.SemesterCategory.EVEN

                _state.update { it.copy(
                    isProcessing = false,
                    currentStep = SubjectExtractionState.Step.REVIEW,
                    extractedSemesters = uiSemesters,
                    syllabusName = draft.name,
                    semesterCategory = category,
                    loadedDraftId = draft.id,
                    isSaveAsDraftEnabled = true // Keep draft mode on when editing a draft by default
                ) }
            } else {
                _state.update { it.copy(isProcessing = false) }
                eventChannel.send(SyllabusExtractorEvent.ShowError(UiText.DynamicString("Failed to load draft.")))
            }
        }
    }
}