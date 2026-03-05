package com.om.timely

import android.app.Application
import com.om.timely.auth.di.authModule
import com.om.timely.core.di.coreModule
import com.om.timely.faculty.home.di.facultyHomeModule
import com.om.timely.profile.di.profileModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class TimelyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@TimelyApp)
            modules(coreModule, authModule, profileModule, facultyHomeModule)
        }
    }
}
