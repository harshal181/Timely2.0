package com.om.timely

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import android.graphics.Color
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.om.timely.navigation.authNavGraph
import com.om.timely.navigation.facultyNavGraph
import com.om.timely.navigation.studentNavGraph
import com.om.timely.splash.presentation.SplashScreen
import com.om.timely.ui.theme.TimelyTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()
        
        splashScreen.setKeepOnScreenCondition { false }

        // Use light style directly since background is #F0FDF4 (Light Green)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        
        setContent {
            TimelyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                        
                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") {
                            SplashScreen(
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }
                        
                        authNavGraph(navController)
                        studentNavGraph(navController)
                        facultyNavGraph(navController)
                    }
                }
            }
        }
    }
}

