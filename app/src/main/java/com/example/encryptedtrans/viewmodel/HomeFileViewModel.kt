package com.example.encryptedtrans.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.encryptedtrans.Auth
import com.example.encryptedtrans.data.SharedFileWithDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

class HomeFileViewModel(private val auth: Auth) : ViewModel() {


    private val _sharedFiles = MutableStateFlow<List<SharedFileWithDetails>>(emptyList())
    val sharedFiles: StateFlow<List<SharedFileWithDetails>> = _sharedFiles.asStateFlow()

    fun accessSharedFile(
        context: Context,
        sharedFileWithDetails: SharedFileWithDetails,
        enteredPin: String? = null
    ) {
        val sharedFile = sharedFileWithDetails.sharedFile
        val currentTime = Date()

        if (sharedFile.expirationDate != null && currentTime.after(sharedFile.expirationDate)) {
            //setErrorMessage("This file has expired.")
            return
        }

        if (sharedFile.pin != null) {
            if (enteredPin == sharedFile.pin) {
                // Proceed to download or open the file
                //downloadFile(context, sharedFileWithDetails.fileRecord!!)
            } else {
                //setErrorMessage("Incorrect PIN.")
            }
        } else {
            // No PIN protection, proceed to download or open the file
            //downloadFile(context, sharedFileWithDetails.fileRecord!!)
        }
    }

    fun fetchSharedFiles() {
        val userId = auth.getCurrentUser()?.uid ?: return
        /*viewModelScope.launch {
            try {
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

                val files = sharedFiles.mapNotNull { sharedFile ->
                    val fileDoc = firestore.collection("files")
                        .document(sharedFile.sharedBy)
                        .collection("userFiles")
                        .document(sharedFile.fileId)
                        .get()
                        .await()
                    val fileRecord = fileDoc.toObject(FileRecord::class.java)?.copy(id = fileDoc.id)
                    if (fileRecord != null) {
                        SharedFileWithDetails(
                            sharedFile = sharedFile,
                            fileRecord = fileRecord
                        )
                    } else null
                }
                _sharedFiles.value = files
            } catch (e: Exception) {
                // Handle the error, e.g., update an error state
                _fileState.value = _fileState.value.copy(
                    errorMessage = "Failed to fetch shared files: ${e.localizedMessage}"
                )
            }
        }
    }*/
    }
}