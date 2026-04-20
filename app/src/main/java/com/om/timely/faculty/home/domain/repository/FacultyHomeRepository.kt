package com.om.timely.faculty.home.domain.repository

import com.om.timely.core.domain.util.NetworkError
import com.om.timely.core.domain.util.Result
import com.om.timely.faculty.home.domain.error.TimeTableError
import com.om.timely.faculty.home.domain.model.DayScheduleInfo

import com.om.timely.faculty.home.data.dto.CreateTimetableSlotDto
import com.om.timely.faculty.home.data.dto.UpdateTimetableSlotDto

interface FacultyHomeRepository {
    suspend fun getScheduleForDay(day: String, semester: Int, dept: String? = null): Result<DayScheduleInfo, TimeTableError>
    suspend fun addSlot(dayId: Int, slotData: CreateTimetableSlotDto): Result<Unit, TimeTableError>
    suspend fun updateSlot(slotId: Int, slotData: UpdateTimetableSlotDto): Result<Unit, TimeTableError>
    suspend fun swapSlots(dayId: Int, firstSlotId: Int, secondSlotId: Int): Result<Unit, TimeTableError>
    suspend fun deleteSlot(slotId: Int): Result<Unit, TimeTableError>
}
