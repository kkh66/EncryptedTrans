package com.example.encryptedtrans.data

import com.example.encryptedtrans.data.VirusTotalAnalysisResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FileRecord(
    val filename: String,
    val timeUpload: String,
    val userId: String?,
    val downloadUrl: String?,
    val isMalicious: Boolean
)

