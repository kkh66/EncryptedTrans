package com.example.encryptedtrans.viewmodel

import com.example.encryptedtrans.data.AnalysisResponse
import com.example.encryptedtrans.data.VirusTotalFileUploadResult
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface VirusTotalApi {
    @Multipart
    @POST("files")
    suspend fun uploadFile(@Part file: MultipartBody.Part): VirusTotalFileUploadResult

    @GET("analyses/{id}")
    suspend fun getAnalysisResult(@Path("id") id: String): AnalysisResponse
}