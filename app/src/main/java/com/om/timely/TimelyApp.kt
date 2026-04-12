package com.om.timely

import android.app.Application
import com.om.timely.auth.di.authModule
import com.om.timely.core.di.coreModule
import com.om.timely.core.di.databaseModule
import com.om.timely.faculty.home.di.facultyHomeModule
import com.om.timely.faculty.subjects.di.subjectManagementModule
import com.om.timely.faculty.syllabus_extractor.di.syllabusExtractorModule
import com.om.timely.profile.di.profileModule
import com.om.timely.student.home.di.studentHomeModule
import com.om.timely.shared.notifications.di.notificationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.om.timely.faculty.home.domain.util.DailyNotificationWorker
import java.util.concurrent.TimeUnit

class TimelyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@TimelyApp)
            modules(coreModule, databaseModule, authModule, profileModule, facultyHomeModule, studentHomeModule, subjectManagementModule, syllabusExtractorModule, notificationModule)
        }
        
        try {
            val config = mapOf(
                "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
                "api_key" to BuildConfig.CLOUDINARY_API_KEY,
                "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
            )
            com.cloudinary.android.MediaManager.init(this, config)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Enqueue daily background sync for alarm scheduling
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyNotificationWorker>(
            24, TimeUnit.HOURS
        ).build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyNotificationWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }
}
