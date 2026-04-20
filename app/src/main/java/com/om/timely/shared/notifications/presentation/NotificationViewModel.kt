package com.om.timely.shared.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.timely.core.domain.util.Result
import com.om.timely.shared.notifications.domain.error.asUiText
import com.om.timely.shared.notifications.domain.model.AppNotification
import com.om.timely.shared.notifications.domain.model.NotificationType
import com.om.timely.shared.notifications.domain.repository.NotificationRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.om.timely.auth.domain.UserRole
import com.om.timely.core.presentation.UiText
import com.om.timely.profile.domain.repository.ProfileRepository

class NotificationViewModel(
    private val repository: NotificationRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationScreenState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<NotificationEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        checkUserRole()
        observeNotifications()
        loadNotifications()
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            repository.getNotificationsFlow().collect { list ->
                _state.update {
                    it.copy(notifications = list)
                }
                applyFilter(currentList = list)
            }
        }
    }

    private fun checkUserRole() {
        viewModelScope.launch {
            val profileResult = profileRepository.getProfile()
            if (profileResult is Result.Success) {
                val isFaculty = profileResult.data.role == UserRole.FACULTY
                _state.update {
                    it.copy(
                        isFaculty = isFaculty,
                        // Set the correct default active filter based on role
                        activeFilter = if (isFaculty) FacultyNotificationFilter.ALL
                                       else StudentNotificationFilter.ALL
                    )
                }
                // Re-apply filter with the new role-aware default
                applyFilter()
            }
        }
    }

    fun onAction(action: NotificationAction) {
        when (action) {
            is NotificationAction.OnFilterSelected -> {
                _state.update { it.copy(activeFilter = action.filter) }
                applyFilter()
            }
            is NotificationAction.OnNotificationClicked -> markAsRead(action.notificationId)
            NotificationAction.OnRefresh -> {
                _state.update { it.copy(isRefreshing = true) }
                loadNotifications()
            }
            NotificationAction.OnMarkAllAsReadClicked -> markAllAsRead()

            // Faculty Send Form Actions
            is NotificationAction.OnToggleSendDialog -> {
                _state.update { it.copy(isSendDialogVisible = action.isVisible) }
                if (!action.isVisible) {
                    _state.update { it.copy(sendFormState = SendNotificationFormState()) }
                }
            }
            is NotificationAction.OnTitleChanged -> _state.update {
                it.copy(sendFormState = it.sendFormState.copy(title = action.title))
            }
            is NotificationAction.OnMessageChanged -> _state.update {
                it.copy(sendFormState = it.sendFormState.copy(message = action.message))
            }
            is NotificationAction.OnTypeSelected -> _state.update {
                it.copy(sendFormState = it.sendFormState.copy(selectedType = action.type))
            }
            is NotificationAction.OnTargetScopeSelected -> _state.update {
                it.copy(sendFormState = it.sendFormState.copy(targetScope = action.scope))
            }
            NotificationAction.OnSendNotificationClicked -> sendNotification()
        }
    }

    private fun sendNotification() {
        viewModelScope.launch {
            val form = _state.value.sendFormState

            // 1. Validation
            if (form.title.isBlank() || form.message.isBlank() || form.selectedType == null) {
                eventChannel.send(NotificationEvent.ShowError(com.om.timely.core.presentation.UiText.DynamicString("Please fill all fields")))
                return@launch
            }

            _state.update { it.copy(isLoading = true) }

            // 2. Map the UI dropdown string to the actual Firebase topic
            val targetTopic = mapScopeToTopic(form.targetScope)

            // 3. Call the repository
            val result = repository.sendNotification(
                title = form.title.trim(),
                message = form.message.trim(),
                type = form.selectedType.name,
                topic = targetTopic
            )

            // 4. Handle Result
            when (result) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isSendDialogVisible = false,
                            sendFormState = SendNotificationFormState() // Reset form
                        )
                    }
                    eventChannel.send(NotificationEvent.ShowSnackbar(com.om.timely.core.presentation.UiText.DynamicString("Notification Sent!")))
                    loadNotifications()
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    eventChannel.send(NotificationEvent.ShowError(result.error.asUiText()))
                }
            }
        }
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            if (!_state.value.isRefreshing) {
                _state.update { it.copy(isLoading = true, error = null) }
            }

            when (val result = repository.fetchNotifications()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    val uiError = result.error.asUiText()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = uiError
                        )
                    }
                    eventChannel.send(NotificationEvent.ShowError(uiError))
                }
            }
        }
    }

    private fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            val result = repository.markAsRead(notificationId)
            if (result is Result.Error) {
                eventChannel.send(NotificationEvent.ShowError(result.error.asUiText()))
            }
        }
    }

    private fun markAllAsRead() {
        viewModelScope.launch {
            val result = repository.markAllAsRead()
            if (result is Result.Error) {
                eventChannel.send(NotificationEvent.ShowError(result.error.asUiText()))
            }
        }
    }

    /**
     * Routes filtering to the role-specific implementation.
     * [StudentNotificationFilter] values only filter by NotificationType.
     * [FacultyNotificationFilter] values include status-based filters (SENT, UNREAD).
     */
    private fun applyFilter(
        filter: NotificationFilter = _state.value.activeFilter,
        currentList: List<AppNotification> = _state.value.notifications
    ) {
        val filtered = when (filter) {
            is StudentNotificationFilter -> applyStudentFilter(filter, currentList)
            is FacultyNotificationFilter -> applyFacultyFilter(filter, currentList)
        }
        _state.update { it.copy(filteredNotifications = filtered) }
    }

    private fun applyStudentFilter(
        filter: StudentNotificationFilter,
        list: List<AppNotification>
    ): List<AppNotification> = when (filter) {
        StudentNotificationFilter.ALL -> list
        StudentNotificationFilter.UPDATES -> list.filter {
            it.type == NotificationType.TIMETABLE_UPDATE || it.type == NotificationType.EXTRA_CLASS
        }
        StudentNotificationFilter.CANCELLED -> list.filter {
            it.type == NotificationType.CLASS_CANCELLED
        }
        StudentNotificationFilter.CHANGES -> list.filter {
            it.type == NotificationType.FACULTY_CHANGE
        }
    }

    private fun applyFacultyFilter(
        filter: FacultyNotificationFilter,
        list: List<AppNotification>
    ): List<AppNotification> = when (filter) {
        FacultyNotificationFilter.ALL -> list
        FacultyNotificationFilter.SENT -> list.filter { it.isSentByMe }
        FacultyNotificationFilter.UNREAD -> list.filter { !it.isRead }
        FacultyNotificationFilter.UPDATES -> list.filter {
            it.type == NotificationType.TIMETABLE_UPDATE || it.type == NotificationType.EXTRA_CLASS
        }
        FacultyNotificationFilter.CANCELLED -> list.filter {
            it.type == NotificationType.CLASS_CANCELLED
        }
        FacultyNotificationFilter.CHANGES -> list.filter {
            it.type == NotificationType.FACULTY_CHANGE
        }
    }

// helper method to map the topic with neat FCM topics
    private fun mapScopeToTopic(scope: String): String {
        return when (scope) {
            "All Students and Faculty" -> "common_topic"
            "Semester 1" -> "semester_1_sync"
            "Semester 2" -> "semester_2_sync"
            "Semester 3" -> "semester_3_sync"
            "Semester 4" -> "semester_4_sync"
            "Semester 5" -> "semester_5_sync"
            "Semester 6" -> "semester_6_sync"
            "Semester 7" -> "semester_7_sync"
            "Semester 8" -> "semester_8_sync"
            else -> {
                // Regex fallback just in case you add more semesters dynamically later
                val match = Regex("Semester (\\d+)").find(scope)
                if (match != null) {
                    val semNumber = match.groupValues[1]
                    "semester_${semNumber}_sync"
                } else {
                    "common_topic"
                }
            }
        }
    }
}
