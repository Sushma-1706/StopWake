package com.example.stopwake

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stopwake.ui.auth.AuthScreen
import com.example.stopwake.ui.auth.SplashScreen
import com.example.stopwake.ui.home.HomeScreen
import com.example.stopwake.ui.map.MapScreen
import com.example.stopwake.ui.alerts.MyAlertsScreen
import com.example.stopwake.ui.history.HistoryScreen
import com.example.stopwake.ui.profile.ProfileScreen
import com.example.stopwake.ui.settings.AlarmSettingsScreen
import com.example.stopwake.ui.settings.BatteryOptimizationScreen
import com.example.stopwake.ui.theme.StopWakeTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StopWakeTheme {
                StopWakeApp(firebaseAuth)
            }
        }
    }
}

@Composable
fun StopWakeApp(firebaseAuth: FirebaseAuth) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController)
        }
        composable("auth") {
            AuthScreen(navController)
        }
        composable("home") {
            HomeScreen(
                onNavigateToMap = { navController.navigate("map") },
                navController = navController
            )
        }
        composable("map") {
            MapScreen(
                onNavigateBack = { navController.popBackStack() },
                navController = navController
            )
        }
        composable("alerts") {
            MyAlertsScreen(navController = navController)
        }
        composable("history") {
            HistoryScreen(navController = navController)
        }
        composable("account") {
            ProfileScreen(
                navController = navController,
                firebaseAuth = firebaseAuth
            )
        }
        composable("alarm_settings") {
            AlarmSettingsScreen(navController = navController)
        }
        composable("battery_optimization") {
            BatteryOptimizationScreen(navController = navController)
        }
    }
}
