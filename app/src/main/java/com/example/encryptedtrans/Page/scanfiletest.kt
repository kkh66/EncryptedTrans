package com.example.encryptedtrans.Page

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.encryptedtrans.ui.theme.EncryptedTransTheme
import okhttp3.OkHttpClient
import okhttp3.Request

@Composable
fun Virultotaltest(){
    val client = OkHttpClient()

    val request = Request.Builder()
        .url("https://www.virustotal.com/api/v3/files")
        //.post(null)
        .addHeader("x-apikey", "142d978e5ac415810aa088e81debb999bdcf27cbdbeae8f04258a63a1b4f2e1e")
        .addHeader("accept", "application/json")
        .addHeader("content-type", "multipart/form-data")
        .build()

    val response = client.newCall(request).execute()

    if(response.isSuccessful){

    }
}



@Preview
@Composable
fun Testfun(){
    EncryptedTransTheme {
        Virultotaltest()
    }
}