package com.om.timely.faculty.syllabus.presentation

import SyllabusManagementAction
import SyllabusManagementEvent
import SyllabusManagementState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.timely.core.domain.util.Result
import com.om.timely.faculty.subjects.domain.error.asUiText
import com.om.timely.faculty.subjects.domain.repository.SubjectRepository
import com.om.timely.faculty.syllabus_extractor.domain.repository.SyllabusExtractorRepository
import com.om.timely.profile.domain.error.asUiText
import com.om.timely.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SyllabusManagementViewModel(
    private val subjectRepository: SubjectRepository,
    private val extractorRepository: SyllabusExtractorRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SyllabusManagementState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<SyllabusManagementEvent>()
    val events = _events.asSharedFlow()

    init {
        loadData()
        observeDrafts()
    }

    private fun observeDrafts() {
        viewModelScope.launch {
            extractorRepository.getAllDrafts().collect { drafts ->
                _state.update { it.copy(drafts = drafts) }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val profileResult = profileRepository.getProfile()
            if (profileResult is Result.Success) {
                val dept = profileResult.data.dept ?: ""
                
                // 1. Observe local Room flow
                viewModelScope.launch {
                    subjectRepository.getSyllabuses(dept).collect { list ->
                        _state.update { it.copy(syllabuses = list, isLoading = false) }
                    }
                }

                // 2. Refresh from network
                val fetchResult = subjectRepository.fetchSyllabuses(dept)
                if (fetchResult is Result.Error) {
                    _state.update { it.copy(error = fetchResult.error.asUiText()) }
                }
            } else if (profileResult is Result.Error) {
                _state.update { it.copy(isLoading = false, error = profileResult.error.asUiText()) }
            }
        }
    }

    fun onAction(action: SyllabusManagementAction) {
        when (action) {
            SyllabusManagementAction.OnExtractSyllabusClicked -> {
                viewModelScope.launch {
                    _events.emit(SyllabusManagementEvent.NavigateToSyllabusExtractor)
                }
            }
            is SyllabusManagementAction.OnDeleteSyllabusClick -> {
                _state.update { it.copy(showDeleteConfirmationDialog = true, deletingSyllabus = action.syllabus) }
            }
            SyllabusManagementAction.OnConfirmDelete -> {
                val syllabusToDelete = _state.value.deletingSyllabus
                if (syllabusToDelete != null) {
                    deleteSyllabus(syllabusToDelete.id)
                }
                _state.update { it.copy(showDeleteConfirmationDialog = false, deletingSyllabus = null) }
            }
            SyllabusManagementAction.OnDismissDeleteDialog -> {
                _state.update { it.copy(showDeleteConfirmationDialog = false, deletingSyllabus = null, deletingDraft = null) }
            }
            SyllabusManagementAction.OnRefresh -> {
                loadData()
            }
            is SyllabusManagementAction.OnDeleteDraftClick -> {
                _state.update { it.copy(deletingDraft = action.draft) }
            }
            SyllabusManagementAction.OnConfirmDeleteDraft -> {
                val draftToDelete = _state.value.deletingDraft
                if (draftToDelete != null) {
                    viewModelScope.launch {
                        extractorRepository.deleteDraft(draftToDelete.id)
                    }
                }
                _state.update { it.copy(deletingDraft = null) }
            }
            is SyllabusManagementAction.OnResumeDraftClick -> {
                viewModelScope.launch {
                    _events.emit(SyllabusManagementEvent.NavigateToResumeDraft(action.draft.id))
                }
            }

            else -> {}
        }
    }

    private fun deleteSyllabus(id: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = subjectRepository.deleteSyllabus(id)
            if (result is Result.Error) {
                _state.update { it.copy(isLoading = false, error = result.error.asUiText()) }
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
