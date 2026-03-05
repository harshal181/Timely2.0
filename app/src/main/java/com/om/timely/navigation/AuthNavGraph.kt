package com.om.timely.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.om.timely.auth.presentation.login.LoginScreen
import com.om.timely.auth.presentation.login.LoginViewModel
import com.om.timely.auth.presentation.signup.SignupScreen
import com.om.timely.auth.presentation.signup.SignupViewModel
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.authNavGraph(navController: NavController) {
    navigation(
        startDestination = Screen.Auth.Login.route,
        route = Screen.AuthGraph.route
    ) {
        composable(Screen.Auth.Login.route) {
            val viewModel: LoginViewModel = koinViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            
            LoginScreen(
                state = state,
                onAction = viewModel::onAction,
                events = viewModel.events,
                onLoginSuccess = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.AuthGraph.route) { inclusive = true }
                    }
                },
                onNavigateSignup = {
                    navController.navigate(Screen.Auth.Signup.route)
                }
            )
        }

        composable(Screen.Auth.Signup.route) {
            val viewModel: SignupViewModel = koinViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            
            SignupScreen(
                state = state,
                onAction = viewModel::onAction,
                events = viewModel.events,
                onSignupSuccess = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.AuthGraph.route) { inclusive = true }
                    }
                },
                onNavigateLogin = {
                    navController.popBackStack()
                }
            )
        }
    }
}
