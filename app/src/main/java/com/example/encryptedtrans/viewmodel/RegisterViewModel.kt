package com.example.encryptedtrans.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.encryptedtrans.Auth
import com.example.encryptedtrans.data.RegisterState
import com.example.encryptedtrans.utils.Utils
import kotlinx.coroutines.launch


class RegisterViewModel(private val auth: Auth) : ViewModel() {
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set
    var username by mutableStateOf("")
        private set

    var registerState by mutableStateOf(RegisterState())
        private set

    private val utils = Utils()

    fun updateEmail(registerEmail: String) {
        email = registerEmail
    }

    fun updatePassword(registerPassword: String) {
        password = registerPassword
    }

    fun updateConfirmPassword(registerConfirmPassword: String) {
        confirmPassword = registerConfirmPassword
    }

    fun updateUsername(registerUsername: String) {
        username = registerUsername
    }

    fun register() {
        viewModelScope.launch {
            registerState = registerState.copy(isLoading = true, errorMessage = null)

            if (!validateInputs()) {
                registerState = registerState.copy(isLoading = false)
                return@launch
            }

            try {
                val result = auth.registerUser(email, password, username)
                registerState = when (result) {
                    is Auth.AuthResult.Success -> {
                        registerState.copy(isRegistrationSuccessful = true)
                    }
                    is Auth.AuthResult.Error -> {
                        registerState.copy(errorMessage = result.message)
                    }
                    else -> {
                        registerState.copy(errorMessage = "Unexpected response during registration.")
                    }
                }
            } catch (e: Exception) {
                registerState = registerState.copy(errorMessage = "Registration failed: ${e.message}")
            } finally {
                registerState = registerState.copy(isLoading = false)
            }
        }
    }

    private fun validateInputs(): Boolean {
        val errorMessage = when {
            username.isBlank() -> "Username cannot be empty"
            !utils.validateUsername(username) -> "invalid_username_format"
            email.isBlank() -> "Email cannot be empty"
            !utils.validateEmail(email) -> "invalid_email_format"
            password.isBlank() -> "Password cannot be empty"
            !utils.validatePassword(password) -> "invalid_password_format"
            !utils.passwordsMatch(password, confirmPassword) -> "passwords_do_not_match"
            else -> null
        }

        if (errorMessage != null) {
            registerState = registerState.copy(errorMessage = errorMessage)
            return false
        }
        return true
    }

    private fun clearInputs() {
        updateEmail("")
        updatePassword("")
        updateConfirmPassword("")
        updateUsername("")
    }
}
