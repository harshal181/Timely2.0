package com.om.timely.main.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
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
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.om.timely.faculty.edit.presentation.FacultyEditScreen
import com.om.timely.faculty.edit.presentation.FacultyEditViewModel
import com.om.timely.faculty.home.presentation.FacultyHomeEvent
import com.om.timely.faculty.home.presentation.FacultyHomeScreen
import com.om.timely.faculty.home.presentation.FacultyHomeViewModel
import com.om.timely.faculty.subjects.presentation.SubjectManagementAction
import com.om.timely.faculty.subjects.presentation.SubjectManagementScreen
import com.om.timely.faculty.subjects.presentation.SubjectManagementViewModel
import com.om.timely.faculty.syllabus_extractor.presentation.SyllabusExtractorViewModel
import com.om.timely.faculty.syllabus_extractor.presentation.screens.SyllabusExtractorWizardScreen
import com.om.timely.shared.notifications.presentation.NotificationScreen
import com.om.timely.shared.notifications.presentation.NotificationViewModel
import com.om.timely.student.home.presentation.StudentHomeScreen
import com.om.timely.student.home.presentation.StudentHomeViewModel
import com.om.timely.faculty.syllabus.presentation.SyllabusManagementScreen
import com.om.timely.faculty.syllabus.presentation.SyllabusManagementViewModel
import com.om.timely.profile.presentation.ProfileAction
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    role: String,
    rootNavController: NavController
) {
    val bottomNavController = rememberNavController()
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)

    val items = if (role == "STUDENT") {
        studentBottomNavItems
    } else {
        facultyBottomNavItems
    }
    
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Show bottom bar ONLY for Faculty
    val showBottomBar = role == "FACULTY" && items.any { it.route == currentRoute }

    val content = @Composable {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    BottomNavigationBar(navController = bottomNavController, items = items)
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())) {
                NavHost(
                    navController = bottomNavController,
                    startDestination = BottomNavItem.Home.route
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
                            val studentViewModel: StudentHomeViewModel = koinViewModel()
                            val studentState by studentViewModel.state.collectAsStateWithLifecycle()
                            StudentHomeScreen(
                                state = studentState,
                                onAction = studentViewModel::onAction,
                                events = studentViewModel.events,
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onNavigateToNotifications = { bottomNavController.navigate(BottomNavItem.Alerts.route) }
                            )
                        }
                    }
                    composable(
                        route = BottomNavItem.Alerts.route,
                        enterTransition = {
                            if (role == "STUDENT") {
                                androidx.compose.animation.slideInHorizontally(initialOffsetX = { it }) + 
                                androidx.compose.animation.slideInVertically(initialOffsetY = { -it })
                            } else null
                        },
                        exitTransition = {
                            if (role == "STUDENT") {
                                androidx.compose.animation.slideOutHorizontally(targetOffsetX = { -it }) + 
                                androidx.compose.animation.slideOutVertically(targetOffsetY = { it })
                            } else null
                        }
                    ) {
                        val notificationViewModel: NotificationViewModel = koinViewModel()
                        val notificationState by notificationViewModel.state.collectAsStateWithLifecycle()
                        val snackbarHostState = remember { SnackbarHostState() }
                        NotificationScreen(
                            state = notificationState,
                            onAction = notificationViewModel::onAction,
                            onNavigateBack = { bottomNavController.popBackStack() },
                            eventFlow = notificationViewModel.events,
                            snackbarHostState = snackbarHostState,
                        )
                    }
                    composable(BottomNavItem.Profile.route) {
                        val viewModel: ProfileViewModel = koinViewModel()
                        val state by viewModel.state.collectAsStateWithLifecycle()
                        
                        ProfileScreen(
                            state = state,
                            onAction = viewModel::onAction,
                            events = viewModel.events,
                            onNavigateToAuth = {
                                rootNavController.navigate(Screen.AuthGraph.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    // Faculty Specific Destinations
                    composable(BottomNavItem.Edit.route) {
                        val editViewModel: FacultyEditViewModel = koinViewModel()
                        val editState by editViewModel.state.collectAsStateWithLifecycle()
                        FacultyEditScreen(
                            state = editState,
                            onAction = editViewModel::onAction,
                            events = editViewModel.events,
                            onNavigateToSyllabus = {
                                bottomNavController.navigate("syllabus_management")
                            },
                            onNavigateToSubjects = {
                                bottomNavController.navigate("subject_management")
                            },
                            onNavigateToGenerate = {
                                rootNavController.navigate(Screen.Faculty.TimetableWizard.route)
                            }
                        )
                    }
                    
                    composable("subject_management") {
                        val subjectViewModel: SubjectManagementViewModel = koinViewModel()
                        val subjectState by subjectViewModel.state.collectAsStateWithLifecycle()
                        SubjectManagementScreen(
                            state = subjectState,
                            events = subjectViewModel.events,
                            onAction = { action ->
                                if (action == SubjectManagementAction.NavigateBack) {
                                    bottomNavController.popBackStack()
                                } else {
                                    subjectViewModel.onAction(action)
                                }
                            }
                        )
                    }
                    
                    composable("syllabus_management") {
                        val viewModel: SyllabusManagementViewModel = koinViewModel()
                        val state by viewModel.state.collectAsStateWithLifecycle()
                        SyllabusManagementScreen(
                            state = state,
                            onAction = viewModel::onAction,
                            events = viewModel.events,
                            onBackClick = { bottomNavController.popBackStack() },
                            onNavigateToExtractor = { draftId ->
                                val route = if (draftId != null) "syllabus_extractor_wizard?draftId=$draftId" else "syllabus_extractor_wizard"
                                rootNavController.navigate(route)
                            }
                        )
                    }
                }
            }
        }
    }

    if (role == "STUDENT") {
        val viewModel: ProfileViewModel = koinViewModel()
        val profileState by viewModel.state.collectAsStateWithLifecycle()
        
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = Color.White,
                    drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                    modifier = Modifier.width(300.dp)
                ) {
                    com.om.timely.main.presentation.components.StudentDrawerContent(
                        profile = profileState.profile,
                        onHomeClick = {
                            scope.launch { drawerState.close() }
                            bottomNavController.navigate(BottomNavItem.Home.route) {
                                launchSingleTop = true
                            }
                        },
                        onProfileClick = {
                            scope.launch { drawerState.close() }
                            bottomNavController.navigate(BottomNavItem.Profile.route) {
                                launchSingleTop = true
                            }
                        },
                        onLogoutClick = {
                            scope.launch { drawerState.close() }
                            viewModel.onAction(ProfileAction.OnSignOutClick)
                            rootNavController.navigate(Screen.AuthGraph.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onCloseClick = { scope.launch { drawerState.close() } }
                    )
                }
            }
        ) {
            content()
        }
    } else {
        content()
    }
}
