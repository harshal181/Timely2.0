package com.om.timely.faculty.home.domain.model

import com.om.timely.faculty.home.domain.model.schedule.ScheduleSlot

data class DailyScheduleStats(
    val totalLectures: Int,
    val totalLabs: Int,
    val uniqueStudents: Int
)

data class QuickStats(
    val subjects: Int,
    val batches: Int,
    val hoursPerWeek: Int
)

data class DayScheduleInfo(
    val activeDay: String,
    val dayId: Int? = null,
    val dailyStats: DailyScheduleStats,
    val quickStats: QuickStats,
    val timeline: List<ScheduleSlot>
)
