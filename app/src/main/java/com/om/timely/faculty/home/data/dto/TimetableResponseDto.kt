package com.om.timely.faculty.home.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimetableResponseDto(
    @SerialName("id") val id: String,
    @SerialName("semester") val semester: Int,
    @SerialName("division") val division: String,
    @SerialName("day_of_week") val dayOfWeek: String,
    @SerialName("timeline") val timeline: List<ScheduleSlotDto>
)
