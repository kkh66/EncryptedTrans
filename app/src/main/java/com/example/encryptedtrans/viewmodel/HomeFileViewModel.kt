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


class HomeFileViewModel(private val auth: Auth) : ViewModel() {
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

    fun accessFile(
        context: Context,
        sharedFileWithDetails: SharedFileWithDetails,
        enteredPin: String? = null
    ) {
        viewModelScope.launch {
            val sharedFile = sharedFileWithDetails.sharedFile
            val currentTime = Date()

            if (sharedFile.expirationDate != null && currentTime.after(sharedFile.expirationDate)) {
                _homeState.update { it.copy(errorMessage = "This file has expired.") }
                return@launch
            }

            if (sharedFile.pin != null) {
                if (enteredPin == null) {
                    _pinRequired.value = sharedFileWithDetails
                    return@launch
                }
                if (sharedFile.pin != enteredPin) {
                    _homeState.update { it.copy(errorMessage = "Incorrect PIN.") }
                    return@launch
                }
            }

            downloadFile(context, sharedFileWithDetails.fileRecord!!)
        }
    }

    fun openFile(context: Context, fileRecord: FileRecord) {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val localFile = File(context.getExternalFilesDir(null), fileRecord.filename)
                if (!localFile.exists()) {
                    // File doesn't exist locally, download it first
                    downloadFile(context, fileRecord)
                }
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

    fun clearPinRequired() {
        _pinRequired.value = null
    }

    private fun downloadFile(
        context: Context,
        fileRecord: FileRecord
    ) {
        viewModelScope.launch {
            try {
                val storageRef =
                    storage.reference.child("files/${fileRecord.userId}/${fileRecord.filename}")
                val localFile = File(context.getExternalFilesDir(null), fileRecord.filename)

                storageRef.getFile(localFile).addOnSuccessListener {
                    _homeState.update { it.copy(successMessage = "File downloaded successfully") }
                }.addOnFailureListener { exception ->
                    _homeState.update { it.copy(errorMessage = "Failed to download file: ${exception.message}") }
                }.await()
            } catch (e: Exception) {
                _homeState.update { it.copy(errorMessage = "Error downloading file: ${e.message}") }
            }
        }
    }

    fun deleteFile(sharedFileWithDetails: SharedFileWithDetails) {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true, errorMessage = null) }
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
            } catch (e: Exception) {
                _homeState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to delete shared file: ${e.message}"
                    )
                }
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