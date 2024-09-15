package com.example.encryptedtrans.data

data class VirusTotalFileUploadResult(
    val `data`: Data
)

data class Data(
    val id: String,
    val links: Links,
    val type: String
)

data class Links(
    val self: String
)