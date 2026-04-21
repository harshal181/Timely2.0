package com.om.timely.faculty.timetable_generation.di

import com.om.timely.faculty.timetable_generation.data.repository.LocalTimetableDraftRepository
import com.om.timely.faculty.timetable_generation.data.repository.RemoteTimetableRepository
import com.om.timely.faculty.timetable_generation.domain.repository.TimetableDraftRepository
import com.om.timely.faculty.timetable_generation.domain.repository.TimetableRepository
import com.om.timely.faculty.timetable_generation.presentation.TimetableGenerationViewModel
import com.om.timely.faculty.timetable_management.presentation.TimetableManagementViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val timetableGenerationModule = module {
    single<TimetableRepository> {
        RemoteTimetableRepository(get())
    }

    single<TimetableDraftRepository> {
        LocalTimetableDraftRepository(get<com.om.timely.core.data.local.TimelyDatabase>().timetableDraftDao)
    }

    viewModel { TimetableGenerationViewModel(get(), get(), get(), get()) }
    viewModel { TimetableManagementViewModel(get(), get()) }
}
