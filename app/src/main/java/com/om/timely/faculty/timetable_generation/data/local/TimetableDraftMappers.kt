package com.om.timely.faculty.timetable_generation.data.local

import com.om.timely.faculty.timetable_generation.domain.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Shared lenient Json instance for local storage
val localJson = Json { ignoreUnknownKeys = true; isLenient = true }

// ── Domain → JSON ─────────────────────────────────────────────────────────────

fun List<TimetableTemplate>.toJson(): String =
    localJson.encodeToString(map { it.toLocalJson() })

fun List<String>.toWarningsJson(): String = localJson.encodeToString(this)

private fun TimetableTemplate.toLocalJson() = LocalTemplateJson(
    id = id,
    department = department,
    semester = semester,
    division = division,
    name = name,
    days = days.map { it.toLocalJson() }
)

private fun TimetableDay.toLocalJson() = LocalDayJson(
    id = id,
    dayOfWeek = dayOfWeek,
    dayIndex = dayIndex,
    slots = slots.map { it.toLocalJson() }
)

private fun TimetableSlot.toLocalJson() = LocalSlotJson(
    id = id,
    slotIndex = slotIndex,
    startTime = startTime,
    endTime = endTime,
    slotKind = slotKind.apiValue,
    title = title,
    assignments = assignments.map { it.toLocalJson() }
)

private fun SlotAssignment.toLocalJson() = LocalAssignmentJson(
    batchLabel = batchLabel,
    subjectCode = subject.code,
    subjectName = subject.name,
    subjectAbbr = subject.abbr,
    subjectSemester = subject.semester,
    subjectDept = subject.department,
    facultyId = faculty.id,
    facultyName = faculty.name,
    facultyDesignation = faculty.designation,
    roomLabel = roomLabel
)

// ── JSON → Domain ─────────────────────────────────────────────────────────────

fun String.toTemplateList(): List<TimetableTemplate> =
    localJson.decodeFromString<List<LocalTemplateJson>>(this).map { it.toDomain() }

fun String.toWarningsList(): List<String> =
    localJson.decodeFromString(this)

private fun LocalTemplateJson.toDomain() = TimetableTemplate(
    id = id,
    department = department,
    semester = semester,
    division = division,
    name = name,
    days = days.map { it.toDomain() }
)

private fun LocalDayJson.toDomain() = TimetableDay(
    id = id,
    dayOfWeek = dayOfWeek,
    dayIndex = dayIndex,
    slots = slots.map { it.toDomain() }
)

private fun LocalSlotJson.toDomain() = TimetableSlot(
    id = id,
    slotIndex = slotIndex,
    startTime = startTime,
    endTime = endTime,
    slotKind = SlotKind.fromApiValue(slotKind),
    title = title,
    assignments = assignments.map { it.toDomain() }
)

private fun LocalAssignmentJson.toDomain() = SlotAssignment(
    batchLabel = batchLabel,
    subject = TimetableSubject(
        code = subjectCode,
        name = subjectName,
        abbr = subjectAbbr,
        semester = subjectSemester,
        department = subjectDept
    ),
    faculty = TimetableFaculty(
        id = facultyId,
        name = facultyName,
        designation = facultyDesignation
    ),
    roomLabel = roomLabel
)
