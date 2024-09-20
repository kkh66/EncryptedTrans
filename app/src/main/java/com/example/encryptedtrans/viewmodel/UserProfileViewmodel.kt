package com.example.encryptedtrans.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.encryptedtrans.Auth
import com.example.encryptedtrans.data.UserProfileState
import com.example.encryptedtrans.utils.Utils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class UserProfileViewModel(private val auth: Auth) : ViewModel() {
    var username by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var newEmail by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    var profileState by mutableStateOf(UserProfileState())
        private set


    private val utils = Utils()

    init {
        loadUserProfile()
    }

    fun resetUpdateSuccessState() {
        profileState = profileState.copy(isUpdateSuccessful = false)
    }

    private fun loadUserProfile() {
        val currentUser = auth.getCurrentUser()
        username = currentUser?.displayName ?: "Unknown User"
        email = currentUser?.email ?: "Unknown Email"
    }

    fun updateUsername(newUsername: String) {
        username = newUsername
    }

    fun updateNewEmail(email: String) {
        newEmail = email
    }

    fun updatePassword(newPassword: String) {
        password = newPassword
    }

    fun saveChanges() {
        viewModelScope.launch {
            profileState = profileState.copy(isLoading = true, errorMessage = null)

            try {
                if (!utils.validateUsername(username)) {
                    profileState =
                        profileState.copy(errorMessage = "Invalid Username", isLoading = false)
                    return@launch
                }

                val usernameResult = auth.updateUserProfile(username)
                if (usernameResult is Auth.AuthResult.Error) {
                    profileState =
                        profileState.copy(errorMessage = usernameResult.message, isLoading = false)
                    return@launch
                }

                profileState = profileState.copy(isUpdateSuccessful = true)
            } catch (e: Exception) {
                profileState = profileState.copy(errorMessage = "Update failed: ${e.message}")
            } finally {
                profileState = profileState.copy(isLoading = false)
            }
        }
    }

    fun sendEmailChangeRequest() {
        viewModelScope.launch {
            if (!utils.validateEmail(newEmail)) {
                profileState = profileState.copy(errorMessage = "Invalid email format")
                return@launch
            }

            if (password.isEmpty()) {
                profileState =
                    profileState.copy(errorMessage = "Password is required for email change")
                return@launch
            }

            profileState = profileState.copy(isLoading = true, errorMessage = null)

            try {
                val result = auth.updateUserEmail(newEmail, email, password)
                when (result) {
                    is Auth.AuthResult.Success -> {
                        profileState = profileState.copy(isEmailChangeSent = true)
                    }

                    is Auth.AuthResult.Error -> {
                        profileState = profileState.copy(errorMessage = result.message)
                    }

                    else -> {
                        profileState =
                            profileState.copy(errorMessage = "Unexpected response. Please try again.")
                    }
                }
            } catch (e: Exception) {
                profileState =
                    profileState.copy(errorMessage = "Failed to send email change request: ${e.message}")
            } finally {
                profileState = profileState.copy(isLoading = false)
                newEmail = ""
                password = ""
            }
        }
    }


    fun logout() {
        auth.logoutUser()
        clearInputs()
        profileState = UserProfileState()
    }


    private fun clearInputs() {
        username = ""
        email = ""
        newEmail = ""
        password = ""
    }
}