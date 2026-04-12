package com.om.timely.faculty.syllabus_extractor.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyllabusJobCreateResponseDto(
    @SerialName("job_id") val jobId: String,
    val status: String,
    val message: String? = null
)

@Serializable
data class SyllabusJobResponseDto(
    @SerialName("job_id") val jobId: String,
    val status: String, // "pending", "processing", "completed", "failed"
    val error: String? = null,
    @SerialName("image_results") val imageResults: List<ImageResultDto>? = null,
    @SerialName("parsed_data") val parsedData: SyllabusExtractionResponseDto? = null
)

@Serializable
data class ImageResultDto(
    @SerialName("image_url") val imageUrl: String,
    @SerialName("extracted_text") val extractedText: String? = null,
    @SerialName("parsed_data") val parsedData: SyllabusExtractionResponseDto? = null
)
