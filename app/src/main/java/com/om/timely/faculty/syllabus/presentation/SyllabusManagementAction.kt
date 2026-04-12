import com.om.timely.faculty.subjects.domain.model.Syllabus
import com.om.timely.faculty.syllabus_extractor.domain.model.SyllabusDraft

sealed interface SyllabusManagementAction {
    object OnExtractSyllabusClicked : SyllabusManagementAction
    data class OnDeleteSyllabusClick(val syllabus: Syllabus) : SyllabusManagementAction
    object OnConfirmDelete : SyllabusManagementAction
    object OnDismissDeleteDialog : SyllabusManagementAction
    object OnRefresh : SyllabusManagementAction
    
    // Draft Actions
    data class OnDeleteDraftClick(val draft: SyllabusDraft) : SyllabusManagementAction
    object OnConfirmDeleteDraft : SyllabusManagementAction
    data class OnResumeDraftClick(val draft: SyllabusDraft) : SyllabusManagementAction
}

sealed interface SyllabusManagementEvent {
    object NavigateToSyllabusExtractor : SyllabusManagementEvent
    data class NavigateToResumeDraft(val draftId: Int) : SyllabusManagementEvent
}
