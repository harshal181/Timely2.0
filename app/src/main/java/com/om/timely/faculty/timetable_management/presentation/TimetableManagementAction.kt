package com.om.timely.faculty.timetable_management.presentation

sealed interface TimetableManagementAction {
    data object LoadGroups : TimetableManagementAction
    /** [isCurrentlyActive] — if true, the tap is a no-op (group is already active). */
    data class ToggleActive(val groupId: Int, val isCurrentlyActive: Boolean) : TimetableManagementAction
    data class ExportPdf(val groupId: Int) : TimetableManagementAction
    data class DeleteGroup(val groupId: Int) : TimetableManagementAction
    data object DismissPdfSheet : TimetableManagementAction
    data object NavigateBack : TimetableManagementAction
}
