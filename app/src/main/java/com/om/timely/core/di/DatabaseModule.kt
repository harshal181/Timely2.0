package com.om.timely.core.di

import androidx.room.Room
import com.om.timely.core.data.local.TimelyDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            TimelyDatabase::class.java,
            TimelyDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    single {
        get<TimelyDatabase>().timetableDao
    }
    
    single {
        get<TimelyDatabase>().subjectDao
    }

    single {
        get<TimelyDatabase>().syllabusDao
    }

    single {
        get<TimelyDatabase>().notificationDao
    }

    single {
        get<TimelyDatabase>().syllabusDraftDao
    }
}
