package com.om.timely.faculty.syllabus_extractor.di

import com.om.timely.faculty.syllabus_extractor.data.repository.CloudinaryRepositoryImpl
import com.om.timely.faculty.syllabus_extractor.data.repository.SyllabusExtractorRepositoryImpl
import com.om.timely.faculty.syllabus_extractor.domain.repository.CloudinaryRepository
import com.om.timely.faculty.syllabus_extractor.domain.repository.SyllabusExtractorRepository
import com.om.timely.faculty.syllabus_extractor.presentation.SyllabusExtractorViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val syllabusExtractorModule = module {
    // --- REAL API IMPLS (Commented out for UI testing) ---
     single<CloudinaryRepository> { CloudinaryRepositoryImpl() }
     single<SyllabusExtractorRepository> { SyllabusExtractorRepositoryImpl(get(), get()) }

    // --- DUMMY MOCKS (Plug & Play) ---
//    single<CloudinaryRepository> { DummyCloudinaryRepository() }
//    single<SyllabusExtractorRepository> { DummySyllabusExtractorRepository() }
    
    viewModel { SyllabusExtractorViewModel(get(), get(), get()) }
}

