package com.om.timely.faculty.subjects.presentation

import com.om.timely.faculty.subjects.domain.model.Subject
import com.om.timely.faculty.subjects.domain.model.Syllabus

sealed interface SubjectManagementAction {
    // Syllabus Handling
    data class OnSyllabusSelected(val syllabus: Syllabus) : SubjectManagementAction
    
    // Top Bar or Lists
    object OnAddSubjectClick : SubjectManagementAction
    data class OnEditSubjectClick(val subject: Subject) : SubjectManagementAction
    data class OnDeleteSubjectClick(val subject: Subject) : SubjectManagementAction
    
    // Filtering
    data class OnSearchQueryChange(val query: String) : SubjectManagementAction
    data class OnSemesterFilterSelected(val semester: Int?) : SubjectManagementAction
    
    // Bottom Sheet Handlers
    data class OnSaveSubject(val subject: Subject) : SubjectManagementAction
    object OnDismissSheet : SubjectManagementAction
    
    // Dialog Handlers
    object OnConfirmDelete : SubjectManagementAction
    object OnDismissDialog : SubjectManagementAction
    
    // Generics
    object NavigateBack : SubjectManagementAction
    object OnDismissError : SubjectManagementAction
}
