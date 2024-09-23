package com.example.encryptedtrans.data

data class ShareInfo(
    val shareId: String = "",
    val shareCode: String = "",
    val fileId: String = "",
    val ownerId: String = "",
    val pin: String? = null,
    val expiryDate: Long? = null,
    val downloadUrl: String? = null
)
