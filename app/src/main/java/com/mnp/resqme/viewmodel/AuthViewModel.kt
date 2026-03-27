package com.mnp.resqme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnp.resqme.data.repository.AuthRepository
import com.mnp.resqme.util.UiState
import com.mnp.resqme.util.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val loginState: StateFlow<UiState<String>> = _loginState

    private val _registerState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val registerState: StateFlow<UiState<String>> = _registerState

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            if (!validateLoginInput(email, password)) return@launch

            _loginState.value = UiState.Loading

            val result = authRepository.loginUser(email, password)

            if (result.isSuccess) {
                _loginState.value = UiState.Success("Login successful")
            } else {
                _loginState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Login failed"
                )
            }
        }

    }


    fun registerUser(email: String, password: String, name: String, confirmPassword: String) {
        viewModelScope.launch {
            if (!validateRegisterInput(email, password, name, confirmPassword)) return@launch

            _registerState.value = UiState.Loading

            val result = authRepository.registerUser(email, password, name)

            if (result.isSuccess) {
                _registerState.value = UiState.Success("Registration successful")
            } else {
                _registerState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Registration failed"
                )
            }
        }
    }

    private fun validateLoginInput(email: String, password: String): Boolean {
        return when {
            email.isBlank() -> {
                _loginState.value = UiState.Error("Email cannot be empty")
                false
            }
            !ValidationUtils.isValidEmail(email) -> {
                _loginState.value = UiState.Error("Please enter a valid email")
                false
            }
            password.isBlank() -> {
                _loginState.value = UiState.Error("Password cannot be empty")
                false
            }
            else -> true
        }
    }

    private fun validateRegisterInput(
        email: String,
        password: String,
        name: String,
        confirmPassword: String
    ): Boolean {
        return when {
            name.isBlank() -> {
                _registerState.value = UiState.Error("Name cannot be empty")
                false
            }
            !ValidationUtils.   isValidName(name) -> {
                _registerState.value = UiState.Error("Name must be at least 2 characters")
                false
            }
            email.isBlank() -> {
                _registerState.value = UiState.Error("Email cannot be empty")
                false
            }
            !ValidationUtils.isValidEmail(email) -> {
                _registerState.value = UiState.Error("Please enter a valid email")
                false
            }
            password.isBlank() -> {
                _registerState.value = UiState.Error("Password cannot be empty")
                false
            }
            !ValidationUtils.isValidPassword(password) -> {
                _registerState.value = UiState.Error("Password must be at least 6 characters")
                false
            }
            password != confirmPassword -> {
                _registerState.value = UiState.Error("Passwords do not match")
                false
            }
            else -> true
        }
    }

    fun resetState() {
        _loginState.value = UiState.Idle
        _registerState.value = UiState.Idle
    }
    fun logout() {
        authRepository.logout()
        _loginState.value = UiState.Idle
        _registerState.value = UiState.Idle
    }
    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()
}