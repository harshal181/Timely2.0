package com.om.timely.faculty.timetable_generation.domain.error

import com.om.timely.core.domain.util.Error
import com.om.timely.core.domain.util.NetworkError

sealed interface TimetableError : Error {
    enum class Local : TimetableError {
        SOLVER_FAILED,
        NO_SYLLABUS_SELECTED,
        UNKNOWN_ERROR
    }

    data class Network(val networkError: NetworkError) : TimetableError
    data class BackendError(val message: String) : TimetableError
}
