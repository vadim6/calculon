package com.vadimski.calculon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vadimski.calculon.ui.screens.DivisionScreen
import com.vadimski.calculon.ui.screens.HomeScreen
import com.vadimski.calculon.ui.screens.MultiplicationScreen
import com.vadimski.calculon.ui.theme.CalculonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculonTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            onMultiplication = { navController.navigate("multiplication") },
                            onDivision = { navController.navigate("division") }
                        )
                    }
                    composable("multiplication") {
                        MultiplicationScreen(onBack = { navController.popBackStack() })
                    }
                    composable("division") {
                        DivisionScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
