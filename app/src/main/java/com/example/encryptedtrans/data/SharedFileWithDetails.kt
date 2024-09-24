package com.example.encryptedtrans.data

data class SharedFileWithDetails(
    val sharedFile: SharedFile,
    val fileRecord: FileRecord?,
    val sharerUsername: String
)