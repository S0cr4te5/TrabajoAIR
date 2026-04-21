package com.sendaurjc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.sendaurjc.ui.navigation.SendaNavHost
import com.sendaurjc.ui.theme.SendaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SendaTheme {
                Surface(color = androidx.compose.material3.MaterialTheme.colorScheme.background) {
                    SendaNavHost(application as SendaApplication)
                }
            }
        }
    }
}
