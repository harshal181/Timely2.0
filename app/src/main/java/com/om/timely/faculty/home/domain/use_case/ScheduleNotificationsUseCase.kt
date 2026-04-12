package com.om.timely.faculty.home.domain.use_case

import android.util.Log
import com.om.timely.auth.domain.UserRole
import com.om.timely.core.domain.util.Result
import com.om.timely.faculty.home.domain.model.schedule.LectureSlot
import com.om.timely.faculty.home.domain.model.schedule.PracticalSlot
import com.om.timely.faculty.home.domain.model.schedule.ScheduleSlot
import com.om.timely.faculty.home.domain.util.NotificationScheduler
import com.om.timely.profile.domain.repository.ProfileRepository
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class ScheduleNotificationsUseCase(
    private val profileRepository: ProfileRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(timeline: List<ScheduleSlot>, targetDay: String, targetSemester: Int) {
        val todayStr = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
        Log.d("ScheduleNotifications", "Attempting schedule, TargetDay: $targetDay, Today: $todayStr")
        
        if (!targetDay.equals(todayStr, ignoreCase = true)) {
            Log.d("ScheduleNotifications", "Skipping alarm scheduling: Target day ($targetDay) is not today ($todayStr)")
            return
        }

        val profileResult = profileRepository.getProfile()
        if (profileResult !is Result.Success) {
            Log.e("ScheduleNotifications", "Failed to fetch profile. Skipping alarms.")
            return
        }

        val userProfile = profileResult.data
        val filteredTimeline = when (userProfile.role) {
            UserRole.STUDENT -> {
                val enrolledSemester = userProfile.studentDetails?.semester ?: -1
                if (enrolledSemester != targetSemester) {
                    Log.d("ScheduleNotifications", "Skipping alarms for STUDENT: looking at sem $targetSemester but enrolled in sem $enrolledSemester")
                    emptyList()
                } else {
                    Log.d("ScheduleNotifications", "Scheduling ${timeline.size} alarms for STUDENT in current semester ($targetSemester).")
                    timeline
                }
            }
            UserRole.FACULTY -> {
                val taughtSubjects = userProfile.facultyDetails?.subjects ?: emptyList()
                val facultyName = userProfile.name

                if (taughtSubjects.isEmpty()) {
                    Log.d("ScheduleNotifications", "Faculty has no taught subjects mapped. Fallback to name matching? For now just checking name matching too for safety")
                }

                val matchedSlots = timeline.filter { slot ->
                    var isMatch = false
                    when (slot) {
                        is LectureSlot -> {
                            val matchesSubject = taughtSubjects.any { 
                                it.name.equals(slot.subjectName, ignoreCase = true) || 
                                slot.subjectCode.contains(it.code, ignoreCase = true) 
                            }
                            val matchesName = slot.facultyName.contains(facultyName, ignoreCase = true)
                            isMatch = matchesSubject || matchesName
                        }
                        is PracticalSlot -> {
                            val titleMatches = taughtSubjects.any { it.name.equals(slot.title, ignoreCase = true) }
                            val batchesMatch = slot.batches.any { batch -> 
                                taughtSubjects.any { it.name.equals(batch.subjectName, ignoreCase = true) } || batch.facultyName.contains(facultyName, ignoreCase = true)
                            }
                            isMatch = titleMatches || batchesMatch
                        }
                        else -> { isMatch = false }
                    }
                    if (!isMatch) {
                        Log.d("ScheduleNotifications", "Skipping slot: not taught by faculty (${if (slot is LectureSlot) slot.subjectName else "Lab"}).")
                    }
                    isMatch
                }
                
                Log.d("ScheduleNotifications", "Scheduling ${matchedSlots.size} alarms for FACULTY.")
                matchedSlots
            }
        }

        if (filteredTimeline.isNotEmpty()) {
            notificationScheduler.scheduleAlarmsForTimeline(filteredTimeline, targetDay)
        }
    }
}
