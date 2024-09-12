package com.example.encryptedtrans.data

data class AnalysisResponse(
    val data: AnalysisData
)

data class AnalysisData(
    val attributes: AnalysisAttributes
)

data class AnalysisAttributes(
    val status: String,
    val stats: VirusTotalAnalysisResult
)