package com.sendaurjc.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.sendaurjc.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoginSuccess: () -> Unit) {
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val loading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mock SSO URJC")
        OutlinedTextField(
            value = username,
            onValueChange = viewModel::setUsername,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Usuario") }
        )
        OutlinedTextField(
            value = password,
            onValueChange = viewModel::setPassword,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation()
        )
        Button(
            onClick = { viewModel.login(onLoginSuccess) },
            enabled = viewModel.canLogin() && !loading,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            if (loading) CircularProgressIndicator(modifier = Modifier.padding(4.dp))
            Text("Acceder")
        }
    }
}
