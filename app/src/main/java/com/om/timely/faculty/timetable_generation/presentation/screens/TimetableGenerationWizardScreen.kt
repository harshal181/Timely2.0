package com.om.timely.faculty.timetable_generation.presentation.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.om.timely.faculty.timetable_generation.presentation.TimetableGenerationAction
import com.om.timely.faculty.timetable_generation.presentation.TimetableGenerationEvent
import com.om.timely.faculty.timetable_generation.presentation.TimetableGenerationState
import com.om.timely.faculty.timetable_generation.presentation.WizardStep
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableGenerationWizardScreen(
    state: TimetableGenerationState,
    onAction: (TimetableGenerationAction) -> Unit,
    events: Flow<TimetableGenerationEvent>,
    rootNavController: NavController
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // 1. Define the scroll behavior for the TopAppBar
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

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
        // 2. Attach the nested scroll connection to the Scaffold
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Generate Timetable",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { rootNavController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = Color(0xFF111827)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                // 3. Apply the scroll behavior to the TopAppBar
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            com.om.timely.faculty.timetable_generation.presentation.components.WizardStepper(
                currentStep = state.currentStep
            )

            Crossfade(
                targetState = state.currentStep,
                label = "wizard_step_transition",
                modifier = Modifier.weight(1f)
            ) { step ->
                when (step) {
                    WizardStep.SCHEMA_SELECTION -> {
                        SchemaSelectionScreen(
                            state = state,
                            onAction = onAction
                        )
                    }

                    WizardStep.PROCESSING -> {
                        ProcessingScreen(
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
}