package com.example.encryptedtrans.Viewmodels

import androidx.lifecycle.ViewModel
import com.example.encryptedtrans.Auth

class UserProfileViewmodel(private val auth: Auth): ViewModel() {
    fun getUsername(): String {
        return auth.getCurrentUser()?.displayName ?: "Unknown User"
    }
    fun logout() {
        auth.logoutUser()
    }

}