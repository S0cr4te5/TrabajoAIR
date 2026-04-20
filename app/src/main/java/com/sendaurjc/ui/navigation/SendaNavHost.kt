package com.sendaurjc.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sendaurjc.SendaApplication
import com.sendaurjc.ui.screen.LoginScreen
import com.sendaurjc.ui.screen.MainScreen
import com.sendaurjc.ui.viewmodel.LoginViewModel
import com.sendaurjc.ui.viewmodel.MainViewModel

@Composable
fun SendaNavHost(app: SendaApplication) {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()
    val mainViewModel: MainViewModel = viewModel(factory = MainViewModel.Factory(app))
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(loginViewModel) {
                navController.navigate("main") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
        composable("main") {
            MainScreen(mainViewModel)
        }
    }
}
