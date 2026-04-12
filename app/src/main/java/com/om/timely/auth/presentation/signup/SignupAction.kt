package com.om.timely.auth.presentation.signup

import com.om.timely.auth.domain.UserRole
import com.om.timely.core.domain.util.Action

sealed interface SignupAction : Action {
    data class OnNameChanged(val name: String) : SignupAction
    data class OnEmailChanged(val email: String) : SignupAction
    data class OnPasswordChanged(val password: String) : SignupAction
    data class OnRoleChanged(val role: UserRole) : SignupAction
    object OnTogglePasswordVisibility : SignupAction
    
    // Dropdown toggles
    data class OnToggleDepartmentDropdown(val isExpanded: Boolean) : SignupAction
    data class OnToggleCourseDropdown(val isExpanded: Boolean) : SignupAction
    data class OnToggleBatchDropdown(val isExpanded: Boolean) : SignupAction

    // Student
    data class OnStudentIdChanged(val id: String) : SignupAction
    data class OnSemesterChanged(val semester: String) : SignupAction
    data class OnDivisionChanged(val division: String) : SignupAction
    data class OnBatchChanged(val batch: String) : SignupAction
    data class OnCourseChanged(val course: String) : SignupAction
    data class OnDepartmentChanged(val department: String) : SignupAction

    // Faculty
    data class OnFacultyIdChanged(val id: String) : SignupAction
    data class OnDesignationChanged(val designation: String) : SignupAction
    data class OnOfficeChanged(val office: String) : SignupAction

    object OnSignupClick : SignupAction
    object OnSignInClick : SignupAction 
}
