package com.example.encryptedtrans.data

import com.google.gson.annotations.SerializedName


data class VirusTotalAnalysisResult(
    val malicious: Int,
    val suspicious: Int,
    val undetected: Int,
    val harmless: Int,
    val timeout: Int,
    @SerializedName("confirmed-timeout")
    val confirmedTimeout: Int,
    val failure: Int,
    @SerializedName("type-unsupported")
    val typeUnsupported: Int
)