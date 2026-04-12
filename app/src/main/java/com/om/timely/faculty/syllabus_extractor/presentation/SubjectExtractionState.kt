package com.om.timely.faculty.syllabus_extractor.presentation

import android.net.Uri
import com.om.timely.faculty.syllabus_extractor.domain.error.SyllabusProcessingError
import com.om.timely.faculty.syllabus_extractor.domain.error.SyllabusUploadError
import com.om.timely.faculty.syllabus_extractor.presentation.model.SemesterUi



data class SubjectExtractionState(
    val currentStep: Step = Step.UPLOAD,
    val semesterCategory: SemesterCategory = SemesterCategory.EVEN,
    val selectedImages: Map<Int, Uri> = emptyMap(),

    val isProcessing: Boolean = false, // True during upload AND background job
    val processingMessage: String = "", // e.g., "Uploading...", "Extracting text...", "AI parsing..."
    val sseStatus: String = "", // e.g., "pending", "processing", "completed", "failed"
    val processingProgress: Float = 0f,

    val extractedSemesters: List<SemesterUi> = emptyList(),
    val selectedSemesterTabIndex: Int = 0,
    val subjectTypeFilter: String = "All Types",
    val error: SyllabusProcessingError? = null, // Consolidated error
    val isSavingFinal: Boolean = false,

    val uploadError: SyllabusUploadError? = null,
    val processingError: SyllabusProcessingError? = null,
    val syllabusName: String = "", 
    val isSaveAsDraftEnabled: Boolean = true, // Default ON
    val loadedDraftId: Int? = null,
) {
    enum class Step {
        UPLOAD, PROCESSING, REVIEW
    }

    enum class SemesterCategory(val semesters: List<Int>) {
        ODD(listOf(1, 3, 5, 7)),
        EVEN(listOf(2, 4, 6, 8))
    }
    
    val canUpload: Boolean
        get() = selectedImages.size == 4
}
