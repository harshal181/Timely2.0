package com.om.timely.faculty.home.domain.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.om.timely.auth.domain.UserRole
import com.om.timely.faculty.home.domain.repository.FacultyHomeRepository
import com.om.timely.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class DailyNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val profileRepository: ProfileRepository by inject()
    private val facultyHomeRepository: FacultyHomeRepository by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val todayDate = LocalDate.now()
            
            // Skip Sunday
            if (todayDate.dayOfWeek == java.time.DayOfWeek.SUNDAY) {
                return@withContext Result.success()
            }
            
            val todayStr = todayDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            Log.d("DailyNotificationWorker", "Running daily notification job for $todayStr")
            
            val profileResult = profileRepository.getProfile()
            if (profileResult !is com.om.timely.core.domain.util.Result.Success) {
                Log.e("DailyNotificationWorker", "Profile not found or failed to fetch. Aborting.")
                return@withContext Result.failure()
            }

            val userProfile = profileResult.data
            when (userProfile.role) {
                UserRole.STUDENT -> {
                    val semester = userProfile.studentDetails?.semester ?: return@withContext Result.failure()
                    facultyHomeRepository.getScheduleForDay(todayStr, semester)
                }
                UserRole.FACULTY -> {
                    // Fetch for all semesters; the UseCase will safely filter subjects
                    for (sem in 1..8) {
                        facultyHomeRepository.getScheduleForDay(todayStr, sem)
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("DailyNotificationWorker", "Failed to run worker", e)
            Result.retry()
        }
    }
}
