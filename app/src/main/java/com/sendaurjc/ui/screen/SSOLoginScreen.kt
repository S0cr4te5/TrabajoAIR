package com.sendaurjc.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SSOLoginScreen(
    onSSOLoginClick: () -> Unit,
    onTraditionalLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título principal
        Text(
            text = "Acceso a SendaURJC",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Botón rojo principal
        Button(
            onClick = onSSOLoginClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD32F2F) // Rojo principal
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "Acceso usuarios URJC",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Texto pequeño en gris para acceso tradicional
        Text(
            text = "Otros modos de acceso",
            color = Color.Gray,
            fontSize = 14.sp,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .clickable(onClick = onTraditionalLoginClick)
                .padding(vertical = 8.dp)
        )
    }
}
