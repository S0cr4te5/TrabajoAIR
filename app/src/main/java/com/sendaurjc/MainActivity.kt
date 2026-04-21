package com.sendaurjc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sendaurjc.ui.navigation.SendaNavHost
import com.sendaurjc.ui.theme.SendaTheme
import com.sendaurjc.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val mainViewModel: MainViewModel = viewModel(factory = MainViewModel.Factory(application as SendaApplication))
            val isDarkMode by mainViewModel.isDarkMode.collectAsState()

            SendaTheme(darkTheme = isDarkMode) {
                Surface(color = androidx.compose.material3.MaterialTheme.colorScheme.background) {
                    SendaNavHost(application as SendaApplication, mainViewModel)
                }
            }
        }
    }
}
