package com.om.timely.core.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.om.timely.auth.domain.UserRole
import com.om.timely.profile.domain.model.FullProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Centralised manager for all Firebase Cloud Messaging topic operations.
 *
 * Topic naming convention (must stay in sync with SplashViewModel subscribe paths):
 * Student : semester_{n}_sync | semester_{n}_div_{d}_sync | semester_{n}_div_{d}_alerts | global
 * Faculty : faculty_{dept}_sync | global
 *
 * All operations use .await() for coroutine integration.
 * SERVICE_NOT_AVAILABLE is retried up to [MAX_RETRIES] times with exponential back-off.
 */
object FcmTopicManager {

    private const val TAG = "FCM_TOPICS"

    /** Maximum number of attempts per topic before giving up. */
    private const val MAX_RETRIES = 3

    /** Base delay in ms; doubles on each retry (500 → 1000 → 2000). */
    private const val BASE_DELAY_MS = 500L

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun subscribeForProfile(profile: FullProfile) {
        // FIX: Ask Firebase to generate/verify the token FIRST before attempting to subscribe.
        // This prevents the SERVICE_NOT_AVAILABLE crash on app startup.
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "⚠️ Fetching FCM registration token failed. Cannot subscribe to topics.", task.exception)
                return@addOnCompleteListener
            }

            // Token is ready and Google Play Services is awake. Safe to subscribe.
            appScope.launch {
                val topics = buildTopicsForProfile(profile)
                Log.i(TAG, "▶ SUBSCRIBE — ${profile.role} '${profile.name}' → $topics")
                subscribeToTopics(topics)
            }
        }
    }

    // ── Topic builders ────────────────────────────────────────────────────────

    fun buildStudentTopics(semester: Int, division: String): List<String> = listOf(
        "semester_${semester}_sync",
        "semester_${semester}_div_${division}_sync",
        "semester_${semester}_div_${division}_alerts",
        "common_topic"
    )

    fun buildFacultyTopics(department: String): List<String> = listOf(
        "faculty_${department}_sync",
        "common_topic"
    )

    /**
     * Derives the correct topic list from the profile.
     * For faculty the department is read from [FullProfile.dept] (top-level user field)
     * not from [FacultyProfile.department] which may be empty in the API response.
     */
    fun buildTopicsForProfile(profile: FullProfile): List<String> = when (profile.role) {
        UserRole.STUDENT -> {
            val sem = profile.studentDetails?.semester ?: 1
            val div = profile.studentDetails?.division?.uppercase()?.trim() ?: "A"
            buildStudentTopics(sem, div)
        }
        UserRole.FACULTY -> {
            // profile.dept is the top-level "dept" field from the /users/profile API response,
            // e.g. "Computer Science and Engineering". Sanitise it for a valid topic name.
            val rawDept = profile.dept
                ?.trim()
                ?.takeIf { it.isNotBlank() }
            // Fallback to facultyDetails.department, then a safe default
                ?: profile.facultyDetails?.department?.trim()?.takeIf { it.isNotBlank() }
                ?: "CSE"
            // Sanitise: replace spaces/special chars with underscores for a valid FCM topic
            val dept = sanitizeTopic(rawDept)
            buildFacultyTopics(dept)
        }
    }

    /**
     * Replaces characters that are invalid in FCM topic names with underscores.
     * FCM allows: a-z, A-Z, 0-9, hyphen, underscore, period.
     */
    private fun sanitizeTopic(raw: String): String =
        raw.replace(Regex("[^a-zA-Z0-9_\\-.]"), "_")
            .replace(Regex("_+"), "_") // collapse consecutive underscores
            .trim('_')


    suspend fun subscribeToTopics(topics: List<String>) {
        topics.forEach { topic -> retryTopicOperation(topic, subscribe = true) }
    }

    // ── Unsubscribe ───────────────────────────────────────────────────────────

    /** Unsubscribe from all topics relevant to the given user profile. */
    fun unsubscribeForProfile(profile: FullProfile) {
        appScope.launch {
            val topics = buildTopicsForProfile(profile)
            Log.i(TAG, "◀ UNSUBSCRIBE — ${profile.role} '${profile.name}' → $topics")
            unsubscribeFromTopics(topics)
        }
    }

    suspend fun unsubscribeFromTopics(topics: List<String>) {
        topics.forEach { topic -> retryTopicOperation(topic, subscribe = false) }
    }

    // ── Retry core ────────────────────────────────────────────────────────────

    /**
     * Attempts a subscribe or unsubscribe operation on a single topic.
     * Retries on SERVICE_NOT_AVAILABLE up to [MAX_RETRIES] times.
     */
    private suspend fun retryTopicOperation(topic: String, subscribe: Boolean) {
        val verb = if (subscribe) "Subscribe" else "Unsubscribe"
        val fm = FirebaseMessaging.getInstance()

        repeat(MAX_RETRIES) { attempt ->
            try {
                if (subscribe) {
                    fm.subscribeToTopic(topic).await()
                } else {
                    fm.unsubscribeFromTopic(topic).await()
                }

                val icon = if (subscribe) "✅" else "✅"
                Log.i(TAG, "  $icon $verb OK  → $topic")
                return   // success — exit the repeat block immediately
            } catch (e: Exception) {
                val isLastAttempt = attempt == MAX_RETRIES - 1
                val delayMs = BASE_DELAY_MS * (1 shl attempt) // 500 → 1000 → 2000

                if (isLastAttempt) {
                    Log.w(TAG, "  ⚠️ $verb FAILED (gave up after $MAX_RETRIES attempts) → $topic | ${e.message}")
                } else {
                    Log.w(TAG, "  ⏳ $verb attempt ${attempt + 1}/$MAX_RETRIES failed → $topic | ${e.message} — retrying in ${delayMs}ms")
                    delay(delayMs)
                }
            }
        }
    }
}