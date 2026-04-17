package com.om.timely.faculty.timetable_generation.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenerateTimetableRequestDto(
    @SerialName("syllabus_id") val syllabusId: Int,
    @SerialName("dept") val dept: String,
    @SerialName("division") val division: String = "A"
)

@Serializable
data class GenerateTimetableResponseDto(
    val status: String,
    val message: String,
    val metadata: GenerateMetadataDto,
    val data: List<TimetableTemplateDto>? = null
)

@Serializable
data class GenerateMetadataDto(
    @SerialName("total_tasks_scheduled") val totalTasksScheduled: Int,
    @SerialName("is_feasible") val isFeasible: Boolean,
    val warnings: List<String> = emptyList()
)

@Serializable
data class TimetableTemplateDto(
    val dept: String,
    val semester: Int,
    val division: String,
    val days: List<TimetableDayDto>
)

@Serializable
data class TimetableDayDto(
    @SerialName("day_index") val dayIndex: Int,
    @SerialName("day_of_week") val dayOfWeek: String,
    val slots: List<TimetableSlotDto>
)

@Serializable
data class TimetableSlotDto(
    @SerialName("slot_index") val slotIndex: Int,
    @SerialName("slot_name") val slotName: String,
    @SerialName("slot_kind") val slotKind: String,
    val assignments: List<TimetableSlotAssignmentDto>
)

@Serializable
data class TimetableSlotAssignmentDto(
    @SerialName("course_code") val courseCode: String,
    @SerialName("subject_name") val subjectName: String?,
    @SerialName("subject_abbr") val subjectAbbr: String?,
    @SerialName("faculty_name") val facultyName: String,
    @SerialName("batch_label") val batchLabel: String?
)
