package com.om.timely.faculty.home.domain.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.om.timely.faculty.home.domain.model.schedule.LectureSlot
import com.om.timely.faculty.home.domain.model.schedule.PracticalSlot
import com.om.timely.faculty.home.domain.model.schedule.ScheduleSlot
import com.om.timely.faculty.home.presentation.notifications.LectureAlarmReceiver
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class NotificationScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAlarmsForTimeline(timeline: List<ScheduleSlot>, targetDay: String) {
        val formatter = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("h:mm a")
            .toFormatter(Locale.ENGLISH)
        
        // The UseCase ensures we only schedule alarms if targetDay == today.
        // So we strictly use today's date context.
        val targetDate = LocalDate.now()

        for (slot in timeline) {
            // We only care about Lectures and Practicals
            if (slot !is LectureSlot && slot !is PracticalSlot) continue

            val (subjectName, roomNo) = when (slot) {
                is LectureSlot -> slot.subjectName to slot.roomNo
                is PracticalSlot -> slot.title to (slot.batches.firstOrNull()?.roomNo ?: "Lab")
                else -> continue
            }

            try {
                // Safely extract just the time digits "H:MM" or "HH:MM"
                val rawTime = slot.startTime.uppercase().replace("AM", "").replace("PM", "").trim()
                val parts = rawTime.split(":")
                if (parts.size < 2) continue

                var hour = parts[0].toIntOrNull() ?: continue
                val minute = parts[1].take(2).toIntOrNull() ?: continue // take(2) safely ignores trailing spaces

                // Heuristic deduction for college schedules (College runs ~8 AM to ~6 PM):
                // Hours from 1 to 7 are strictly PM. E.g., "2:00" -> 14:00
                // Hours from 8 to 11 are strictly AM. E.g., "10:30" -> 10:30
                // Hour 12 is noon (PM). E.g., "12:30" -> 12:30
                if (hour in 1..7) hour += 12

                val startTime = LocalTime.of(hour, minute)
                val classDateTime = LocalDateTime.of(targetDate, startTime)
                val alarmTime = classDateTime.minusMinutes(5)

                // If this calculated alarm time is already in the past, skip it
                if (alarmTime.isBefore(LocalDateTime.now())) {
                    continue
                }

                val intent = Intent(context, LectureAlarmReceiver::class.java).apply {
                    putExtra(LectureAlarmReceiver.EXTRA_SUBJECT_NAME, subjectName)
                    putExtra(LectureAlarmReceiver.EXTRA_ROOM_NO, roomNo)
                    putExtra(LectureAlarmReceiver.EXTRA_START_TIME, slot.startTime)
                }

                // Create a unique request code for each slot
                val requestCode = slot.id.hashCode()

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Convert to epoch milliseconds
                val triggerAtMillis = alarmTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
                
                Log.d("NotificationScheduler", "Scheduled alarm for $subjectName at $alarmTime")

            } catch (e: Exception) {
                Log.e("NotificationScheduler", "Failed to parse or schedule time: ${slot.startTime}", e)
            }
        }
    }
}
