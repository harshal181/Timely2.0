package com.om.timely.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.om.timely.main.presentation.MainScreen

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
    }
}
