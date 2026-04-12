package com.om.timely.faculty.syllabus_extractor.domain.error

import com.om.timely.core.domain.util.Error
import com.om.timely.core.domain.util.NetworkError

sealed interface SyllabusUploadError : Error {
    data object MissingImages : SyllabusUploadError
    data object FileTooLarge : SyllabusUploadError
}

sealed interface SyllabusProcessingError : Error {
    data object ExtractionTimeout : SyllabusProcessingError
    data object ExtractionFailed : SyllabusProcessingError
    data object InvalidResponse : SyllabusProcessingError
    data class ServerRejection(val msg: String): SyllabusProcessingError
    data class Network(val error: NetworkError) : SyllabusProcessingError
    data object NetworkFailure : SyllabusProcessingError
    data class ParsingError(val msg: String) : SyllabusProcessingError
}

sealed interface SubjectValidationError : Error {
    data object EmptyName : SubjectValidationError
    data object EmptyCode : SubjectValidationError
    data object InvalidHours : SubjectValidationError
    data object MissingElectiveGroup : SubjectValidationError
}
