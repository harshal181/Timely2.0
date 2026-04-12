package com.om.timely.faculty.home.di

import com.om.timely.faculty.home.data.repository.FacultyHomeRepositoryImpl
import com.om.timely.faculty.edit.presentation.FacultyEditViewModel
import com.om.timely.faculty.home.domain.repository.FacultyHomeRepository
import com.om.timely.faculty.home.presentation.FacultyHomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val facultyHomeModule = module {
    single {
        com.om.timely.faculty.home.domain.util.NotificationScheduler(androidContext())
    }

    single {
        com.om.timely.faculty.home.domain.use_case.ScheduleNotificationsUseCase(get(), get())
    }

    single<FacultyHomeRepository> {
        FacultyHomeRepositoryImpl(get(), get(), get())
    }
    
    viewModel {
        FacultyHomeViewModel(get(), get())
    }

    viewModel {
        FacultyEditViewModel(get(), get(), get())
    }

    viewModel {
        com.om.timely.faculty.syllabus.presentation.SyllabusManagementViewModel(get(), get(), get())
    }
}
