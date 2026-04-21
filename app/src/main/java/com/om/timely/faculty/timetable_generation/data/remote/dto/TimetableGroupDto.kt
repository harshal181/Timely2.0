package com.om.timely.faculty.timetable_generation.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Summary DTO returned by GET /api/timetables/groups and /groups/details.
 * Represents a saved timetable version (group) with optional nested template summaries.
 */
@Serializable
data class TimetableGroupDto(
    val id: Int,
    val name: String,
    val dept: String,
    @SerialName("term_type") val termType: String,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("template_count") val templateCount: Int = 0,
    val semesters: List<Int> = emptyList(),
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    // Only populated by the /groups/details endpoint
    val templates: List<TimetableTemplateSummaryDto> = emptyList()
)

/**
 * Lightweight template summary as returned inside a TimetableGroupDto.
 * Does NOT include full day/slot data — use the dedicated template endpoint for that.
 */
@Serializable
data class TimetableTemplateSummaryDto(
    val id: Int,
    @SerialName("group_id") val groupId: Int,
    val dept: String,
    val semester: Int,
    val division: String,
    val name: String? = null
)

@Serializable
data class BulkPublishResponseDto(
    @SerialName("created_count") val createdCount: Int,
    val group: TimetableGroupDto,
    val templates: List<TimetableTemplateSummaryDto>
)
