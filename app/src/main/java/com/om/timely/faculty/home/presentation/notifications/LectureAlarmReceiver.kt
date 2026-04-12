package com.om.timely.faculty.home.presentation.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.om.timely.MainActivity
import com.om.timely.R

class LectureAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME) ?: "Upcoming Class"
        val roomNo = intent.getStringExtra(EXTRA_ROOM_NO) ?: "Unknown Room"
        val startTime = intent.getStringExtra(EXTRA_START_TIME) ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Timetable Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for upcoming lectures and practicals"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap action opens the app
        val contentIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_APP,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Fallback icon
            .setContentTitle("Class in 5 Mins: $subjectName")
            .setContentText("Room: $roomNo | Starts at $startTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Generate unique notification ID based on subject name string
        val notificationId = subjectName.hashCode()
        notificationManager.notify(notificationId, notification)
    }

    companion object {
        const val CHANNEL_ID = "TIMELY_ALERTS"
        const val EXTRA_SUBJECT_NAME = "extra_subject_name"
        const val EXTRA_ROOM_NO = "extra_room_no"
        const val EXTRA_START_TIME = "extra_start_time"
        const val REQUEST_CODE_OPEN_APP = 1001
    }
}
