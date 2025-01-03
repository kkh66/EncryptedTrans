package com.example.encryptedtrans.data

data class UserProfileState(
    val isLoading: Boolean = false,
    val isUpdateSuccessful: Boolean = false,
    val isEmailChangeSent: Boolean = false,
    val isEmailVerificationInProgress: Boolean = false,
    val isEmailVerified: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedOut: Boolean = false
)