package com.om.timely.auth.presentation.signup

import com.om.timely.auth.domain.UserRole
import com.om.timely.core.presentation.UiText

data class SignupState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    
    val selectedRole: UserRole = UserRole.STUDENT,
    
    // Student specific fields
    val studentId: String = "",
    val semester: String = "",
    val division: String = "",
    val batch: String = "",
    val course: String = "", 
    val department: String = "",

    // Faculty specific fields
    val facultyId: String = "",
    val designation: String = "",
    val office: String = "", 

    // UI State
    val isLoading: Boolean = false,
    val error: UiText? = null,
    
    // Dropdown States
    val isDepartmentDropdownExpanded: Boolean = false,
    val isCourseDropdownExpanded: Boolean = false
)
