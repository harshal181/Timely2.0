package com.om.timely.faculty.subjects.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyllabusDto(
    val id: Int,
    val name: String,
    val dept: String,
    @SerialName("term_type")
    val termType: String,
    @SerialName("created_at")
    val createdAt: String
)
