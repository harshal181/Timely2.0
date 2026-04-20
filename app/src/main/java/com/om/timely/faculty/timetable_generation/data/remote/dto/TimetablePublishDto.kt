package com.om.timely.faculty.timetable_generation.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PublishTimetableBulkRequestDto(
    val templates: List<PublishTimetableTemplateDto>
)

@Serializable
data class PublishTimetableTemplateDto(
    val dept: String,
    val semester: Int,
    val division: String,
    val name: String,
    @SerialName("is_active") val isActive: Boolean,
    val days: List<PublishTimetableDayDto>
)

@Serializable
data class PublishTimetableDayDto(
    @SerialName("day_of_week") val dayOfWeek: String,
    val slots: List<PublishTimetableSlotDto>
)

@Serializable
data class PublishTimetableSlotDto(
    @SerialName("slot_index") val slotIndex: Int,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("slot_kind") val slotKind: String,
    val title: String?,
    val assignments: List<PublishTimetableAssignmentDto>
)

@Serializable
data class PublishTimetableAssignmentDto(
    @SerialName("subject_code") val subjectCode: String?,
    @SerialName("subject_name") val subjectName: String?,
    @SerialName("faculty_name") val facultyName: String?,
    @SerialName("room_label") val roomLabel: String?,
    @SerialName("batch_label") val batchLabel: String?
)
