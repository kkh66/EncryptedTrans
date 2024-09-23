package com.example.encryptedtrans.data

import java.util.Date

data class SharedFile(
    val id: String,
    val fileId: String,
    val sharedBy: String,
    val pin: String?,
    val expirationDate: Date?
)