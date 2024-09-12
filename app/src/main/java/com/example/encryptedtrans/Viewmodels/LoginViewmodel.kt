package com.example.encryptedtrans.Viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.encryptedtrans.Auth
import com.example.encryptedtrans.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val auth: Auth) : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoginSuccessful by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val utils = Utils()

    fun login() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val result = auth.loginUser(email, password)
                when (result) {
                    is Auth.AuthResult.Success -> {
                        isLoginSuccessful = true
                    }

                    is Auth.AuthResult.Error -> {
                        errorMessage = result.message
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Login failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val result = auth.signInWithGoogle(idToken)
                when (result) {
                    is Auth.AuthResult.Success -> {
                        isLoginSuccessful = true
                    }
                    is Auth.AuthResult.Error -> {
                        errorMessage = result.message
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Google Sign-In failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}

