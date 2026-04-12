package com.om.timely.faculty.subjects.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.timely.core.domain.AuthPreferences
import com.om.timely.core.domain.util.Result
import com.om.timely.faculty.subjects.domain.error.asUiText
import com.om.timely.faculty.subjects.domain.model.Subject
import com.om.timely.faculty.subjects.domain.repository.SubjectRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SubjectManagementViewModel(
    private val repository: SubjectRepository,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(SubjectManagementState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<SubjectManagementEvent>()
    val events = _events.asSharedFlow()

    private val currentDept: String = authPreferences.getDept()
        ?: "Computer Science and Engineering" // Ideally fetched from profile matching the scope!

    private var subjectsJob: Job? = null

    init {
        // Collect Syllabuses DB updates
        viewModelScope.launch {
            repository.getSyllabuses(currentDept).collect { syllabusList ->
                _state.update { it.copy(syllabuses = syllabusList) }
                if (_state.value.selectedSyllabus == null && syllabusList.isNotEmpty()) {
                    onAction(SubjectManagementAction.OnSyllabusSelected(syllabusList.first()))
                }
            }
        }
        
        loadSyllabuses()
    }

    private fun loadSyllabuses(dept: String = currentDept) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = repository.fetchSyllabuses(dept)
            if(result is Result.Error) {
                val errorText = result.error.asUiText()
                _state.update { it.copy(isLoading = false, error = errorText) }
                _events.emit(SubjectManagementEvent.ShowError(errorText))
            } else {
                _state.update { it.copy(isLoading = false, error = null) }
            }
        }
    }

    private fun observeSubjectsForSyllabus(syllabusId: Int) {
        subjectsJob?.cancel()
        subjectsJob = viewModelScope.launch {
            repository.getSubjects(syllabusId).collect { subjectsList ->
                _state.update { it.copy(subjects = subjectsList, isLoading = false, error = null) }
                updateFilteredSubjects()
            }
        }
        loadSubjectsForSyllabus(syllabusId)
    }

    private fun loadSubjectsForSyllabus(syllabusId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = repository.fetchSubjects(syllabusId)
            if(result is Result.Error) {
                val errorText = result.error.asUiText()
                _state.update { it.copy(isLoading = false, error = errorText) }
                _events.emit(SubjectManagementEvent.ShowError(errorText))
            } else {
                _state.update { it.copy(isLoading = false, error = null) }
            }
        }
    }

    fun onAction(action: SubjectManagementAction) {
        when (action) {
            is SubjectManagementAction.OnSyllabusSelected -> {
                _state.update { it.copy(selectedSyllabus = action.syllabus) }
                observeSubjectsForSyllabus(action.syllabus.id)
            }
            is SubjectManagementAction.OnSearchQueryChange -> {
                _state.update { it.copy(searchQuery = action.query) }
                updateFilteredSubjects()
            }
            is SubjectManagementAction.OnSemesterFilterSelected -> {
                _state.update { it.copy(selectedSemester = action.semester) }
                updateFilteredSubjects()
            }
            SubjectManagementAction.OnAddSubjectClick -> {
                _state.update { it.copy(isAddEditSheetVisible = true, subjectToEdit = null) }
            }
            is SubjectManagementAction.OnEditSubjectClick -> {
                _state.update { it.copy(isAddEditSheetVisible = true, subjectToEdit = action.subject) }
            }
            SubjectManagementAction.OnDismissSheet -> {
                _state.update { it.copy(isAddEditSheetVisible = false, subjectToEdit = null) }
            }
            is SubjectManagementAction.OnDeleteSubjectClick -> {
                _state.update { it.copy(subjectToDelete = action.subject) }
            }
            SubjectManagementAction.OnDismissDialog -> {
                _state.update { it.copy(subjectToDelete = null) }
            }
            is SubjectManagementAction.OnSaveSubject -> {
                val selectedId = state.value.selectedSyllabus?.id ?: return
                saveSubject(action.subject.copy(syllabusId = selectedId))
            }
            SubjectManagementAction.OnConfirmDelete -> {
                state.value.subjectToDelete?.let { subject ->
                    deleteSubject(subject.id)
                }
            }
            SubjectManagementAction.NavigateBack -> {
                viewModelScope.launch { _events.emit(SubjectManagementEvent.NavigateBack) }
            }
            SubjectManagementAction.OnDismissError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun saveSubject(subject: Subject) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isAddEditSheetVisible = false) }
            val isEditing = state.value.subjectToEdit != null
            val result = if (isEditing) {
                repository.updateSubject(subject)
            } else {
                repository.addSubject(subject)
            }

            when (result) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false, subjectToEdit = null) }
                    _events.emit(SubjectManagementEvent.ShowSuccess(if (isEditing) "Subject updated successfully" else "Subject added successfully"))
                    // Flow updates automatically thanks to optimistic UI in Repo
                }
                is Result.Error -> {
                    val errorUiText = result.error.asUiText()
                    _state.update { it.copy(isLoading = false, error = errorUiText, subjectToEdit = null) }
                    _events.emit(SubjectManagementEvent.ShowError(errorUiText))
                }
            }
        }
    }

    private fun deleteSubject(subjectId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, subjectToDelete = null) }
            when (val result = repository.deleteSubject(subjectId)) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.emit(SubjectManagementEvent.ShowSuccess("Subject deleted successfully"))
                    _state.value.selectedSyllabus?.id?.let { loadSubjectsForSyllabus(it) }
                }

                is Result.Error -> {
                    val errorUiText = result.error.asUiText()
                    _state.update { it.copy(isLoading = false, error = errorUiText) }
                    _events.emit(SubjectManagementEvent.ShowError(errorUiText))
                }
            }
        }
    }

    fun updateFilteredSubjects() {
        _state.update { currentState ->
            val filtered = currentState.subjects.filter { subject ->
                val matchesSearch = subject.name.contains(currentState.searchQuery, ignoreCase = true) ||
                        subject.code.contains(currentState.searchQuery, ignoreCase = true) ||
                        subject.abbreviation.contains(currentState.searchQuery, ignoreCase = true)
                val matchesSem = currentState.selectedSemester == null || subject.semester == currentState.selectedSemester
                matchesSearch && matchesSem
            }
            currentState.copy(filteredSubjects = filtered)
        }
    }
}

