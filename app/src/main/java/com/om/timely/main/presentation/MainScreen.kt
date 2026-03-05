package com.om.timely.main.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.om.timely.main.presentation.components.BottomNavigationBar
import com.om.timely.navigation.BottomNavItem
import com.om.timely.navigation.Screen
import com.om.timely.navigation.facultyBottomNavItems
import com.om.timely.navigation.studentBottomNavItems
import com.om.timely.profile.presentation.ProfileEvent
import com.om.timely.profile.presentation.ProfileScreen
import com.om.timely.profile.presentation.ProfileViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.om.timely.faculty.home.presentation.FacultyHomeScreen
import com.om.timely.faculty.home.presentation.FacultyHomeViewModel

@Composable
fun MainScreen(
    role: String,
    rootNavController: NavController
) {
    val bottomNavController = rememberNavController()
    
    val items = if (role == "STUDENT") {
        studentBottomNavItems
    } else {
        facultyBottomNavItems
    }
    
    val startDestination = if (role == "STUDENT") {
        BottomNavItem.Home.route
    } else {
        BottomNavItem.Home.route
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = bottomNavController, items = items)
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())) {
            NavHost(
                navController = bottomNavController,
                startDestination = startDestination
            ) {
                // Shared Destinations
                composable(BottomNavItem.Home.route) {
                    if (role == "FACULTY") {
                        val homeViewModel: FacultyHomeViewModel = koinViewModel()
                        val homeState by homeViewModel.state.collectAsStateWithLifecycle()
                        FacultyHomeScreen(
                            state = homeState,
                            onAction = homeViewModel::onAction,
                            events = homeViewModel.events
                        )
                    } else {
                        Text(text = "Student Home Screen")
                    }
                }
                composable(BottomNavItem.Alerts.route) {
                    Text(text = "$role Alerts Screen")
                }
                composable(BottomNavItem.Profile.route) {
                    val viewModel: ProfileViewModel = koinViewModel()
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    
                    LaunchedEffect(viewModel.events) {
                        viewModel.events.collect { event ->
                            when (event) {
                                is ProfileEvent.SignOutSuccess -> {
                                    rootNavController.navigate(Screen.AuthGraph.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                                is ProfileEvent.ShowError -> {
                                    // Could show toast or snackbar here
                                }

                                ProfileEvent.PasswordChangeSuccess -> {}
                                ProfileEvent.ProfileUpdateSuccess -> {}
                            }
                        }
                    }
                    
                    ProfileScreen(
                        state = state,
                        onAction = viewModel::onAction,
                        onNavigateToAuth = {
                            rootNavController.navigate(Screen.AuthGraph.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                
                // Student Specific Destinations
                composable(BottomNavItem.Calendar.route) {
                    Text(text = "Student Calendar Screen")
                }
                
                // Faculty Specific Destinations
                composable(BottomNavItem.Edit.route) {
                    Text(text = "Faculty Edit Screen")
                }
            }
        }
    }
}
