package com.om.timely.faculty.home.presentation.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.om.timely.faculty.home.domain.util.DailyNotificationWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted. Rescheduling today's alarms via WorkManager.")
            
            val workRequest = OneTimeWorkRequestBuilder<DailyNotificationWorker>().build()
            
            WorkManager.getInstance(context).enqueueUniqueWork(
                "BootNotificationWorker",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}
