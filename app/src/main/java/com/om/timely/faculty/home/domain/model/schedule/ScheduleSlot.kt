package com.om.timely.faculty.home.domain.model.schedule

sealed interface ScheduleSlot {
    val id: String
    val startTime: String
    val endTime: String
}

data class LectureSlot(
    override val id: String,
    val subjectCode: String,
    val subjectName: String,
    val facultyName: String,
    val roomNo: String,
    override val startTime: String,
    override val endTime: String,
    val colorArgb: Long
) : ScheduleSlot

data class PracticalSlot(
    override val id: String,
    val title: String = "PRACTICAL SESSION",
    override val startTime: String,
    override val endTime: String,
    val colorArgb: Long,
    val batches: List<BatchInfo>
) : ScheduleSlot

data class BatchInfo(
    val id: String,
    val batchName: String,
    val subjectName: String,
    val facultyName: String,
    val roomNo: String
)

data class RecessSlot(
    override val id: String,
    val title: String = "Lunch Break",
    override val startTime: String,
    override val endTime: String
) : ScheduleSlot

data class EmptySlot(
    override val id: String,
    val title: String = "No class scheduled",
    override val startTime: String,
    override val endTime: String
) : ScheduleSlot
