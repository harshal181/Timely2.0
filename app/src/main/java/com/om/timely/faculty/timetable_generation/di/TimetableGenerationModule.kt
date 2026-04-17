package com.om.timely.faculty.timetable_generation.di

import com.om.timely.faculty.timetable_generation.data.repository.RemoteTimetableRepository
import com.om.timely.faculty.timetable_generation.domain.repository.TimetableRepository
import com.om.timely.faculty.timetable_generation.presentation.TimetableGenerationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val timetableGenerationModule = module {
    single<TimetableRepository> {
        RemoteTimetableRepository(get())
    }

//    to  be changed later
    viewModel { TimetableGenerationViewModel(get(), get(), get()) }
}
