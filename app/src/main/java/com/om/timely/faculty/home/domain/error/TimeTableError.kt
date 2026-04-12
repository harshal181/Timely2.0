package com.om.timely.faculty.home.domain.error

import com.om.timely.core.domain.util.Error
import com.om.timely.core.domain.util.NetworkError
import com.om.timely.profile.domain.error.ProfileError

sealed interface TimeTableError: Error {
    object NO_SCHEDULE_FOUND: TimeTableError

    data class Network(val networkError: NetworkError) : TimeTableError
}