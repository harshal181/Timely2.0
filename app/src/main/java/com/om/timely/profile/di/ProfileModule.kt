package com.om.timely.profile.di

import com.om.timely.profile.data.repository.ProfileRepositoryImpl
import com.om.timely.profile.domain.repository.ProfileRepository
import com.om.timely.profile.presentation.ProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
    single<ProfileRepository> {
        ProfileRepositoryImpl(get(), get())
    }
    
    viewModel {
        ProfileViewModel(get(), get())
    }
}
