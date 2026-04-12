package com.om.timely.faculty.subjects.presentation

import com.om.timely.core.presentation.UiText
import com.om.timely.faculty.subjects.domain.model.Subject
import com.om.timely.faculty.subjects.domain.model.Syllabus

data class SubjectManagementState(
    val isLoading: Boolean = false,
    val syllabuses: List<Syllabus> = emptyList(),
    val selectedSyllabus: Syllabus? = null,
    val subjects: List<Subject> = emptyList(),
    val filteredSubjects: List<Subject> = emptyList(),
    val searchQuery: String = "",
    val selectedSemester: Int? = null,
    val error: UiText? = null,
    
    // Bottom Sheet (Add/Edit)
    val isAddEditSheetVisible: Boolean = false,
    val subjectToEdit: Subject? = null, // null means "Add New", non-null means "Edit"
    
    // Delete Confirmation Dialog
    val subjectToDelete: Subject? = null // null means hidden, non-null means show dialog for this subject
)
