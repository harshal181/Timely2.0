import com.om.timely.core.presentation.UiText
import com.om.timely.faculty.subjects.domain.model.Syllabus
import com.om.timely.faculty.syllabus_extractor.domain.model.SyllabusDraft

data class SyllabusManagementState(
    val isLoading: Boolean = false,
    val syllabuses: List<Syllabus> = emptyList(),
    val drafts: List<SyllabusDraft> = emptyList(),
    val error: UiText? = null,
    val showDeleteConfirmationDialog: Boolean = false,
    val deletingSyllabus: Syllabus? = null,
    val deletingDraft: SyllabusDraft? = null
)
