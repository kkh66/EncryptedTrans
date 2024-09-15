package com.example.encryptedtrans.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.encryptedtrans.Auth
import com.example.encryptedtrans.data.AnalysisResponse
import com.example.encryptedtrans.data.FileRecord
import com.example.encryptedtrans.data.VirusTotalAnalysisResult
import com.example.encryptedtrans.data.VirusTotalFileUploadResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date

class FileViewModel : ViewModel() {
    var scanResult by mutableStateOf<VirusTotalFileUploadResult?>(null)
    var analysisResult by mutableStateOf<VirusTotalAnalysisResult?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var progressMessage by mutableStateOf<String?>(null)
    var completionMessage by mutableStateOf<String?>(null)
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    var filesList by mutableStateOf<List<FileRecord>>(emptyList())

    init {
        fetchFilesForUser(Auth().getCurrentUser()?.uid)
    }

    private fun fetchFilesForUser(userId: String?) {
        if (userId == null) return

        viewModelScope.launch {
            try {
                firestore.collection("files")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener { result: QuerySnapshot ->
                        val dateFormat = SimpleDateFormat("MM/dd/yyyy")  // Define the format you want
                        val files = result.documents.map { doc ->
                            val date = doc.getDate("timeUpload") ?: Date()  // Get the `Date` object
                            val formattedDate = dateFormat.format(date)  // Format the date
                            FileRecord(
                                filename = doc.getString("filename") ?: "Unknown",
                                timeUpload = formattedDate,  // Use the formatted date string here
                                userId = doc.getString("userId"),
                                downloadUrl = doc.getString("downloadUrl"),
                                isMalicious = doc.getBoolean("isMalicious") ?: false
                            )
                        }
                        filesList = files
                    }
            } catch (e: Exception) {
                errorMessage = "Failed to fetch files: ${e.localizedMessage}"
            }
        }
    }


    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.virustotal.com/api/v3/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(
            OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("x-apikey", "a39401798e8096ce3d699c944a5a858b1ea305a2fba28e0e657a613ab93d28aa")
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            }
            .build())
        .build()

    private val virusTotalApi = retrofit.create(VirusTotalApi::class.java)

    fun scanFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            isLoading = true
            progressMessage = "Starting virus scan..."
            errorMessage = null

            try {
                withContext(Dispatchers.IO) {
                    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                    if (inputStream == null) {
                        errorMessage = "Failed to open file stream"
                        return@withContext
                    }

                    val originalFileName = getFileName(context, uri)
                    val tempFile = File(context.cacheDir, originalFileName)
                    val outputStream = FileOutputStream(tempFile)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()

                    val requestFile = tempFile.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", originalFileName, requestFile)

                    val response = virusTotalApi.uploadFile(body)
                    scanResult = response

                    progressMessage = "File uploaded. Checking analysis status..."
                    checkAnalysisStatus(response.data.id, tempFile, originalFileName)
                }
            } catch (e: Exception) {
                errorMessage = "An exception occurred: ${e.localizedMessage}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result ?: "unknown_file"
    }

    private suspend fun checkAnalysisStatus(analysisId: String, file: File, originalFileName: String) {
        while (true) {
            try {
                val analysisResponse = virusTotalApi.getAnalysisResult(analysisId)
                when (analysisResponse.data.attributes.status) {
                    "completed" -> {
                        analysisResult = analysisResponse.data.attributes.stats
                        progressMessage = "Analysis completed. Checking results..."
                        handleAnalysisResult(file, analysisResult!!, originalFileName)
                        break
                    }
                    "queued", "in-progress" -> {
                        progressMessage = "Analysis in progress. Waiting..."
                        delay(3000) // Wait for 10 seconds before checking again
                    }
                    else -> {
                        errorMessage = "Unexpected analysis status: ${analysisResponse.data.attributes.status}"
                        break
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error checking analysis status: ${e.localizedMessage}"
                break
            }
        }
    }

    private suspend fun handleAnalysisResult(file: File, result: VirusTotalAnalysisResult, originalFileName: String) {
        if (result.malicious > 0) {
            // File is malicious
            progressMessage = "Warning: The file appears to be malicious. It will not be saved to storage."
            saveAnalysisResultToFirestore(result, originalFileName, isMalicious = true)
        } else {
            // File is not malicious
            progressMessage = "File appears to be safe. Saving to Firebase..."
            saveResultsToFirebase(file, result, originalFileName)
        }
    }

    private suspend fun saveResultsToFirebase(file: File, result: VirusTotalAnalysisResult, originalFileName: String) {
        try {
            // Upload file to Firebase Storage
            val fileUri = Uri.fromFile(file)
            val storageRef = storage.child("files/$originalFileName")
            val uploadTask = storageRef.putFile(fileUri)

            uploadTask.await()

            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()

            // Save record to Firestore
            saveAnalysisResultToFirestore(result, originalFileName, isMalicious = false, downloadUrl)

            completionMessage = "Upload Successfully!"
        } catch (e: Exception) {
            errorMessage = "Failed to save results to Firebase: ${e.localizedMessage}"
        }
    }

    private suspend fun saveAnalysisResultToFirestore(
        result: VirusTotalAnalysisResult,
        originalFileName: String,
        isMalicious: Boolean,
        downloadUrl: String? = null
    ) {
        try {
            val fileRecord = hashMapOf(
                "filename" to originalFileName,
                "timeUpload" to Date(),
                "userId" to Auth().getCurrentUser()?.uid,
                "downloadUrl" to downloadUrl,
                "analysisResult" to result,
                "isMalicious" to isMalicious
            )

            firestore.collection("files")
                .add(fileRecord)
                .await()

            if (isMalicious) {
                progressMessage = "Analysis results for malicious file recorded."
            } else {
                progressMessage = "File and analysis results saved successfully!"
            }
        } catch (e: Exception) {
            errorMessage = "Failed to save analysis results to Firestore: ${e.localizedMessage}"
        }
    }

    fun clearCompletionMessage() {
        completionMessage = null
    }

    fun clearErrorMessage() {
        errorMessage = null
    }
}