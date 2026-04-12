package com.om.timely.faculty.subjects.di

import com.om.timely.core.domain.AuthPreferences
import com.om.timely.faculty.subjects.data.repository.SubjectRepositoryImpl
import com.om.timely.faculty.subjects.domain.repository.SubjectRepository
import com.om.timely.faculty.subjects.presentation.SubjectManagementViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val subjectManagementModule = module {
    single<SubjectRepository> { SubjectRepositoryImpl(get(), get(), get()) }
    viewModel { SubjectManagementViewModel(get<SubjectRepository>(),get<AuthPreferences>()) }
}
