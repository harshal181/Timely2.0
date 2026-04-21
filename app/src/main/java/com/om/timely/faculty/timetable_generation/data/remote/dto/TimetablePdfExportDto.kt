package com.om.timely.faculty.timetable_generation.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimetablePdfExportRequestDto(
    val data: TimetableJobResultDataDto
)

@Serializable
data class TimetablePdfAssetDto(
    val label: String,
    val group: String,
    val semester: Int? = null,
    val filename: String,
    @SerialName("public_id") val publicId: String,
    val url: String,
    @SerialName("secure_url") val secureUrl: String,
    @SerialName("uploaded_at") val uploadedAt: String
)

@Serializable
data class TimetablePdfExportResponseDto(
    val status: String = "SUCCESS",
    val message: String,
    val assets: List<TimetablePdfAssetDto> = emptyList()
)
