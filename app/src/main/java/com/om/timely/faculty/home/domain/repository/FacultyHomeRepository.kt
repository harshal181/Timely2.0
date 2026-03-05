package com.om.timely.faculty.home.domain.repository

import com.om.timely.core.domain.util.NetworkError
import com.om.timely.core.domain.util.Result
import com.om.timely.faculty.home.domain.model.DayScheduleInfo

interface FacultyHomeRepository {
    suspend fun getScheduleForDay(day: String, semester: Int): Result<DayScheduleInfo, NetworkError>
}
