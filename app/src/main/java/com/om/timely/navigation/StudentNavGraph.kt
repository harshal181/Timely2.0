package com.om.timely.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.om.timely.main.presentation.MainScreen

fun NavGraphBuilder.studentNavGraph(navController: NavController) {
    navigation(
        startDestination = Screen.Student.Home.route,
        route = Screen.StudentGraph.route
    ) {
        composable(Screen.Student.Home.route) {
            MainScreen(
                role = "STUDENT",
                rootNavController = navController
            )
        }
    }
}
