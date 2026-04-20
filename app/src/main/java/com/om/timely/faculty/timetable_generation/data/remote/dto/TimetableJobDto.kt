package com.om.timely.faculty.timetable_generation.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimetableJobRequestDto(
    @SerialName("syllabus_id") val syllabusId: Int,
    @SerialName("dept") val dept: String,
    @SerialName("division") val division: String = "A",
    @SerialName("fcm_token") val fcmToken: String
)

@Serializable
data class TimetableJobResponseDto(
    @SerialName("job_id") val jobId: String,
    val status: String,
    val stage: String,
    @SerialName("created_at") val createdAt: String,
    val message: String
)

@Serializable
data class TimetableJobStatusDto(
    @SerialName("job_id") val jobId: String,
    val status: String,
    val stage: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("completed_at") val completedAt: String? = null,
    val error: String? = null,
    val warnings: List<String> = emptyList()
)

@Serializable
data class TimetableJobResultDto(
    @SerialName("job_id") val jobId: String,
    val status: String,
    val stage: String,
    @SerialName("completed_at") val completedAt: String? = null,
    val warnings: List<String> = emptyList(),
    val data: TimetableJobResultDataDto? = null
)

@Serializable
data class TimetableJobResultDataDto(
    val timetables: List<TimetableTemplateDto> = emptyList()
)
