package com.om.timely.faculty.timetable_generation.data.local

import kotlinx.serialization.Serializable

// ── Local JSON snapshot DTOs ─────────────────────────────────────────────────
// These are self-contained and separate from the remote DTOs.

@Serializable
data class LocalTemplateJson(
    val id: Int,
    val department: String,
    val semester: Int,
    val division: String,
    val name: String?,
    val days: List<LocalDayJson>
)

@Serializable
data class LocalDayJson(
    val id: Int,
    val dayOfWeek: String,
    val dayIndex: Int,
    val slots: List<LocalSlotJson>
)

@Serializable
data class LocalSlotJson(
    val id: Int,
    val slotIndex: Int,
    val startTime: String,
    val endTime: String,
    val slotKind: String,
    val title: String? = null,
    val assignments: List<LocalAssignmentJson> = emptyList()
)

@Serializable
data class LocalAssignmentJson(
    val batchLabel: String? = null,
    val subjectCode: String,
    val subjectName: String,
    val subjectAbbr: String,
    val subjectSemester: Int,
    val subjectDept: String,
    val facultyId: Int,
    val facultyName: String,
    val facultyDesignation: String,
    val roomLabel: String? = null
)
