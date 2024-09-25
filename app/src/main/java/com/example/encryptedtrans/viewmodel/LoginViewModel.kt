package com.example.encryptedtrans.viewmodel


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.encryptedtrans.Auth
import com.example.encryptedtrans.data.LoginState
import com.example.encryptedtrans.utils.Utils
import kotlinx.coroutines.launch

class LoginViewModel(private val auth: Auth) : ViewModel() {
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var resetEmail by mutableStateOf("")
        private set

    var loginState by mutableStateOf(LoginState())
        private set

    private val utils = Utils()

    fun updateEmail(loginUser: String) {
        email = loginUser
    }

    fun updatePassword(loginPassword: String) {
        password = loginPassword
    }

    fun updateResetEmail(email: String) {
        resetEmail = email
    }

    fun login() {
        viewModelScope.launch {
            loginState = loginState.copy(isLoading = true, errorMessage = null)

            try {
                val result = auth.loginUser(email, password)
                loginState = when (result) {
                    is Auth.AuthResult.Success -> {
                        loginState.copy(isLoginSuccessful = true)
                    }

                    is Auth.AuthResult.Error -> {
                        loginState.copy(errorMessage = result.message)

                    }
                    else -> {
                        loginState.copy(errorMessage = "Unexpected response during login.")
                    }
                }
            } catch (e: Exception) {
                loginState = loginState.copy(errorMessage = "Login failed: ${e.message}")
                clearInputs()
            } finally {
                loginState = loginState.copy(isLoading = false)
            }
        }
    }


    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            loginState = loginState.copy(isLoading = true, errorMessage = null)
            try {
                val result = auth.signInWithGoogle(idToken)
                loginState = when (result) {
                    is Auth.AuthResult.Success -> {
                        loginState.copy(isLoginSuccessful = true)
                    }

                    is Auth.AuthResult.Error -> {
                        loginState.copy(errorMessage = result.message)
                    }

                    else -> {
                        loginState.copy(errorMessage = "Unexpected response during Google Sign-In.")
                    }
                }
            } catch (e: Exception) {
                loginState = loginState.copy(errorMessage = "Google Sign-In failed: ${e.message}")
            } finally {
                loginState = loginState.copy(isLoading = false)
            }
        }
    }


    fun sendPasswordResetEmail() {
        viewModelScope.launch {
            // Validate email format before proceeding
            if (!utils.validateEmail(resetEmail)) {
                loginState = loginState.copy(errorMessage = "Invalid email format")
                return@launch
            }

            loginState = loginState.copy(isLoading = true, errorMessage = null)

            try {
                val result = auth.sendPasswordResetEmail(resetEmail)
                loginState = when (result) {
                    is Auth.AuthResult.PasswordResetSuccess -> {
                        loginState.copy(isPasswordResetSent = true)
                    }

                    is Auth.AuthResult.Error -> {
                        loginState.copy(errorMessage = result.message)
                    }

                    else -> {
                        loginState.copy(errorMessage = "Unexpected response. Please try again.")
                    }
                }
            } catch (e: Exception) {
                loginState =
                    loginState.copy(errorMessage = "Failed to send reset email: ${e.message}")
            } finally {
                loginState = loginState.copy(isLoading = false)
                resetEmail = ""
            }
        }
    }


    private fun clearInputs() {
        updateEmail("")
        updatePassword("")
    }
}
