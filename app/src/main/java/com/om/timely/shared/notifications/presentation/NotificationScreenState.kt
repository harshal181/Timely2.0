package com.om.timely.shared.notifications.presentation

import com.om.timely.core.presentation.UiText
import com.om.timely.shared.notifications.domain.model.AppNotification

/**
 * Shared contract for all notification filter types.
 * Students use [StudentNotificationFilter]; faculty use [FacultyNotificationFilter].
 */
sealed interface NotificationFilter {
    val label: String
}

/**
 * Filters available to students — purely type-based (no status filters).
 */
enum class StudentNotificationFilter(override val label: String) : NotificationFilter {
    ALL("All"),
    UPDATES("Updates"),
    CANCELLED("Cancelled"),
    CHANGES("Changes")
}

/**
 * Filters available to faculty — includes status-based filters (SENT, UNREAD)
 * that cannot be mapped to a [NotificationType].
 */
enum class FacultyNotificationFilter(override val label: String) : NotificationFilter {
    ALL("All"),
    SENT("Sent"),           // Status: notifications created by this faculty member
    UNREAD("Unread"),       // Status: notifications not yet read
    UPDATES("Updates"),     // Type: TIMETABLE_UPDATE + EXTRA_CLASS
    CANCELLED("Cancelled"), // Type: CLASS_CANCELLED
    CHANGES("Changes")      // Type: FACULTY_CHANGE
}

data class NotificationScreenState(
    val notifications: List<AppNotification> = emptyList(),
    val filteredNotifications: List<AppNotification> = emptyList(),
    val activeFilter: NotificationFilter = StudentNotificationFilter.ALL,
    val isFaculty: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,

    // Faculty Send Form State
    val isSendDialogVisible: Boolean = false,
    val sendFormState: SendNotificationFormState = SendNotificationFormState()
)

data class SendNotificationFormState(
    val title: String = "",
    val message: String = "",
    val selectedType: com.om.timely.shared.notifications.domain.model.NotificationType? = null,
    val targetScope: String = "All Students" // Defaults to all, can be Semester 1, 2, etc.
)
