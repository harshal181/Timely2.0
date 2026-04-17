package com.om.timely.faculty.timetable_generation.data.mapper

import com.om.timely.faculty.timetable_generation.data.remote.dto.TimetableDayDto
import com.om.timely.faculty.timetable_generation.data.remote.dto.TimetableSlotDto
import com.om.timely.faculty.timetable_generation.data.remote.dto.TimetableTemplateDto
import com.om.timely.faculty.timetable_generation.domain.model.*

fun TimetableTemplateDto.toDomain(): TimetableTemplate {
    return TimetableTemplate(
        // The API returns the timetable for the given template parameters.
        // We might not have a database ID for the template initially, so we can use a hash or 0.
        id = this.hashCode(), 
        department = this.dept,
        semester = this.semester,
        division = this.division,
        name = "Sem ${this.semester} — Division ${this.division}",
        days = this.days.map { it.toDomain(this.dept, this.semester) }
    )
}

fun TimetableDayDto.toDomain(dept: String, semester: Int): TimetableDay {
    return TimetableDay(
        id = this.dayIndex + 1, // Let's use 1-based IDs for drag-drop uniqueness
        dayOfWeek = this.dayOfWeek,
        dayIndex = this.dayIndex,
        slots = buildList {
            val mapped = this@toDomain.slots.map { dto ->
                val frontendIndex = if (dto.slotIndex >= 3) dto.slotIndex + 1 else dto.slotIndex
                dto.toDomain(this@toDomain.dayIndex + 1, frontendIndex, dept, semester)
            }
            addAll(mapped)
            
            // Inject recess slot at fixed index 3
            add(
                TimetableSlot(
                    id = (this@toDomain.dayIndex + 1) * 100 + 3,
                    slotIndex = 3,
                    startTime = "1:30",
                    endTime = "2:00",
                    slotKind = SlotKind.BREAK,
                    title = "RECESS",
                    assignments = emptyList()
                )
            )
            
            // Ensure proper order
            sortBy { it.slotIndex }
        }
    )
}

fun TimetableSlotDto.toDomain(dayId: Int, targetSlotIndex: Int, dept: String, semester: Int): TimetableSlot {
    // Generate a unique ID for the slot that remains stable
    val slotId = dayId * 100 + targetSlotIndex
    
    // Parse the start/end time from slot_name (e.g. "10:30-11:30")
    val timeParts = this.slotName.split("-")
    val startTime = timeParts.getOrNull(0) ?: ""
    val endTime = timeParts.getOrNull(1) ?: ""

    val slotKind = SlotKind.fromApiValue(this.slotKind)

    val assignments = this.assignments.map { dto ->
        SlotAssignment(
            batchLabel = dto.batchLabel,
            subject = TimetableSubject(
                code = dto.courseCode,
                name = dto.subjectName ?: dto.courseCode, // Fallback to code if name is missing
                abbr = dto.subjectAbbr ?: dto.courseCode.take(3), 
                semester = semester,
                department = dept
            ),
            faculty = TimetableFaculty(
                id = dto.facultyName.hashCode(), // Use hash since API currently doesn't return faculty ID
                name = dto.facultyName,
                designation = "" // Add default empty for now
            )
        )
    }

    return TimetableSlot(
        id = slotId,
        slotIndex = targetSlotIndex,
        startTime = startTime,
        endTime = endTime,
        slotKind = slotKind,
        title = if (slotKind == SlotKind.BREAK) "RECESS" else null, // Handle break natively
        assignments = assignments
    )
}
