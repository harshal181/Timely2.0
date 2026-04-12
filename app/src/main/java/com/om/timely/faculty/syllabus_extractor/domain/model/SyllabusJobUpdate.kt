package com.om.timely.faculty.syllabus_extractor.domain.model

import com.om.timely.faculty.syllabus_extractor.domain.error.SyllabusProcessingError

sealed interface SyllabusJobUpdate {
    data class Progress(val status: String, val message: String) : SyllabusJobUpdate
    data class Completed(val data: SubjectExtractionData) : SyllabusJobUpdate
    data class Failed(val error: SyllabusProcessingError) : SyllabusJobUpdate
}
