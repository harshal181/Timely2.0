package com.om.timely.shared.notifications.di

import com.om.timely.shared.notifications.data.repository.NotificationRepositoryImpl
import com.om.timely.shared.notifications.domain.repository.NotificationRepository
import com.om.timely.shared.notifications.presentation.NotificationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val notificationModule = module {
    single<NotificationRepository> {
        NotificationRepositoryImpl(
            httpClient = get(),
            notificationDao = get()
        )
    }
    
    viewModel {
        NotificationViewModel(
            repository = get(),
            profileRepository = get()
        )
    }
}

