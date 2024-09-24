package com.example.encryptedtrans.data

data class HomeState(
    val sharedFiles: List<SharedFileWithDetails> = emptyList(),
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val requirePin: Boolean = false
)