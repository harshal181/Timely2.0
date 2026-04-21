package com.om.timely.auth.presentation.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.timely.auth.domain.AuthRepository
import com.om.timely.auth.domain.UserRole
import com.om.timely.auth.domain.error.asUiText
import com.om.timely.core.domain.util.Result
import com.om.timely.core.presentation.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignupViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SignupState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<SignupEvent>()
    val events = eventChannel.receiveAsFlow()

    // Regex for Identifiers
    private val studentIdRegex = Regex("^ABC\\d+$")
    private val facultyIdRegex = Regex("^FAC\\d+$")

    fun onAction(action: SignupAction) {
        when (action) {
            is SignupAction.OnNameChanged -> _state.update { it.copy(fullName = action.name) }
            is SignupAction.OnEmailChanged -> _state.update { it.copy(email = action.email) }
            is SignupAction.OnPasswordChanged -> _state.update { it.copy(password = action.password) }
            is SignupAction.OnRoleChanged -> _state.update { it.copy(selectedRole = action.role) }
            is SignupAction.OnTogglePasswordVisibility -> _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

            is SignupAction.OnToggleDepartmentDropdown -> _state.update { it.copy(isDepartmentDropdownExpanded = action.isExpanded) }
            is SignupAction.OnToggleCourseDropdown -> _state.update { it.copy(isCourseDropdownExpanded = action.isExpanded) }
            is SignupAction.OnToggleBatchDropdown -> _state.update { it.copy(isBatchDropdownExpanded = action.isExpanded) }

            is SignupAction.OnStudentIdChanged -> _state.update { it.copy(studentId = action.id) }
            is SignupAction.OnSemesterChanged -> _state.update { it.copy(semester = action.semester) }
            is SignupAction.OnDivisionChanged -> {
                val formatted = action.division.uppercase().take(1)
                _state.update { it.copy(division = formatted) }
            }
            is SignupAction.OnBatchChanged -> _state.update { it.copy(batch = action.batch) }
            is SignupAction.OnCourseChanged -> _state.update { it.copy(course = action.course) }
            is SignupAction.OnDepartmentChanged -> _state.update { it.copy(department = action.department) }

            is SignupAction.OnFacultyIdChanged -> _state.update { it.copy(facultyId = action.id) }
            is SignupAction.OnDesignationChanged -> _state.update { it.copy(designation = action.designation) }
            is SignupAction.OnOfficeChanged -> _state.update { it.copy(office = action.office) }

            SignupAction.OnSignInClick -> {} // Handled in UI
            SignupAction.OnSignupClick -> handleSignup()
        }
    }


    private fun handleSignup() {
        if (!validateInput()) return

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val currentState = state.value

            // Notice how clean this is! No DTOs, no null checks for faculty vs student.
            // Just raw data passing exactly what is needed for the specific role.
            val result = if (currentState.selectedRole == UserRole.STUDENT) {
                authRepository.signUpStudent(
                    name = currentState.fullName,
                    email = currentState.email,
                    password = currentState.password,
                    studId = currentState.studentId,
                    semester = (currentState.semester.toIntOrNull() ?: 1).toString(),
                    divison = currentState.division,
                    batch = currentState.batch,
                    course = currentState.department
                )
            } else {
                authRepository.signupFaculty(
                    name = currentState.fullName,
                    email = currentState.email,
                    pass = currentState.password,
                    facultyId = currentState.facultyId,
                    designation = currentState.designation,
                    office = currentState.department,
                    dept = currentState.department
                )
            }

            when (result) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    eventChannel.send(SignupEvent.SignupSuccess("auth_graph"))
                }
                is Result.Error -> {
                    // Backend errors go to snackbar via event — NOT into state
                    val errorMessage = result.error.asUiText()
                    _state.update { it.copy(isLoading = false) }
                    eventChannel.send(SignupEvent.SignupError(errorMessage))
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        val currentState = state.value

        // Helper function to update error state with UiText
        fun setError(message: String): Boolean {
            _state.update { it.copy(error = UiText.DynamicString(message)) }
            return false
        }

        if (currentState.fullName.isBlank()) return setError("Name is required")
        if (!currentState.email.contains("@")) return setError("Enter a valid email address")
        if (currentState.password.length < 8) return setError("Password must be at least 8 characters")

        if (currentState.selectedRole == UserRole.STUDENT) {
            val sem = currentState.semester.toIntOrNull() ?: 0
            if (!studentIdRegex.matches(currentState.studentId)) return setError("Student ID must start with ABC followed by numbers")
            if (sem !in 1..8) return setError("Semester must be between 1 and 8")
            if (currentState.division.uppercase() !in listOf("A", "B")) return setError("Division must be A or B")
            if (currentState.batch !in listOf("Batch 1", "Batch 2")) return setError("Batch must be Batch 1 or Batch 2")
        } else if (currentState.selectedRole == UserRole.FACULTY) {
            if (!facultyIdRegex.matches(currentState.facultyId)) return setError("Faculty ID must start with FAC followed by numbers")
            if (currentState.designation.isBlank()) return setError("Designation is required")
        }

        if (currentState.department.isBlank()) return setError("Please select a department")

        _state.update { it.copy(error = null) }
        return true
    }
}
