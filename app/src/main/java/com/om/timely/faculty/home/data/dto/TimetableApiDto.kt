package com.om.timely.faculty.home.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimetableTemplateResponseDto(
    val id: Int,
    @SerialName("group_id") val groupId: Int? = null,
    val dept: String,
    val semester: Int,
    val division: String,
    val name: String? = null,
    val days: List<TimetableDayResponseDto>
)

@Serializable
data class TimetableDayResponseDto(
    val id: Int,
    @SerialName("day_of_week") val dayOfWeek: String,
    @SerialName("day_index") val dayIndex: Int,
    val slots: List<TimetableSlotResponseDto>
)

@Serializable
data class TimetableSlotResponseDto(
    val id: Int,
    @SerialName("slot_index") val slotIndex: Int,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("slot_kind") val slotKind: String,
    val title: String?,
    val assignments: List<TimetableAssignmentResponseDto>
)

@Serializable
data class TimetableAssignmentResponseDto(
    val id: Int,
    @SerialName("room_label") val roomLabel: String?,
    @SerialName("batch_label") val batchLabel: String?,
    val subject: SubjectResponseDto?,
    val faculty: FacultyResponseDto?
)

@Serializable
data class SubjectResponseDto(
    val id: Int,
    val code: String,
    val name: String,
    val abbr: String,
    val sem: Int,
    val dept: String,
    val type: String,
    @SerialName("color_argb") val colorArgb: Long?
)

@Serializable
data class FacultyResponseDto(
    val id: Int,
    @SerialName("fac_id") val facId: String?,
    val name: String,
    val designation: String?,
    val office: String?
)

@Serializable
data class CreateTimetableSlotDto(
    @SerialName("slot_index") val slotIndex: Int,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("slot_kind") val slotKind: String,
    val title: String?,
    val assignments: List<CreateTimetableAssignmentDto>
)

@Serializable
data class UpdateTimetableSlotDto(
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("slot_kind") val slotKind: String,
    val title: String?,
    val assignments: List<CreateTimetableAssignmentDto>
)

@Serializable
data class CreateTimetableAssignmentDto(
    @SerialName("subject_code") val subjectCode: String,
    @SerialName("faculty_name") val facultyName: String,
    @SerialName("room_label") val roomLabel: String?,
    @SerialName("batch_label") val batchLabel: String?
)

@Serializable
data class SwapSlotsDto(
    @SerialName("first_slot_id") val firstSlotId: Int,
    @SerialName("second_slot_id") val secondSlotId: Int
)

@Serializable
data class TimetableGroupSummaryDto(
    val id: Int,
    @SerialName("is_active") val isActive: Boolean = false
)
