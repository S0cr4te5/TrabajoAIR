package com.sendaurjc.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sendaurjc.SendaApplication
import com.sendaurjc.ui.screen.PreAlertScreen
import com.sendaurjc.ui.screen.IncidentManagementScreen
import com.sendaurjc.ui.screen.LoginScreen
import com.sendaurjc.ui.screen.MainScreen
import com.sendaurjc.ui.screen.SSOLoginScreen
import com.sendaurjc.ui.viewmodel.LoginViewModel
import com.sendaurjc.ui.viewmodel.MainViewModel

@Composable
fun SendaNavHost(app: SendaApplication, mainViewModel: MainViewModel = viewModel(factory = MainViewModel.Factory(app))) {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()
    NavHost(navController = navController, startDestination = "sso_login") {
        composable("sso_login") {
            SSOLoginScreen(
                onSSOLoginClick = {
                    navController.navigate("main") {
                        popUpTo("sso_login") { inclusive = true }
                    }
                },
                onTraditionalLoginClick = {
                    navController.navigate("traditional_login")
                }
            )
        }
        composable("traditional_login") {
            LoginScreen(loginViewModel, mainViewModel) {
                navController.navigate("main") {
                    popUpTo("traditional_login") { inclusive = true }
                }
            }
        }
        composable("main") {
            MainScreen(
                mainViewModel,
                onManageIncidents = { navController.navigate("manage_incidents") },
                onNavigateToPreAlert = { navController.navigate("pre_alert") },
                onLogout = {
                    navController.navigate("sso_login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("pre_alert") {
            PreAlertScreen(mainViewModel, onDeactivate = { 
                navController.navigate("main") {
                    popUpTo("main") { inclusive = true }
                }
            })
        }
        composable("manage_incidents") {
            IncidentManagementScreen(mainViewModel, onBack = { navController.popBackStack() })
        }
    }
}
