package com.example.encryptedtrans.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.encryptedtrans.Auth
import com.example.encryptedtrans.data.UserProfileImage
import com.example.encryptedtrans.data.UserProfileState
import com.example.encryptedtrans.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class UserProfileViewModel(private val auth: Auth, context: Context) : ViewModel() {
    var username by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var newEmail by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    var currentPassword by mutableStateOf("")
        private set
    var newPassword by mutableStateOf("")
        private set
    var confirmNewPassword by mutableStateOf("")
        private set

    private val _profileState = MutableStateFlow(UserProfileState())
    val profileState: StateFlow<UserProfileState> = _profileState.asStateFlow()

    var profileImagePath by mutableStateOf<String?>(null)
        private set

    private val utils = Utils()
    private val userProfileDao = AppDatabase.getDatabase(context).userProfileDao()

    private val _currentProfileImageUri = MutableStateFlow<Uri?>(null)
    val currentProfileImageUri: StateFlow<Uri?> = _currentProfileImageUri.asStateFlow()

    private val _isProfileImageLoading = MutableStateFlow(false)
    val isProfileImageLoading: StateFlow<Boolean> = _isProfileImageLoading.asStateFlow()


    init {
        loadUserProfile()
        loadProfileImage()
    }

    fun updateCurrentPassword(password: String) {
        currentPassword = password
    }

    fun updateNewPassword(password: String) {
        newPassword = password
    }

    fun updateConfirmNewPassword(password: String) {
        confirmNewPassword = password
    }

    fun resetUpdateSuccessState() {
        _profileState.update { it.copy(isUpdateSuccessful = false) }
    }

    private fun loadUserProfile() {
        val currentUser = auth.getCurrentUser()
        username = currentUser?.displayName ?: "Unknown User"
        email = currentUser?.email ?: "Unknown Email"
    }

    fun loadProfileImage() {
        viewModelScope.launch {
            _isProfileImageLoading.value = true
            try {
                val userId =
                    auth.getCurrentUser()?.uid ?: throw IllegalStateException("User not logged in")
                val userProfile = userProfileDao.getUserProfile(userId)
                profileImagePath = userProfile?.profileImagePath
                _currentProfileImageUri.value = profileImagePath?.let { Uri.parse(it) }
            } catch (e: Exception) {
                _profileState.update { it.copy(errorMessage = "Failed to load profile image: ${e.message}") }
            } finally {
                _isProfileImageLoading.value = false
            }
        }
    }

    fun deleteProfileImage() {
        viewModelScope.launch {
            _isProfileImageLoading.value = true
            try {
                val userId =
                    auth.getCurrentUser()?.uid ?: throw IllegalStateException("User not logged in")

                userProfileDao.deleteProfilePicture(userId)

                profileImagePath?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                }

                profileImagePath = null
                _currentProfileImageUri.value = null

            } catch (e: Exception) {
                _profileState.update { it.copy(errorMessage = "Failed to delete profile image: ${e.message}") }
            } finally {
                _isProfileImageLoading.value = false
            }
        }
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

    fun saveProfileImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            _isProfileImageLoading.value = true
            _currentProfileImageUri.value = uri
            try {
                val userId =
                    auth.getCurrentUser()?.uid ?: throw IllegalStateException("User not logged in")
                val file = File(context.filesDir, "profile_${userId}.jpg")

                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val userProfileImage = UserProfileImage(userId, file.absolutePath)
                userProfileDao.insertProfilePicture(userProfileImage)
                profileImagePath = file.absolutePath
                _currentProfileImageUri.value = Uri.fromFile(file)
            } catch (e: Exception) {
                _profileState.update { it.copy(errorMessage = "Failed to save profile image: ${e.message}") }
                _currentProfileImageUri.value = null
            } finally {
                _isProfileImageLoading.value = false
            }
        }
    }

    fun saveChanges() {
        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                if (!utils.validateUsername(username)) {
                    _profileState.update {
                        it.copy(
                            errorMessage = "Invalid Username",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                when (val usernameResult = auth.updateUserProfile(username)) {
                    is Auth.AuthResult.Success -> {
                        _profileState.update { it.copy(isUpdateSuccessful = true) }
                    }

                    is Auth.AuthResult.Error -> {
                        _profileState.update {
                            it.copy(
                                errorMessage = usernameResult.message,
                                isLoading = false
                            )
                        }
                    }

                    else -> {
                        _profileState.update {
                            it.copy(
                                errorMessage = "Unexpected response during update",
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _profileState.update { it.copy(errorMessage = "Update failed: ${e.message}") }
            } finally {
                _profileState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun sendEmailChangeRequest() {
        viewModelScope.launch {
            if (!utils.validateEmail(newEmail)) {
                _profileState.update { it.copy(errorMessage = "Invalid email format") }
                return@launch
            }

            if (password.isEmpty()) {
                _profileState.update { it.copy(errorMessage = "Password is required for email change") }
                return@launch
            }

            _profileState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val result = auth.updateUserEmail(newEmail, email, password)
                _profileState.update {
                    when (result) {
                        is Auth.AuthResult.Success -> it.copy(isEmailChangeSent = true)
                        is Auth.AuthResult.Error -> it.copy(errorMessage = result.message)
                        else -> it.copy(errorMessage = "Unexpected response. Please try again.")
                    }
                }
            } catch (e: Exception) {
                _profileState.update { it.copy(errorMessage = "Failed to send email change request: ${e.message}") }
            } finally {
                _profileState.update { it.copy(isLoading = false) }
                newEmail = ""
                password = ""
            }
        }
    }

    fun changePassword() {
        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true, errorMessage = null) }

            if (!utils.validatePassword(newPassword)) {
                _profileState.update {
                    it.copy(errorMessage = "Password must contain at least 8 characters, including an uppercase letter, a lowercase letter, and a number.")
                }
                return@launch
            }

            if (!utils.passwordsMatch(newPassword, confirmNewPassword)) {
                _profileState.update { it.copy(errorMessage = "Passwords do not match.") }
                return@launch
            }

            try {
                when (val result = auth.changePassword(currentPassword, newPassword)) {
                    is Auth.AuthResult.Success -> {
                        _profileState.update { it.copy(isUpdateSuccessful = true) }
                        clearPasswordFields()
                    }

                    is Auth.AuthResult.Error -> {
                        _profileState.update { it.copy(errorMessage = result.message) }
                    }

                    is Auth.AuthResult.PasswordResetSuccess -> {
                        _profileState.update { it.copy(errorMessage = "Unexpected result: Password reset success") }
                    }
                }
            } catch (e: Exception) {
                _profileState.update { it.copy(errorMessage = "Failed to change password: ${e.message}") }
            } finally {
                _profileState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun clearPasswordFields() {
        currentPassword = ""
        newPassword = ""
        confirmNewPassword = ""
    }

    fun logout() {
        viewModelScope.launch {
            auth.logoutUser()
            _profileState.update { it.copy(isLoggedOut = true) }
        }
    }
}