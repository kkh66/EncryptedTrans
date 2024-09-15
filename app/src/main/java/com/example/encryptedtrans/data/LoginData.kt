package com.example.encryptedtrans.data

data class LoginData(
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordResetSent: Boolean = false,
    val isAccountLinkingRequired: Boolean = false
)