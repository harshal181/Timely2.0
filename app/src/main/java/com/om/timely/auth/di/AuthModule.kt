package com.om.timely.auth.di

import com.om.timely.auth.data.AuthRepositoryImpl
import com.om.timely.auth.domain.AuthRepository
import com.om.timely.auth.presentation.login.LoginViewModel
import com.om.timely.auth.presentation.signup.SignupViewModel
import com.om.timely.splash.presentation.SplashViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    viewModel { SplashViewModel(get(), get(), get()) } // AuthRepository, ProfileRepository, AuthPreferences
    viewModel { LoginViewModel(get(), get()) }          // AuthRepository, ProfileRepository
    viewModel { SignupViewModel(get()) }
}
