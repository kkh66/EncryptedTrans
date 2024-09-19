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
import com.example.encryptedtrans.data.VirusTotalAnalysisResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

class FileViewModel(private val auth: Auth, private val context: Context) : ViewModel() {

    private val notification = notification(context)
    // UI State managed using MutableStateFlow
    private val _fileState = MutableStateFlow(FileState())
    val fileState: StateFlow<FileState> = _fileState

    // Initialize Firebase Firestore and Storage references
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    // Initialize Retrofit for VirusTotal API
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

    // Fetch existing files for the authenticated user
    init {
        fetchFiles(auth.getCurrentUser()?.uid)
    }

    /**
     * Fetches files associated with the current user from Firestore.
     * Updates the UI state accordingly.
     */
    private fun fetchFiles(userId: String?) {
        if (userId == null) {
            _fileState.value = _fileState.value.copy(
                errorMessage = "User not authenticated.",
                isLoading = false
            )
            return
        }
        viewModelScope.launch {
            _fileState.value = _fileState.value.copy(isLoading = true, errorMessage = null)

            try {
                val result = firestore.collection("files")
                    .document(userId)
                    .collection("userFiles")
                    .get()
                    .await()

                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
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
                    _fileState.value = _fileState.value.copy(
                        filesList = files,
                        isLoading = false,
                        errorMessage = "No files found."
                    )
                } else {
                    _fileState.value = _fileState.value.copy(
                        filesList = files,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _fileState.value = _fileState.value.copy(
                    errorMessage = "Failed to fetch files: ${e.localizedMessage}",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Scans a file using the VirusTotal API.
     * Uploads the file for scanning and handles the analysis result.
     */
    fun scanFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            _fileState.value = _fileState.value.copy(
                isLoading = true,
                progressMessage = "Starting virus scan...",
                errorMessage = null,
                completionMessage = null
            )

            try {
                withContext(Dispatchers.IO) {
                    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                    if (inputStream == null) {
                        throw Exception("Failed to open file stream")
                    }

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
                    checkAnalysisStatus(response.data.id, tempFile, originalFileName)
                }
            } catch (e: Exception) {
                _fileState.value = _fileState.value.copy(
                    errorMessage = "An exception occurred: ${e.localizedMessage}",
                    isLoading = false
                )
                e.printStackTrace()
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
                        handleAnalysisResult(file, analysisStats, originalFileName)
                        break
                    }

                    "queued", "in-progress" -> {
                        _fileState.value =
                            _fileState.value.copy(progressMessage = "Analysis in progress. Waiting...")
                        delay(3000) // Wait for 3 seconds before checking again
                    }

                    else -> {
                        _fileState.value = _fileState.value.copy(
                            errorMessage = "Unexpected analysis status: ${analysisResponse.data.attributes.status}"
                        )
                        break
                    }
                }
            } catch (e: Exception) {
                _fileState.value = _fileState.value.copy(
                    errorMessage = "Error checking analysis status: ${e.localizedMessage}"
                )
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
            // File is malicious; do not save to storage
            _fileState.value = _fileState.value.copy(
                progressMessage = "Warning: The file appears to be malicious. It will not be saved to storage."
            )
            saveAnalysisResult(result, originalFileName, isMalicious = true)
        } else {

            _fileState.value =
                _fileState.value.copy(progressMessage = "File appears to be safe. Saving to Firebase...")
            saveToFirebase(file, result, originalFileName)
        }

        // Cleanup temporary file
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
        result: VirusTotalAnalysisResult,
        originalFileName: String
    ) {
        try {
            val fileUri = Uri.fromFile(file)
            val sanitizedFileName = sanitizeFileName(originalFileName)
            val storageRef = storage.child("files/${auth.getCurrentUser()?.uid}/$sanitizedFileName")
            storageRef.putFile(fileUri).await()

            val downloadUrl = storageRef.downloadUrl.await().toString()

            // Save file record to Firestore with formatted date
            saveAnalysisResult(
                result,
                sanitizedFileName,
                isMalicious = false,
                downloadUrl = downloadUrl
            )

            _fileState.value = _fileState.value.copy(
                completionMessage = "Upload Successfully!"
            )
        } catch (e: Exception) {
            _fileState.value = _fileState.value.copy(
                errorMessage = "Failed to save results to Firebase: ${e.localizedMessage}"
            )
        }
    }

    /**
     * Saves the analysis results to Firestore.
     */
    private suspend fun saveAnalysisResult(
        result: VirusTotalAnalysisResult,
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
                "analysisResult" to result,
                "isMalicious" to isMalicious
            )

            firestore.collection("files")
                .document(auth.getCurrentUser()?.uid ?: "unknown_user")
                .collection("userFiles")
                .add(fileRecord)
                .await()

            _fileState.value = _fileState.value.copy(
                progressMessage = if (isMalicious) {
                    "Analysis results for malicious file recorded."
                } else {
                    "File and analysis results saved successfully!"
                }
            )
        } catch (e: Exception) {
            _fileState.value = _fileState.value.copy(
                errorMessage = "Failed to save analysis results to Firestore: ${e.localizedMessage}"
            )
        }
    }

    /**
     * Downloads a file from Firebase Storage.
     * Updates the UI state with download progress.
     */
    fun downloadFile(context: Context, fileRecord: FileRecord) {
        viewModelScope.launch {
            _fileState.update { it.copy(
                isDownloading = true,
                downloadProgress = 0,
                errorMessage = null,
                completionMessage = null
            ) }
            notification.showDownloadNotification("Downloading", "Starting download...", 0)

            try {
                val downloadUrl = fileRecord.downloadUrl ?: throw Exception("Download URL is missing.")
                val storageRef = storage.storage.getReferenceFromUrl(downloadUrl)
                val sanitizedFileName = sanitizeFileName(fileRecord.filename)
                val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                val localFile = File(downloadsDir, sanitizedFileName)

                storageRef.getFile(localFile)
                    .addOnProgressListener { taskSnapshot ->
                        val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                        _fileState.update { it.copy(downloadProgress = progress) }
                        notification.showDownloadNotification("Downloading", "${fileRecord.filename} - $progress%", progress)
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
                                _fileState.update { it.copy(
                                    completionMessage = "File downloaded successfully.",
                                    isDownloading = false
                                ) }
                                notification.showDownloadNotification("Download Complete", "${fileRecord.filename} downloaded successfully", 100)
                            } catch (e: Exception) {
                                _fileState.update { it.copy(
                                    errorMessage = "Failed to insert file data: ${e.message}",
                                    isDownloading = false
                                ) }
                                notification.showDownloadNotification("Download Failed", "Failed to save file data: ${e.message}")
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        _fileState.update { it.copy(
                            errorMessage = "Failed to download file: ${exception.localizedMessage}",
                            isDownloading = false
                        ) }
                        notification.showDownloadNotification("Download Failed", "Failed to download file: ${exception.localizedMessage}")
                    }
                    .await()
            } catch (e: Exception) {
                _fileState.update { it.copy(
                    errorMessage = "Failed to download file: ${e.localizedMessage}",
                    isDownloading = false
                ) }
                notification.showDownloadNotification("Download Failed", "Failed to download file: ${e.localizedMessage}")
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
                _fileState.value = _fileState.value.copy(
                    errorMessage = "Failed to open file: ${e.localizedMessage}"
                )
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

            // Get the MIME type of the file
            val mimeType = getMimeType(file)

            // Create an Intent to open the file
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            // Start the activity to open the file (user selects an external app)
            context.startActivity(Intent.createChooser(intent, "Open with"))
        } catch (e: Exception) {
            _fileState.value = _fileState.value.copy(
                errorMessage = "Failed to open file: ${e.localizedMessage}"
            )
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
                val userId = auth.getCurrentUser()?.uid ?: throw SecurityException("User not authenticated.")

                // Delete the file from Firebase Storage
                fileRecord.downloadUrl?.let { url ->
                    val storageRef = storage.storage.getReferenceFromUrl(url)
                    storageRef.delete().await()
                } ?: throw IllegalStateException("Download URL is missing.")

                // Delete Firestore document and local file
                firestore.runTransaction { transaction ->
                    // Delete the Firestore document
                    val documentRef = firestore.collection("files")
                        .document(userId)
                        .collection("userFiles")
                        .document(fileRecord.id)
                    transaction.delete(documentRef)

                    // Delete local file if it exists
                    // Note: Using runBlocking here is not ideal for performance, but it allows us to perform
                    // suspending operations within the transaction. Consider alternative approaches for better performance.
                    runBlocking {
                        val fileDao = AppDatabase.getDatabase(context).fileDao()
                        val localFileData = fileDao.getFileByFilename(fileRecord.filename)
                        localFileData?.let {
                            val localFile = File(it.filePath)
                            if (localFile.exists()) {
                                localFile.delete()
                            }
                            fileDao.deleteFile(it)
                        }
                    }
                }.await()

                // Remove the file from the local list
                val updatedList = _fileState.value.filesList.toMutableList().apply {
                    remove(fileRecord)
                }

                _fileState.update { it.copy(
                    filesList = updatedList,
                    completionMessage = "File deleted successfully.",
                    isLoading = false
                ) }
                notification.showScanNotification("File Deleted", "File deleted successfully.", 100)

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is SecurityException -> "User authentication required."
                    is IllegalStateException -> "File information is incomplete."
                    else -> "Failed to delete file: ${e.localizedMessage}"
                }
                _fileState.update { it.copy(
                    errorMessage = errorMessage,
                    isLoading = false
                ) }
                notification.showScanNotification("Deletion Failed", errorMessage)
            }
        }
    }

    /**
     * Sanitizes the filename to prevent issues with Firebase Storage paths.
     */
    private fun sanitizeFileName(filename: String): String {
        return filename.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
    }

    /**
     * Clears the completion message from the state.
     */
    fun clearCompletionMessage() {
        _fileState.value = _fileState.value.copy(completionMessage = null)
    }

    /**
     * Clears the error message from the state.
     */
    fun clearErrorMessage() {
        _fileState.value = _fileState.value.copy(errorMessage = null)
    }
}