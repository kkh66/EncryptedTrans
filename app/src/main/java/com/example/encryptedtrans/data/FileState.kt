package com.example.encryptedtrans.data

data class FileState(
    val scanResult: VirusTotalFileUploadResult? = null,
    val analysisResult: VirusTotalAnalysisResult? = null,
    val isLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Int = 0,
    val errorMessage: String? = null,
    val progressMessage: String? = null,
    val completionMessage: String? = null,
    val filesList: List<FileRecord> = emptyList(),
    val noFilesMessage: String? = null,
    var isSearchActive: Boolean = false
)