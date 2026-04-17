package com.om.timely.faculty.timetable_generation.domain.model

// ─────────────────────────────────────────────────────────────────────────────
//  Enums
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The kind of an individual time-slot cell.
 *
 * API values (slot_kind):
 *   "lecture"    → a 1-hour class assigned to a subject + faculty
 *   "tutorial"   → a 1-hour tutorial session
 *   "practical"  → a 2-hour lab session split across batches
 *   "wholeprac"  → a 2-hour lab session for the whole class
 *   "break"      → a fixed non-draggable recess slot
 *   "empty"      → a free slot (no assignment)
 */
enum class SlotKind(val apiValue: String) {
    LECTURE("lecture"),
    TUTORIAL("tutorial"),
    PRACTICAL("practical"),
    WHOLE_CLASS_PRACTICAL("wholeprac"),
    BREAK("break"),
    EMPTY("empty");

    /** True for any 2-hour practical variant */
    val is2Hour: Boolean get() = this == PRACTICAL || this == WHOLE_CLASS_PRACTICAL

    companion object {
        fun fromApiValue(value: String): SlotKind =
            entries.firstOrNull { it.apiValue == value.lowercase() } ?: EMPTY
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Subject summary  (fetched alongside the timetable from /subjects or embedded)
// ─────────────────────────────────────────────────────────────────────────────

data class TimetableSubject(
    val code: String,       // "BCS2T07"
    val name: String,       // "Python Programming"
    val abbr: String,       // "PY"
    val semester: Int,      // 2
    val department: String  // "Computer Science"
)

// ─────────────────────────────────────────────────────────────────────────────
//  Faculty summary  (fetched alongside the timetable from /faculty or embedded)
// ─────────────────────────────────────────────────────────────────────────────

data class TimetableFaculty(
    val id: Int,
    val name: String,           // "Prof. Disha Deotale"
    val designation: String     // "Assistant Professor"
)

// ─────────────────────────────────────────────────────────────────────────────
//  Assignment  — one batch × subject × faculty entry inside a slot
//
//  Maps to a single element of the API "assignments" array.
//  For LECTURE / TUTORIAL / WHOLE_CLASS_PRACTICAL there is exactly 1.
//  For PRACTICAL there are 1-N (one per batch running in parallel).
// ─────────────────────────────────────────────────────────────────────────────

data class SlotAssignment(
    /** e.g. "Batch 1", "Batch A", null for whole-class */
    val batchLabel: String?,
    val subject: TimetableSubject,
    val faculty: TimetableFaculty,
    /** Room / lab label, may be null if not assigned */
    val roomLabel: String? = null
)

// ─────────────────────────────────────────────────────────────────────────────
//  Timetable Slot  — one cell in the grid
//
//  Maps directly to one entry in the API week's day map.
//  slot_index is the 1-based position (1=10:30, 2=11:30 …)
// ─────────────────────────────────────────────────────────────────────────────

data class TimetableSlot(
    /** Stable identity used in undo/redo and drag tracking */
    val id: Int,
    /** 0-based row index, matching TIME_ROWS in the UI */
    val slotIndex: Int,
    /** Display string, e.g. "10:30 AM" — derived from slotIndex in the UI */
    val startTime: String,
    val endTime: String,
    val slotKind: SlotKind,
    /** Non-null for BREAK ("RECESS") or empty-label overrides */
    val title: String? = null,
    val assignments: List<SlotAssignment> = emptyList()
) {
    /** Convenience: true when this slot spans 2 visual rows */
    val is2Hour: Boolean get() = slotKind.is2Hour

    /** Convenience: true when this slot has no assignments to display */
    val isEmpty: Boolean get() = slotKind == SlotKind.EMPTY || assignments.isEmpty() && slotKind != SlotKind.BREAK
}

// ─────────────────────────────────────────────────────────────────────────────
//  Timetable Day  — one column in the grid
// ─────────────────────────────────────────────────────────────────────────────

data class TimetableDay(
    val id: Int,
    /** "Mon", "Tue", … — exactly as the API returns */
    val dayOfWeek: String,
    /** 0-based column index */
    val dayIndex: Int,
    /** Always 7 elements, one per TIME_ROWS entry */
    val slots: List<TimetableSlot>
) {
    init {
        require(slots.size == 7) {
            "TimetableDay '$dayOfWeek' must have exactly 7 slots (one per time row), got ${slots.size}"
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Timetable Template  — the top-level entity returned by the generator
// ─────────────────────────────────────────────────────────────────────────────

data class TimetableTemplate(
    val id: Int,
    val department: String,
    val semester: Int,
    val division: String,
    /** Human-readable label, e.g. "Sem II — Division A" */
    val name: String? = null,
    val days: List<TimetableDay>
)

// ─────────────────────────────────────────────────────────────────────────────
//  Timetable Generation Result
// ─────────────────────────────────────────────────────────────────────────────

data class TimetableGenerationResult(
    val templates: List<TimetableTemplate>,
    val warnings: List<String>
)
