package com.om.timely.faculty.subjects.domain.error

import com.om.timely.core.domain.util.Error
import com.om.timely.core.domain.util.NetworkError
import com.om.timely.faculty.home.domain.error.TimeTableError

sealed interface SubjectError : Error {
    enum class Local : SubjectError {
        SUBJECT_NOT_FOUND,
        CONFLICT,
        UNKNOWN_ERROR
    }

    data class Network(val networkError: NetworkError) : SubjectError
}
