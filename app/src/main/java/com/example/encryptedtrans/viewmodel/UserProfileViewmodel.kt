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

    var profileState by mutableStateOf(UserProfileState())
        private set

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
        profileState = profileState.copy(isUpdateSuccessful = false)
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
                profileState =
                    profileState.copy(errorMessage = "Failed to load profile image: ${e.message}")
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
                profileState =
                    profileState.copy(errorMessage = "Failed to delete profile image: ${e.message}")
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
                profileState =
                    profileState.copy(errorMessage = "Failed to save profile image: ${e.message}")
                _currentProfileImageUri.value = null
            } finally {
                _isProfileImageLoading.value = false
            }
        }
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
                profileState = when (val result = auth.updateUserEmail(newEmail, email, password)) {
                    is Auth.AuthResult.Success -> {
                        profileState.copy(isEmailChangeSent = true)
                    }

                    is Auth.AuthResult.Error -> {
                        profileState.copy(errorMessage = result.message)
                    }

                    else -> {
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

    fun changePassword() {
        viewModelScope.launch {
            profileState = profileState.copy(isLoading = true, errorMessage = null)
            try {
                if (newPassword != confirmNewPassword) {
                    profileState = profileState.copy(errorMessage = "New passwords do not match")
                    return@launch
                }
                when (val result = auth.changePassword(currentPassword, newPassword)) {
                    is Auth.AuthResult.Success -> {
                        profileState = profileState.copy(isUpdateSuccessful = true)
                        clearPasswordFields()
                    }

                    is Auth.AuthResult.Error -> {
                        profileState = profileState.copy(errorMessage = result.message)
                    }

                    is Auth.AuthResult.PasswordResetSuccess -> {
                        profileState =
                            profileState.copy(errorMessage = "Unexpected result: Password reset success")
                    }
                }
            } catch (e: Exception) {
                profileState =
                    profileState.copy(errorMessage = "Failed to change password: ${e.message}")
            } finally {
                profileState = profileState.copy(isLoading = false)
            }
        }
    }

    private fun clearPasswordFields() {
        currentPassword = ""
        newPassword = ""
        confirmNewPassword = ""
    }

    fun logout() {
        auth.logoutUser()
    }
}