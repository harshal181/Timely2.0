package com.om.timely.faculty.syllabus_extractor.presentation

import com.om.timely.core.presentation.UiText

sealed interface SyllabusExtractorEvent {
    data class ShowError(val error: UiText) : SyllabusExtractorEvent
    data class ExtractSuccess(val message: String) : SyllabusExtractorEvent
    data class SaveSuccess(val message: String) : SyllabusExtractorEvent
}
