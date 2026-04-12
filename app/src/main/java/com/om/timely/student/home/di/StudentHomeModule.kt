package com.om.timely.student.home.di

import com.om.timely.student.home.presentation.StudentHomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val studentHomeModule = module {
    viewModel { StudentHomeViewModel(get(), get()) }
}
