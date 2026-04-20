package com.sendaurjc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun setUsername(value: String) { _username.value = value }
    fun setPassword(value: String) { _password.value = value }

    fun canLogin(): Boolean = _username.value.isNotBlank() && _password.value.isNotBlank()

    fun login(onSuccess: () -> Unit) {
        if (!canLogin()) return
        viewModelScope.launch {
            _isLoading.value = true
            delay(1500)
            _isLoading.value = false
            onSuccess()
        }
    }
}
