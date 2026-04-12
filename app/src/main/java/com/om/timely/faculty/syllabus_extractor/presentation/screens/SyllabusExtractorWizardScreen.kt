package com.om.timely.faculty.syllabus_extractor.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.Flow
import com.om.timely.faculty.syllabus_extractor.presentation.SubjectExtractionState
import com.om.timely.faculty.syllabus_extractor.presentation.SyllabusExtractorAction
import com.om.timely.faculty.syllabus_extractor.presentation.SyllabusExtractorEvent

@Composable
fun SyllabusExtractorWizardScreen(
    state: SubjectExtractionState,
    onAction: (SyllabusExtractorAction) -> Unit,
    events: Flow<SyllabusExtractorEvent>
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var isStepperVisible by remember { mutableStateOf(true) }
    var scrollAccumulator by remember { mutableFloatStateOf(0f) }

    // This connection listens to scroll events from the inner screens
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y

                // Reset accumulator if the user changes scroll direction
                if ((delta > 0 && scrollAccumulator < 0) || (delta < 0 && scrollAccumulator > 0)) {
                    scrollAccumulator = 0f
                }

                scrollAccumulator += delta

                // Thresholds prevent accidental hiding/showing from tiny finger twitches
                if (scrollAccumulator < -40f) { // User is scrolling down
                    isStepperVisible = false
                } else if (scrollAccumulator > 40f) { // User is scrolling up
                    isStepperVisible = true
                }

                // We return Offset.Zero so we don't consume the scroll;
                // we just use it to trigger our visibility state.
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(key1 = true) {
        events.collect { event ->
            when (event) {
                is SyllabusExtractorEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.error.asString(context))
                }
                is SyllabusExtractorEvent.ExtractSuccess -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is SyllabusExtractorEvent.SaveSuccess -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = androidx.compose.ui.graphics.Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(nestedScrollConnection)
        ) {
            AnimatedVisibility(
                visible = isStepperVisible,
                // Adjust the tween(500) duration (in milliseconds) to make the fade slower or faster
                enter = fadeIn(tween(500)) + expandVertically(tween(500)),
                exit = fadeOut(tween(500)) + shrinkVertically(tween(500))
            ) {
                SyllabusStepper(currentStep = state.currentStep)
            }

            when (state.currentStep) {
                SubjectExtractionState.Step.UPLOAD -> {
                    UploadSyllabusScreen(
                        state = state,
                        onAction = onAction
                    )
                }
                SubjectExtractionState.Step.PROCESSING -> {
                    ProcessingSyllabusScreen(
                        state = state
                    )
                }
                SubjectExtractionState.Step.REVIEW -> {
                    ReviewSyllabusScreen(
                        state = state,
                        onAction = onAction
                    )
                }
            }
        }
    }
}
