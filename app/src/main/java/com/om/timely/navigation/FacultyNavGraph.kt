package com.om.timely.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.om.timely.main.presentation.MainScreen
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.om.timely.faculty.syllabus_extractor.presentation.SyllabusExtractorAction
import androidx.compose.runtime.LaunchedEffect
import com.om.timely.faculty.syllabus_extractor.presentation.SyllabusExtractorViewModel
import com.om.timely.faculty.syllabus_extractor.presentation.screens.SyllabusExtractorWizardScreen
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.facultyNavGraph(navController: NavController) {
    navigation(
        startDestination = Screen.Faculty.Dashboard.route,
        route = Screen.FacultyGraph.route
    ) {
        composable(Screen.Faculty.Dashboard.route) {
            MainScreen(
                role = "FACULTY",
                rootNavController = navController
            )
        }

        composable(
            route = "syllabus_extractor_wizard?draftId={draftId}",
            arguments = listOf(
                navArgument("draftId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val draftId = backStackEntry.arguments?.getInt("draftId") ?: -1
            val viewModel: SyllabusExtractorViewModel = koinViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(draftId) {
                if (draftId != -1) {
                    viewModel.onAction(SyllabusExtractorAction.OnLoadDraft(draftId))
                }
            }

            SyllabusExtractorWizardScreen(
                state = state,
                onAction = viewModel::onAction,
                events = viewModel.events
            )
        }
        
        composable(Screen.Faculty.TimetableWizard.route) {
            val viewModel: com.om.timely.faculty.timetable_generation.presentation.TimetableGenerationViewModel = org.koin.androidx.compose.koinViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            
            com.om.timely.faculty.timetable_generation.presentation.screens.TimetableGenerationWizardScreen(
                state = state,
                onAction = viewModel::onAction,
                events = viewModel.events,
                rootNavController = navController
            )
        }

    }
}
