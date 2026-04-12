package com.om.timely.faculty.edit.domain.model

// Temporary Domain models until Data Layer is fully mapped
data class Faculty(
    val id: String,
    val name: String,
    val department: String
)

data class Room(
    val id: String,
    val roomNumber: String,
    val type: String // "Lecture" or "Lab"
)

data class EditSlotState(
    val isVisible: Boolean = false,
    val slotId: String = "",
    val isLecture: Boolean = true, // true = Lecture (1 hr), false = Practical (2 hrs)
    
    // Lecture Fields
    val lectureSubjectId: Int? = null,
    val lectureFacultyId: String? = null,
    val lectureRoomId: String? = null,
    
    // Practical Fields
    val practicalBatches: List<EditBatchState> = listOf(
        EditBatchState(id = "1", name = "Batch A"),
        EditBatchState(id = "2", name = "Batch B")
    ),
    
    // Dropdown Data Sources
    val availableFaculties: List<Faculty> = emptyList(),
    val availableRooms: List<Room> = emptyList()
)

data class EditBatchState(
    val id: String,
    val name: String,
    val subjectId: Int? = null,
    val facultyId: String? = null,
    val roomId: String? = null
)
