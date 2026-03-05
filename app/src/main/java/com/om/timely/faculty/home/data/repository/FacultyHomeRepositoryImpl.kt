package com.om.timely.faculty.home.data.repository

import com.om.timely.core.domain.util.NetworkError
import com.om.timely.core.domain.util.Result
import com.om.timely.faculty.home.domain.model.DailyScheduleStats
import com.om.timely.faculty.home.domain.model.DayScheduleInfo
import com.om.timely.faculty.home.domain.model.QuickStats
import com.om.timely.faculty.home.domain.model.schedule.BatchInfo
import com.om.timely.faculty.home.domain.model.schedule.EmptySlot
import com.om.timely.faculty.home.domain.model.schedule.LectureSlot
import com.om.timely.faculty.home.domain.model.schedule.PracticalSlot
import com.om.timely.faculty.home.domain.model.schedule.RecessSlot
import com.om.timely.faculty.home.domain.repository.FacultyHomeRepository
import kotlinx.coroutines.delay

class FacultyHomeRepositoryImpl : FacultyHomeRepository {
    
    // Simulating network delay and returning mock data matching React dashboard
    override suspend fun getScheduleForDay(day: String, semester: Int): Result<DayScheduleInfo, NetworkError> {
        delay(500) 
        
        // Mock payload mimicking the React logic: 3 Lectures, 1 Lab, 280 Students
        val dailyStats = DailyScheduleStats(
            totalLectures = if (semester == 2) 2 else 3,
            totalLabs = if (semester == 2) 0 else 1,
            uniqueStudents = if (semester == 2) 150 else 280
        )
        
        // Mock payload mimicking Quick Stats: 5 Subjects, 3 Batches, 18 Hrs/week
        val quickStats = QuickStats(
            subjects = if (semester == 2) 4 else 5,
            batches = if (semester == 2) 2 else 3,
            hoursPerWeek = if (semester == 2) 14 else 18
        )
        
        val mockTimeline = if (semester == 2) {
            listOf(
                LectureSlot(
                    id = "s2_1", subjectCode = "MATH", subjectName = "Engineering Mathematics II", 
                    facultyName = "Dr. Raj Mathur", roomNo = "Room 101", 
                    startTime = "9:00 AM", endTime = "11:00 AM", colorArgb = 0xFF2563EB // Blue
                ),
                RecessSlot(
                    id = "s2_2", startTime = "11:00 AM", endTime = "12:00 PM"
                ),
                LectureSlot(
                    id = "s2_3", subjectCode = "BEE", subjectName = "Basic Electrical Engineering", 
                    facultyName = "Prof. Kiran Kumar", roomNo = "Room 104", 
                    startTime = "12:00 PM", endTime = "1:00 PM", colorArgb = 0xFFD97706 // Yellow/Orange
                ),
                EmptySlot(
                    id = "s2_4", startTime = "1:00 PM", endTime = "5:00 PM"
                )
            )
        } else {
            listOf(
                LectureSlot(
                    id = "1", subjectCode = "DBMS", subjectName = "Database Management", 
                    facultyName = "Dr. Priya Patel", roomNo = "Room 302", 
                    startTime = "9:00 AM", endTime = "10:00 AM", colorArgb = 0xFFDC2626
                ),
                LectureSlot(
                    id = "2", subjectCode = "Data Structures", subjectName = "Data Structures",
                    facultyName = "Dr. Anita Sharma", roomNo = "Room 301",
                    startTime = "10:00 AM", endTime = "11:00 AM", colorArgb = 0xFF7C3AED
                ),
                LectureSlot(
                    id = "3", subjectCode = "Operating Systems", subjectName = "Operating Systems",
                    facultyName = "Prof. Suresh Babu", roomNo = "Room 201",
                    startTime = "11:00 AM", endTime = "12:00 PM", colorArgb = 0xFF059669
                ),
                EmptySlot(
                    id = "4", startTime = "12:00 PM", endTime = "1:00 PM"
                ),
                RecessSlot(
                    id = "5", startTime = "1:00 PM", endTime = "2:00 PM"
                ),
                PracticalSlot(
                    id = "6", startTime = "2:00 PM", endTime = "4:00 PM", colorArgb = 0xFF059669,
                    batches = listOf(
                        BatchInfo(id = "b1", batchName = "Batch A", subjectName = "OS Lab", facultyName = "Prof. Suresh Babu", roomNo = "Systems Lab"),
                        BatchInfo(id = "b2", batchName = "Batch B", subjectName = "OS Lab", facultyName = "Ms. Deepa Rao", roomNo = "Systems Lab")
                    )
                ),
                EmptySlot(
                    id = "7", startTime = "4:00 PM", endTime = "5:00 PM"
                )
            )
        }
        
        return Result.Success(
            DayScheduleInfo(
                activeDay = day,
                dailyStats = dailyStats,
                quickStats = quickStats,
                timeline = mockTimeline
            )
        )
    }
}
