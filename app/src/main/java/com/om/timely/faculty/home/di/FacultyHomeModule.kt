package com.om.timely.faculty.home.di

import com.om.timely.faculty.home.data.repository.FacultyHomeRepositoryImpl
import com.om.timely.faculty.home.domain.repository.FacultyHomeRepository
import com.om.timely.faculty.home.presentation.FacultyHomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val facultyHomeModule = module {
    single<FacultyHomeRepository> {
        FacultyHomeRepositoryImpl()
    }
    
    viewModel {
        FacultyHomeViewModel(get(), get())
    }
}
