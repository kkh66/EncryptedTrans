package com.example.encryptedtrans.Viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.encryptedtrans.Auth
import com.example.encryptedtrans.utils.Utils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterViewModel(private val auth: Auth) : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var username by mutableStateOf("")
    var errorMessage by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)
    var isRegistrationSuccessful by mutableStateOf(false)


    private val utils = Utils()
    private val db = FirebaseFirestore.getInstance()

    fun register() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            // Input validation
            if (!validateInputs()) {
                isLoading = false
                return@launch
            }

            try {
                val result = auth.registerUser(email, password, username)
                when (result) {
                    is Auth.AuthResult.Success -> {
                        // Add user data to Firestore
                        addUserToFirestore(result.user.uid)
                        isRegistrationSuccessful = true
                    }

                    is Auth.AuthResult.Error -> {
                        errorMessage = result.message
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Registration failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun validateInputs(): Boolean {
        return when {
            username.isBlank() -> {
                errorMessage = "Username cannot be empty"
                false
            }

            !utils.validateUsername(username) -> {
                errorMessage =
                    "Invalid username format. It should be 3-20 characters long and can only contain letters, numbers, and underscores."
                false
            }

            email.isBlank() -> {
                errorMessage = "Email cannot be empty"
                false
            }

            !utils.validateEmail(email) -> {
                errorMessage = "Invalid email format"
                false
            }

            password.isBlank() -> {
                errorMessage = "Password cannot be empty"
                false
            }

            !utils.validatePassword(password) -> {
                errorMessage =
                    "Invalid password. It should be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number."
                false
            }

            !utils.passwordsMatch(password, confirmPassword) -> {
                errorMessage = "Passwords do not match"
                false
            }

            else -> true
        }
    }

    private suspend fun addUserToFirestore(userId: String) {
        try {
            val user = hashMapOf(
                "username" to username,
                "email" to email
            )
            db.collection("users").document(userId).set(user).await()
        } catch (e: Exception) {
            errorMessage = "Failed to save user data: ${e.message}"
        }
    }
}