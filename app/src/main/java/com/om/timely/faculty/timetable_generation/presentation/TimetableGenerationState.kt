package com.om.timely.faculty.timetable_generation.presentation

import com.om.timely.core.presentation.UiText
import com.om.timely.faculty.subjects.domain.model.Syllabus
import com.om.timely.faculty.timetable_generation.domain.model.TimetableTemplate

enum class WizardStep {
    SCHEMA_SELECTION,
    ELECTIVE_SELECTION,
    PROCESSING,
    WARNINGS,
    REVIEW
}

data class TimetableGenerationState(
    val isLoading: Boolean = false,
    val currentStep: WizardStep = WizardStep.SCHEMA_SELECTION,
    
    // Schema Selection Data
    val availableSyllabi: List<Syllabus> = emptyList(),
    val selectedSyllabus: Syllabus? = null,
    val selectedDivision: String = "A",
    
    // Editor State
    val generatedTemplates: List<TimetableTemplate> = emptyList(),
    val warnings: List<String> = emptyList(),
    val activeTemplate: TimetableTemplate? = null,
    val undoStack: List<TimetableTemplate> = emptyList(),
    val redoStack: List<TimetableTemplate> = emptyList(),
    
    // Error Handling
    val globalError: UiText? = null
)
