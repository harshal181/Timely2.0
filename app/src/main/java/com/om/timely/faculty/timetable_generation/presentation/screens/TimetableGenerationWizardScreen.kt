package com.om.timely.faculty.timetable_generation.presentation.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.om.timely.faculty.timetable_generation.presentation.TimetableGenerationAction
import com.om.timely.faculty.timetable_generation.presentation.TimetableGenerationEvent
import com.om.timely.faculty.timetable_generation.presentation.TimetableGenerationState
import com.om.timely.faculty.timetable_generation.presentation.WizardStep
import com.om.timely.ui.theme.BrandColors
import kotlinx.coroutines.flow.Flow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

@Composable
fun TimetableGenerationWizardScreen(
    state: TimetableGenerationState,
    onAction: (TimetableGenerationAction) -> Unit,
    events: Flow<TimetableGenerationEvent>,
    rootNavController: NavController
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(true) {
        events.collect { event ->
            when (event) {
                is TimetableGenerationEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.text.asString(context)
                    )
                }
                TimetableGenerationEvent.NavigateToDashboard -> {
                    rootNavController.navigateUp()
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BrandColors.BgFaculty,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Crossfade(
            targetState = state.currentStep,
            label = "wizard_step_transition",
            modifier = Modifier.padding(padding)
        ) { step ->
            when (step) {
                WizardStep.SCHEMA_SELECTION -> {
                    SchemaSelectionScreen(
                        state = state,
                        onAction = onAction
                    )
                }
                WizardStep.ELECTIVE_SELECTION -> {
                    ElectiveSelectionScreen(
                        state = state,
                        onAction = onAction
                    )
                }
                WizardStep.PROCESSING -> {
                    ProcessingScreen(
                        state = state
                    )
                }
                WizardStep.WARNINGS -> {
                    WarningsScreen(
                        state = state,
                        onAction = onAction
                    )
                }
                WizardStep.REVIEW -> {
                    ReviewEditorScreen(
                        state = state,
                        onAction = onAction
                    )
                }
            }
        }
    }
}
