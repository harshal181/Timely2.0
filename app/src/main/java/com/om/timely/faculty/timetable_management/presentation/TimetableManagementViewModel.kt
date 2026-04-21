package com.om.timely.faculty.timetable_management.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.timely.core.domain.AuthPreferences
import com.om.timely.core.domain.util.Result
import com.om.timely.faculty.timetable_generation.domain.error.asUiText
import com.om.timely.faculty.timetable_generation.domain.repository.TimetableRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TimetableManagementViewModel(
    private val timetableRepository: TimetableRepository,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(TimetableManagementState())
    val state = _state.asStateFlow()

    private val _eventChannel = Channel<TimetableManagementEvent>(Channel.BUFFERED)
    val events = _eventChannel.receiveAsFlow()

    private val dept: String get() = authPreferences.getDept() ?: ""

    init {
        loadGroups()
    }

    fun onAction(action: TimetableManagementAction) {
        when (action) {
            TimetableManagementAction.LoadGroups -> loadGroups()
            is TimetableManagementAction.ToggleActive -> activateGroup(action.groupId, action.isCurrentlyActive)
            is TimetableManagementAction.ExportPdf -> exportGroup(action.groupId)
            is TimetableManagementAction.DeleteGroup -> deleteGroup(action.groupId)
            TimetableManagementAction.DismissPdfSheet -> _state.update { it.copy(showPdfSheet = false, pdfAssets = emptyList()) }
            TimetableManagementAction.NavigateBack -> viewModelScope.launch {
                _eventChannel.send(TimetableManagementEvent.NavigateBack)
            }
        }
    }

    private fun loadGroups() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = timetableRepository.getTimetableGroupsDetails(dept)) {
                is Result.Success -> _state.update { it.copy(isLoading = false, groups = result.data) }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _eventChannel.send(TimetableManagementEvent.ShowSnackbar(result.error.asUiText()))
                }
            }
        }
    }

    private fun activateGroup(groupId: Int, isCurrentlyActive: Boolean) {
        // Tapping on an already-active group is a no-op
        if (isCurrentlyActive) return

        viewModelScope.launch {
            _state.update { it.copy(isActivating = true) }
            when (val result = timetableRepository.activateGroup(groupId, dept)) {
                is Result.Success -> {
                    _state.update { it.copy(isActivating = false) }
                    loadGroups() // Refresh to reflect new active badge
                }
                is Result.Error -> {
                    _state.update { it.copy(isActivating = false) }
                    _eventChannel.send(TimetableManagementEvent.ShowSnackbar(result.error.asUiText()))
                }
            }
        }
    }

    private fun exportGroup(groupId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true) }
            when (val result = timetableRepository.exportGroupToPdf(groupId)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(isExporting = false, showPdfSheet = true, pdfAssets = result.data)
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(isExporting = false) }
                    _eventChannel.send(TimetableManagementEvent.ShowSnackbar(result.error.asUiText()))
                }
            }
        }
    }

    private fun deleteGroup(groupId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true) }
            when (val result = timetableRepository.deleteGroup(groupId)) {
                is Result.Success -> {
                    _state.update { it.copy(isDeleting = false) }
                    loadGroups()
                }
                is Result.Error -> {
                    _state.update { it.copy(isDeleting = false) }
                    _eventChannel.send(TimetableManagementEvent.ShowSnackbar(result.error.asUiText()))
                }
            }
        }
    }
}
