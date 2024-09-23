package com.example.encryptedtrans.data

data class FileRecord(
    val id: String = "",
    val filename: String = "",
    val timeUpload: String = "",
    val userId: String? = null,
    val downloadUrl: String? = null,
    val isMalicious: Boolean = false
)
