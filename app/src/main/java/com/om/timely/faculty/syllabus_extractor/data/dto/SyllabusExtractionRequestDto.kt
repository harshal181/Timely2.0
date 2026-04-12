package com.om.timely.faculty.syllabus_extractor.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyllabusExtractionRequestDto(
    @SerialName("image_urls") val imageUrls: List<String>
)
