package com.om.timely.faculty.timetable_management.presentation

import com.om.timely.faculty.timetable_generation.domain.model.TimetableGroup
import com.om.timely.faculty.timetable_generation.domain.model.TimetablePdfAsset

data class TimetableManagementState(
    val isLoading: Boolean = false,
    val groups: List<TimetableGroup> = emptyList(),
    // Loading states for individual operations
    val isActivating: Boolean = false,
    val isExporting: Boolean = false,
    val isDeleting: Boolean = false,
    // PDF export sheet
    val showPdfSheet: Boolean = false,
    val pdfAssets: List<TimetablePdfAsset> = emptyList()
)
