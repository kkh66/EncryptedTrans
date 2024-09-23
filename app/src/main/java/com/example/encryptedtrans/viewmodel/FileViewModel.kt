package com.example.encryptedtrans.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.encryptedtrans.Auth
import com.example.encryptedtrans.BuildConfig
import com.example.encryptedtrans.data.FileData
import com.example.encryptedtrans.data.FileRecord
import com.example.encryptedtrans.data.FileState
import com.example.encryptedtrans.data.User
import com.example.encryptedtrans.data.VirusTotalAnalysisResult
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileViewModel(private val auth: Auth, context: Context) : ViewModel() {

    private val notification = notification(context)
    private val _fileState = MutableStateFlow(FileState())
    val fileState: StateFlow<FileState> = _fileState
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _usersList = MutableStateFlow<List<User>>(emptyList())
    val usersList: StateFlow<List<User>> = _usersList.asStateFlow()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference


    init {
        getFile(auth.getCurrentUser()?.uid)
    }

    fun refreshFiles() {
        viewModelScope.launch {
            setLoading(true)
            delay(1000)
            getFile(auth.getCurrentUser()?.uid)
            setLoading(false)
        }
    }


    private fun setErrorMessage(message: String) {
        _fileState.value = _fileState.value.copy(errorMessage = message)
    }

    private fun setLoading(isLoading: Boolean) {
        _fileState.value = _fileState.value.copy(isLoading = isLoading)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val filteredFilesList: StateFlow<List<FileRecord>> = _searchQuery
        .combine(_fileState.map { it.filesList }) { query, filesList ->
            if (query.isEmpty()) {
                emptyList()
            } else {
                filesList.filter { it.filename.contains(query, ignoreCase = true) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Get the User for select
     */
    fun getUsers() {
        viewModelScope.launch {
            try {
                val result = firestore.collection("users").get().await()
                val users = result.documents.mapNotNull { doc ->
                    val id = doc.id
                    val name = doc.getString("username")
                    val email = doc.getString("email")
                    if (name != null && email != null) {
                        User(id = id, name = name, email = email)
                    } else null
                }
                _usersList.value = users
            } catch (e: Exception) {
                setErrorMessage("Failed to get users")
            }
        }
    }


    /**
     * Share the file function use
     */
    fun shareFile(
        fileRecord: FileRecord?,
        userIds: List<String>,
        pin: String?,
        expirationDate: Date?
    ) {
        if (fileRecord == null) return
        viewModelScope.launch {
            try {
                val shareData = hashMapOf(
                    "fileId" to fileRecord.id,
                    "sharedBy" to auth.getCurrentUser()?.uid,
                    "sharedWith" to userIds,
                    "pin" to pin,
                    "expirationDate" to expirationDate?.let { Timestamp(it) },
                    "timestamp" to FieldValue.serverTimestamp()
                )
                firestore.collection("shared_files")
                    .add(shareData)
                    .await()

            } catch (e: Exception) {
                setErrorMessage("Failed to share file: ${e.localizedMessage}")
            }
        }
    }

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://www.virustotal.com/api/v3/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .header("x-apikey", BuildConfig.API_KEY) // Secure API key
                        .method(original.method, original.body)
                        .build()
                    chain.proceed(request)
                }
                .build()
        )
        .build()

    private val virusTotalApi: VirusTotalApi = retrofit.create(VirusTotalApi::class.java)

    /**
     * Get files with the current user from Firestore.
     */
    private fun getFile(userId: String?) {
        if (userId == null) {
            setErrorMessage("Please login.")
            setLoading(false)
            return
        }
        viewModelScope.launch {
            setLoading(true)

            try {
                val result = firestore.collection("files")
                    .document(userId)
                    .collection("userFiles")
                    .get()
                    .await()

                val files = result.documents.map { doc ->
                    val formattedDate = doc.getString("timeUpload") ?: "Unknown Date"
                    FileRecord(
                        id = doc.id,
                        filename = doc.getString("filename") ?: "Unknown",
                        timeUpload = formattedDate,
                        userId = doc.getString("userId"),
                        downloadUrl = doc.getString("downloadUrl"),
                        isMalicious = doc.getBoolean("isMalicious") ?: false
                    )
                }
                if (files.isEmpty()) {
                    setErrorMessage("No files found")
                    setLoading(false)
                    _fileState.value = _fileState.value.copy(
                        filesList = files
                    )
                } else {
                    setLoading(false)
                    _fileState.value = _fileState.value.copy(
                        filesList = files
                    )
                }
            } catch (e: Exception) {
                setErrorMessage("Failed to get files")
                setLoading(false)
            }
        }
    }

    /**
     * Scans a file using the VirusTotal API.
     * Uploads the file for scanning and handles the analysis result.
     */
    fun scanFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            setLoading(true)
            notification.showScanNotification("Uploading File", "Starting upload...", 0)
            _fileState.value = _fileState.value.copy(
                progressMessage = "Starting virus scan...",
                errorMessage = null,
                completionMessage = null
            )

            try {
                withContext(Dispatchers.IO) {
                    val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                        ?: throw Exception("Failed to open file stream")

                    val originalFileName = getFileName(context, uri)
                    val tempFile = File(context.cacheDir, originalFileName).apply {
                        FileOutputStream(this).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    val requestFile =
                        tempFile.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                    val body =
                        MultipartBody.Part.createFormData("file", originalFileName, requestFile)

                    // Upload the file to VirusTotal for scanning
                    val response = virusTotalApi.uploadFile(body)
                    _fileState.value = _fileState.value.copy(
                        progressMessage = "File uploaded. Checking analysis status..."
                    )
                    notification.showScanNotification(
                        "Scanning File",
                        "File uploaded, scanning in progress...",
                        33
                    )

                    checkAnalysisStatus(response.data.id, tempFile, originalFileName)
                }
            } catch (e: Exception) {
                setErrorMessage("Fail to Scan the File")
                setLoading(false)
            }
        }
    }

    /**
     * Retrieves the filename from the given URI.
     */
    private fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        result = it.getString(nameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path?.substringAfterLast('/')
        }
        return result ?: "unknown_file"
    }

    /**
     * Periodically checks the analysis status of the uploaded file.
     */
    private suspend fun checkAnalysisStatus(
        analysisId: String,
        file: File,
        originalFileName: String
    ) {
        while (true) {
            try {
                val analysisResponse = virusTotalApi.getAnalysisResult(analysisId)
                when (analysisResponse.data.attributes.status) {
                    "completed" -> {
                        val analysisStats = analysisResponse.data.attributes.stats
                        _fileState.value = _fileState.value.copy(
                            analysisResult = analysisStats,
                            progressMessage = "Analysis completed. Checking results..."
                        )
                        notification.showScanNotification(
                            "Checking Results",
                            "Analysis completed, checking results...",
                            66
                        )
                        handleAnalysisResult(file, analysisStats, originalFileName)
                        break
                    }

                    "queued", "in-progress" -> {
                        _fileState.value =
                            _fileState.value.copy(progressMessage = "Analysis in progress. Waiting...")
                        notification.showScanNotification(
                            "Scanning File",
                            "Scan in progress, please wait...",
                            50
                        )
                        delay(3000) // Wait for 3 seconds before checking again
                    }

                    else -> {
                        setErrorMessage("Unexpected analysis status: ${analysisResponse.data.attributes.status}")
                        notification.showScanNotification(
                            "Scan Failed",
                            "Unexpected analysis status."
                        )
                        break
                    }
                }
            } catch (e: Exception) {
                setErrorMessage("Error checking analysis status: ${e.localizedMessage}")
                notification.showScanNotification("Scan Failed", "Error checking analysis status")
                break
            }
        }
    }

    /**
     * Handles the results of the VirusTotal analysis.
     * Saves the file to Firebase Storage and Firestore if safe.
     */
    private suspend fun handleAnalysisResult(
        file: File,
        result: VirusTotalAnalysisResult,
        originalFileName: String
    ) {
        if (result.malicious > 0) {
            _fileState.value = _fileState.value.copy(
                progressMessage = "The File got problem the file will not saved"
            )
            notification.showScanNotification(
                "Malicious File Detected",
                "The file is malicious and will not be saved."
            )
            saveAnalysisResult(originalFileName, isMalicious = false)
        } else {
            _fileState.value =
                _fileState.value.copy(progressMessage = "File is safe. Saving to Firebase...")
            notification.showScanNotification(
                "Saving File",
                "Analysis complete, saving to Firebase...",
                75
            )
            saveToFirebase(file, originalFileName)
        }
        withContext(Dispatchers.IO) {
            if (file.exists()) {
                file.delete()
            }
        }
    }

    /**
     * Saves safe files to Firebase Storage and records metadata in Firestore.
     */
    private suspend fun saveToFirebase(
        file: File,
        originalFileName: String
    ) {
        try {
            val fileUri = Uri.fromFile(file)
            val sanitizedFileName = sanitizeFileName(originalFileName)
            val storageRef = storage.child("files/${auth.getCurrentUser()?.uid}/$sanitizedFileName")
            storageRef.putFile(fileUri).await()

            val downloadUrl = storageRef.downloadUrl.await().toString()

            saveAnalysisResult(
                sanitizedFileName,
                isMalicious = false,
                downloadUrl = downloadUrl
            )

            _fileState.value = _fileState.value.copy(
                completionMessage = "Upload Successfully!"
            )
            notification.showScanNotification(
                "Upload Complete",
                "File uploaded and saved successfully."
            )
        } catch (e: Exception) {
            setErrorMessage("Failed to save results to Firebase: ${e.localizedMessage}")
        }
    }

    /**
     * Saves the analysis results to Firestore.
     */
    private suspend fun saveAnalysisResult(
        originalFileName: String,
        isMalicious: Boolean,
        downloadUrl: String? = null
    ) {
        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(Date())

            val fileRecord = hashMapOf(
                "filename" to originalFileName,
                "timeUpload" to formattedDate,
                "userId" to auth.getCurrentUser()?.uid,
                "downloadUrl" to downloadUrl,
                "isMalicious" to isMalicious
            )

            firestore.collection("files")
                .document(auth.getCurrentUser()?.uid ?: "unknown_user")
                .collection("userFiles")
                .add(fileRecord)
                .await()

            getFile(auth.getCurrentUser()?.uid)

            _fileState.value = _fileState.value.copy(
                progressMessage = if (isMalicious) {
                    "Analysis results for malicious file recorded."
                } else {
                    "File and analysis results saved successfully!"
                }
            )
        } catch (e: Exception) {
            setErrorMessage(
                "Failed to save analysis results to Firestore: ${e.localizedMessage}"
            )
        }
    }

    /**
     * Downloads a file from Firebase Storage.
     */
    fun downloadFile(context: Context, fileRecord: FileRecord) {
        viewModelScope.launch {
            _fileState.update {
                it.copy(
                    isDownloading = true,
                    downloadProgress = 0,
                    errorMessage = null,
                    completionMessage = null
                )
            }
            notification.showDownloadNotification("Downloading", "Starting download...", 0)

            try {
                val downloadUrl =
                    fileRecord.downloadUrl ?: throw Exception("Download URL is missing.")
                val storageRef = storage.storage.getReferenceFromUrl(downloadUrl)
                val sanitizedFileName = sanitizeFileName(fileRecord.filename)
                val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                val localFile = File(downloadsDir, sanitizedFileName)

                storageRef.getFile(localFile)
                    .addOnProgressListener { taskSnapshot ->
                        val progress =
                            (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                        _fileState.update { it.copy(downloadProgress = progress) }
                        notification.showDownloadNotification(
                            "Downloading",
                            "${fileRecord.filename} - $progress%",
                            progress
                        )
                    }
                    .addOnSuccessListener {
                        viewModelScope.launch {
                            try {
                                val fileDao = AppDatabase.getDatabase(context).fileDao()
                                val fileData = FileData(
                                    filename = fileRecord.filename,
                                    filePath = localFile.absolutePath
                                )
                                withContext(Dispatchers.IO) {
                                    fileDao.insertFile(fileData)
                                }
                                _fileState.update {
                                    it.copy(
                                        completionMessage = "File downloaded successfully.",
                                        isDownloading = false
                                    )
                                }
                                notification.showDownloadNotification(
                                    "Download Complete",
                                    "File downloaded successfully"
                                )
                            } catch (e: Exception) {
                                _fileState.update {
                                    it.copy(
                                        errorMessage = "Failed to download file",
                                        isDownloading = false
                                    )
                                }
                                notification.showDownloadNotification(
                                    "Download Failed",
                                    "Failed to download file"
                                )
                            }
                        }
                    }
                    .addOnFailureListener {
                        _fileState.update {
                            it.copy(
                                errorMessage = "Failed to download file",
                                isDownloading = false
                            )
                        }
                        notification.showDownloadNotification(
                            "Download Failed",
                            "Failed to download file"
                        )
                    }
                    .await()
            } catch (e: Exception) {
                _fileState.update {
                    it.copy(
                        errorMessage = "Failed to download file",
                        isDownloading = false
                    )
                }
                notification.showDownloadNotification(
                    "Download Failed",
                    "Failed to download file"
                )
            }
        }
    }

    /**
     * Opens a file from local storage using external apps based on MIME type.
     * Checks if the file is already downloaded, and if not, downloads it first.
     */
    fun openFile(context: Context, fileRecord: FileRecord) {
        viewModelScope.launch {
            try {
                val fileDao = AppDatabase.getDatabase(context).fileDao()

                // Check if the file has already been downloaded (exists in the database)
                val fileData = fileDao.getFileByFilename(fileRecord.filename)

                if (fileData != null) {
                    // File exists in the database, open it directly
                    openFileWithIntent(context, File(fileData.filePath))
                } else {
                    // File doesn't exist, download it first
                    downloadAndOpenFile(context, fileRecord)
                }
            } catch (e: Exception) {
                setErrorMessage("Failed to open file: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Downloads the file and opens it after successful download.
     */
    private suspend fun downloadAndOpenFile(context: Context, fileRecord: FileRecord) {
        try {
            val downloadUrl = fileRecord.downloadUrl ?: throw Exception("Download URL is missing.")

            // Get reference from download URL
            val storageRef = storage.storage.getReferenceFromUrl(downloadUrl)
            val sanitizedFileName = sanitizeFileName(fileRecord.filename)
            val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val localFile = File(downloadsDir, sanitizedFileName)

            // Start downloading the file
            storageRef.getFile(localFile)
                .addOnProgressListener { taskSnapshot ->
                    val progress =
                        (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                    _fileState.value = _fileState.value.copy(downloadProgress = progress)
                }
                .addOnSuccessListener {
                    viewModelScope.launch {
                        val fileDao = AppDatabase.getDatabase(context).fileDao()
                        val fileData = FileData(
                            filename = fileRecord.filename,
                            filePath = localFile.absolutePath
                        )
                        withContext(Dispatchers.IO) {
                            fileDao.insertFile(fileData) // Store the file in the database
                        }

                        // Open the file after successful download
                        openFileWithIntent(context, localFile)

                        _fileState.value = _fileState.value.copy(
                            completionMessage = "File downloaded and opened successfully.",
                            isDownloading = false
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    _fileState.value = _fileState.value.copy(
                        errorMessage = "Failed to download file: ${exception.localizedMessage}",
                        isDownloading = false
                    )
                }
                .await()
        } catch (e: Exception) {
            _fileState.value = _fileState.value.copy(
                errorMessage = "Failed to download file: ${e.localizedMessage}",
                isDownloading = false
            )
        }
    }

    /**
     * Opens the file using an external app based on the MIME type.
     */
    private fun openFileWithIntent(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val mimeType = getMimeType(file)


            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(Intent.createChooser(intent, "Open with"))
        } catch (e: Exception) {
            setErrorMessage("Failed to open file: ${e.localizedMessage}")
        }
    }

    /**
     * Get MIME type from file extension.
     */
    private fun getMimeType(file: File): String? {
        val extension = file.extension
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    /**
     * Deletes a file from Firebase Storage and Firestore.
     */
    fun deleteFile(context: Context, fileRecord: FileRecord) {
        viewModelScope.launch {
            _fileState.update { it.copy(isLoading = true, errorMessage = null) }
            notification.showScanNotification("Deleting File", "Starting file deletion...", 0)

            try {
                val userId =
                    auth.getCurrentUser()?.uid ?: throw SecurityException("User not authenticated.")

                if (fileRecord.downloadUrl != null) {
                    try {
                        val storageRef = storage.storage.getReferenceFromUrl(fileRecord.downloadUrl)
                        storageRef.delete().await()
                    } catch (e: Exception) {
                        setErrorMessage("Got some error")
                    }
                }

                val documentRef = firestore.collection("files")
                    .document(userId)
                    .collection("userFiles")
                    .document(fileRecord.id)
                documentRef.delete().await()

                val fileDao = AppDatabase.getDatabase(context).fileDao()
                val localFileData = fileDao.getFileByFilename(fileRecord.filename)
                localFileData?.let {
                    val localFile = File(it.filePath)
                    if (localFile.exists()) {
                        localFile.delete()
                    }
                    fileDao.deleteFile(it)
                }

                _fileState.update { currentState ->
                    val updatedList = currentState.filesList.toMutableList().apply {
                        remove(fileRecord)
                    }
                    currentState.copy(
                        filesList = updatedList,
                        completionMessage = "File deleted successfully.",
                        isLoading = false
                    )
                }
                notification.showScanNotification("File Deleted", "File deleted successfully.")
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is SecurityException -> "User authentication required."
                    is IllegalStateException -> "File information is incomplete."
                    else -> "Failed to delete file: ${e.localizedMessage}"
                }
                _fileState.update {
                    it.copy(
                        errorMessage = errorMessage,
                        isLoading = false
                    )
                }
                notification.showScanNotification("Deletion Failed", errorMessage)
            }
        }
    }

    fun updateFileName(fileRecord: FileRecord, newFileName: String) {
        viewModelScope.launch {
            _fileState.update { it.copy(isLoading = true, errorMessage = null) }
            notification.showScanNotification("Updating File", "Starting file update...")

            try {
                val userId =
                    auth.getCurrentUser()?.uid ?: throw SecurityException("User not authenticated.")

                fileRecord.downloadUrl?.let { url ->
                    val oldStorageRef = storage.storage.getReferenceFromUrl(url)
                    val newStorageRef = storage.child("files/$userId/$newFileName")

                    oldStorageRef.getBytes(Long.MAX_VALUE).await().let { bytes ->
                        newStorageRef.putBytes(bytes).await()
                    }


                    val newDownloadUrl = newStorageRef.downloadUrl.await().toString()


                    oldStorageRef.delete().await()


                    val documentRef = firestore.collection("files")
                        .document(userId)
                        .collection("userFiles")
                        .document(fileRecord.id)

                    documentRef.update(
                        mapOf(
                            "filename" to newFileName,
                            "downloadUrl" to newDownloadUrl
                        )
                    ).await()

                    _fileState.update { currentState ->
                        val updatedList = currentState.filesList.map {
                            if (it.id == fileRecord.id) {
                                it.copy(filename = newFileName, downloadUrl = newDownloadUrl)
                            } else it
                        }
                        currentState.copy(
                            filesList = updatedList,
                            completionMessage = "File updated successfully.",
                            isLoading = false
                        )
                    }
                    notification.showScanNotification(
                        "File Updated",
                        "File updated successfully."
                    )
                } ?: throw IllegalStateException("Download URL is missing.")

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is SecurityException -> "User authentication required."
                    is IllegalStateException -> "File information is incomplete."
                    else -> "Failed to update file: ${e.message ?: "Unknown error"}"
                }
                _fileState.update {
                    it.copy(
                        errorMessage = errorMessage,
                        isLoading = false
                    )
                }
                notification.showScanNotification("Update Failed", errorMessage)
            }
        }
    }

    /**
     * Sanitizes the filename to prevent issues with Firebase Storage paths.
     */
    private fun sanitizeFileName(filename: String): String {
        return filename.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
    }
}