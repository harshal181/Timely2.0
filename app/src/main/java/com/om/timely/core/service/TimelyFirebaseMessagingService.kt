package com.om.timely.core.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.om.timely.core.domain.AuthPreferences
import com.om.timely.faculty.home.domain.repository.FacultyHomeRepository
import com.om.timely.faculty.subjects.domain.repository.SubjectRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class TimelyFirebaseMessagingService : FirebaseMessagingService() {

    private val homeRepository: FacultyHomeRepository by inject()
    private val subjectRepository: SubjectRepository by inject()
    private val authPreferences: AuthPreferences by inject()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.d("FCM", "TimelyFirebaseMessagingService created and initialised.")

    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "Message received from: ${message.from}")

        // ── Silent Data Notification ─────────────────────────────────────────
        if (message.data.isNotEmpty()) {
            val action = message.data["action"]
            Log.d("FCM", "Silent payload action: $action")

            when (action) {
                "REFRESH_TIMETABLE" -> {
                    Log.d("FCM", "Invalidating timetable cache…")
                    scope.launch {
                        try {
                            homeRepository.getScheduleForDay("Mon", 1)
                            Log.d("FCM", "Timetable re-synced via FCM.")
                        } catch (e: Exception) {
                            Log.e("FCM", "Timetable sync failed: ${e.message}")
                        }
                    }
                }

                "REFRESH_SUBJECTS" -> {
                    // Read the real department string persisted at login / cold-start.
                    // This matches the value stored in Room so the cache is correctly invalidated.
                    val dept = authPreferences.getDept()
                    if (dept.isNullOrBlank()) {
                        Log.w("FCM", "REFRESH_SUBJECTS: dept not found in prefs — skipping cache invalidation")
                        return
                    }

                    Log.d("FCM", "Invalidating subjects cache for dept=\"$dept\"…")
                    scope.launch {
                        try {
                            // 1. Fetch latest syllabuses for the department
                            subjectRepository.fetchSyllabuses(dept)
                            
                            // 2. Read them from the local database
                            val syllabuses = subjectRepository.getSyllabuses(dept).first()
                            
                            // 3. Fetch subjects for all syllabuses concurrently
                            val deferredFetches = syllabuses.map { syllabus ->
                                async { subjectRepository.fetchSubjects(syllabus.id) }
                            }
                            deferredFetches.awaitAll()

                            Log.d("FCM", "Subjects re-synced for dept=\"$dept\" via FCM.")
                        } catch (e: Exception) {
                            Log.e("FCM", "Subject sync failed: ${e.message}")
                        }
                    }
                }

                "REFRESH_NOTIFICATIONS" -> {
                    Log.d("FCM", "Invalidating notifications cache…")
                    scope.launch {
                        try {
                            // Fetch latest notifications from backend and insert into Room
                            val notificationRepo: com.om.timely.shared.notifications.domain.repository.NotificationRepository by inject()
                            notificationRepo.fetchNotifications()
                            Log.d("FCM", "Notifications re-synced via FCM.")
                        } catch (e: Exception) {
                            Log.e("FCM", "Notifications sync failed: ${e.message}")
                        }
                    }
                }
            }
        }

        // ── Visible Push Notification ────────────────────────────────────────
        message.notification?.let {
            Log.d("FCM", "Visible notification — Title: ${it.title}, Body: ${it.body}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "FCM token refreshed: $token")
        // Topic-based messaging — token does not need to be sent to backend.
    }
}
