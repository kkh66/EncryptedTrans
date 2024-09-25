package com.example.encryptedtrans.viewmodel

import android.content.Context
import android.content.Intent
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.encryptedtrans.Auth
import com.example.encryptedtrans.data.FileRecord
import com.example.encryptedtrans.data.HomeState
import com.example.encryptedtrans.data.SharedFile
import com.example.encryptedtrans.data.SharedFileWithDetails
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
import java.io.File
import java.util.Date


class HomeFileViewModel(private val auth: Auth, context: Context) : ViewModel() {
    private val notification = notification(context)
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val _homeState = MutableStateFlow(HomeState())
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _pinRequired = MutableStateFlow<SharedFileWithDetails?>(null)
    val pinRequired: StateFlow<SharedFileWithDetails?> = _pinRequired.asStateFlow()

    val filteredSharedFilesList: StateFlow<List<SharedFileWithDetails>> = combine(
        _searchQuery,
        _homeState.map { it.sharedFiles }
    ) { query, sharedFiles ->
        if (query.isEmpty()) {
            sharedFiles
        } else {
            sharedFiles.filter {
                it.fileRecord?.filename?.contains(query, ignoreCase = true) == true
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        getSharedFiles()
    }

    fun getSharedFiles() {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val userId =
                    auth.getCurrentUser()?.uid ?: throw SecurityException("User not authenticated.")
                val result = firestore.collection("shared_files")
                    .whereArrayContains("sharedWith", userId)
                    .get()
                    .await()

                val sharedFiles = result.documents.mapNotNull { doc ->
                    val fileId = doc.getString("fileId")
                    val sharedBy = doc.getString("sharedBy")
                    val pin = doc.getString("pin")
                    val expirationDate = doc.getTimestamp("expirationDate")?.toDate()
                    if (fileId != null && sharedBy != null) {
                        SharedFile(
                            id = doc.id,
                            fileId = fileId,
                            sharedBy = sharedBy,
                            pin = pin,
                            expirationDate = expirationDate
                        )
                    } else null
                }

                val sharedFilesWithDetails = sharedFiles.mapNotNull { sharedFile ->
                    val fileDoc = firestore.collection("files")
                        .document(sharedFile.sharedBy)
                        .collection("userFiles")
                        .document(sharedFile.fileId)
                        .get()
                        .await()
                    val fileRecord = fileDoc.toObject(FileRecord::class.java)?.copy(id = fileDoc.id)

                    val sharerDoc =
                        firestore.collection("users").document(sharedFile.sharedBy).get().await()
                    val sharerUsername = sharerDoc.getString("username") ?: "Unknown User"

                    if (fileRecord != null) {
                        SharedFileWithDetails(
                            sharedFile = sharedFile,
                            fileRecord = fileRecord,
                            sharerUsername = sharerUsername
                        )
                    } else null
                }

                _homeState.update {
                    it.copy(
                        sharedFiles = sharedFilesWithDetails,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _homeState.update {
                    it.copy(
                        errorMessage = "Failed to fetch shared files: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun downloadFile(
        context: Context,
        sharedFileWithDetails: SharedFileWithDetails? = null,
        fileRecord: FileRecord? = null,
        enteredPin: String? = null
    ) {
        viewModelScope.launch {
            val targetFileRecord = fileRecord ?: sharedFileWithDetails?.fileRecord ?: return@launch

            //check the pin and expiration
            sharedFileWithDetails?.let { sharedFileWithDetails ->
                val sharedFile = sharedFileWithDetails.sharedFile
                val currentTime = Date()

                // Check expiration date
                if (sharedFile.expirationDate != null && currentTime.after(sharedFile.expirationDate)) {
                    _homeState.update { it.copy(errorMessage = "This file has expired.") }
                    return@launch
                }

                // Check if a PIN is required
                if (sharedFile.pin != null) {
                    if (enteredPin == null) {
                        _pinRequired.update { sharedFileWithDetails }
                        return@launch
                    }
                    if (sharedFile.pin != enteredPin) {
                        _homeState.update { it.copy(errorMessage = "Incorrect PIN.") }
                        return@launch
                    }
                }
            }

            // Proceed with downloading the file
            notification.showDownloadNotification("Downloading", "Starting download...", 0)

            try {
                val storageRef =
                    storage.reference.child("files/${targetFileRecord.userId}/${targetFileRecord.filename}")
                val localFile = File(context.getExternalFilesDir(null), targetFileRecord.filename)

                storageRef.getFile(localFile)
                    .addOnProgressListener { taskSnapshot ->
                        val progress =
                            (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                        notification.showDownloadNotification(
                            "Downloading",
                            "${targetFileRecord.filename} - $progress%",
                            progress
                        )
                    }
                    .addOnSuccessListener {
                        _homeState.update { it.copy(successMessage = "File downloaded successfully") }
                        notification.showDownloadNotification(
                            "Download Complete",
                            "File downloaded successfully"
                        )
                    }
                    .addOnFailureListener { exception ->
                        _homeState.update { it.copy(errorMessage = "Failed to download file: ${exception.message}") }
                        notification.showDownloadNotification(
                            "Download Failed",
                            "Failed to download file"
                        )
                    }
                    .await()
            } catch (e: Exception) {
                _homeState.update { it.copy(errorMessage = "Error downloading file") }
                notification.showDownloadNotification(
                    "Download Failed",
                    "Error downloading file"
                )

            }
        }
    }

    fun openFile(
        context: Context,
        sharedFileWithDetails: SharedFileWithDetails,
        enteredPin: String? = null
    ) {
        viewModelScope.launch {
            val sharedFile = sharedFileWithDetails.sharedFile
            val currentTime = Date()

            // Check expiration date
            if (sharedFile.expirationDate != null && currentTime.after(sharedFile.expirationDate)) {
                _homeState.update { it.copy(errorMessage = "This file has expired.") }
                return@launch
            }

            // Check if a PIN is required
            if (sharedFile.pin != null) {
                if (enteredPin == null) {
                    _pinRequired.update { sharedFileWithDetails }
                    return@launch
                }

                if (sharedFile.pin != enteredPin) {
                    _homeState.update { it.copy(errorMessage = "Incorrect PIN.") }
                    return@launch
                }
            }

            downloadAndOpenFile(context, sharedFileWithDetails.fileRecord!!)
        }
    }

    private fun downloadAndOpenFile(context: Context, fileRecord: FileRecord) {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val localFile = File(context.getExternalFilesDir(null), fileRecord.filename)

                // If file doesn't exist locally, download it first
                if (!localFile.exists()) {
                    downloadFile(context, fileRecord = fileRecord)
                }

                // Open the file once it's downloaded
                openFileWithIntent(context, localFile)
                _homeState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _homeState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error opening file: ${e.message}"
                    )
                }
            }
        }
    }

    private fun openFileWithIntent(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, getMimeType(file))
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(Intent.createChooser(intent, "Open with"))
    }

    private fun getMimeType(file: File): String? {
        val extension = file.extension
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    fun deleteFile(sharedFileWithDetails: SharedFileWithDetails) {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true, errorMessage = null) }
            notification.showScanNotification("Deleting File", "Deleting the File")

            try {
                firestore.collection("shared_files")
                    .document(sharedFileWithDetails.sharedFile.id)
                    .delete()
                    .await()

                _homeState.update { currentState ->
                    currentState.copy(
                        sharedFiles = currentState.sharedFiles.filter { it != sharedFileWithDetails },
                        isLoading = false,
                        successMessage = "Shared file removed successfully."
                    )
                }
                notification.showScanNotification("File Deleted", "File Deleted successfully.")
            } catch (e: Exception) {
                _homeState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to delete shared file: ${e.message}"
                    )
                }
                notification.showScanNotification(
                    "Deletion Failed",
                    "Failed to delete shared file: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _homeState.update {
            it.copy(errorMessage = null, successMessage = null)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}