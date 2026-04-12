package com.om.timely.faculty.syllabus_extractor.presentation

import android.net.Uri
import com.om.timely.faculty.syllabus_extractor.presentation.SubjectExtractionState.SemesterCategory

sealed interface SyllabusExtractorAction {
    // Upload Phase
    data class OnCategoryChanged(val category: SemesterCategory) : SyllabusExtractorAction
    data class OnImagePicked(val semesterNumber: Int, val uri: Uri) : SyllabusExtractorAction
    data class OnRemoveImage(val semesterNumber: Int) : SyllabusExtractorAction
    data object OnSubmitUpload : SyllabusExtractorAction

    // Review Phase
    data class OnFilterChanged(val type: String) : SyllabusExtractorAction
    data class OnSemesterTabSelected(val index: Int) : SyllabusExtractorAction
    data class OnEditSubjectClicked(val subjectId: String) : SyllabusExtractorAction
    data class OnSubjectFieldChanged(
        val subjectId: String,
        val field: SubjectField,
        val newValue: String
    ) : SyllabusExtractorAction
    data class OnSaveSubjectEdit(val subjectId: String) : SyllabusExtractorAction
    data class OnCancelSubjectEdit(val subjectId: String) : SyllabusExtractorAction

    // Final Action
    data class OnSyllabusNameChange(val name: String) : SyllabusExtractorAction
    data object OnConfirmAndSaveAll : SyllabusExtractorAction
    data object OnDismissError : SyllabusExtractorAction
    data class OnToggleSaveAsDraft(val enabled: Boolean) : SyllabusExtractorAction
    data class OnLoadDraft(val draftId: Int) : SyllabusExtractorAction
}

enum class SubjectField {
    NAME, CODE, LECTURE, TUTORIAL, PRACTICAL, TYPE, ELECTIVE_GROUP
}
