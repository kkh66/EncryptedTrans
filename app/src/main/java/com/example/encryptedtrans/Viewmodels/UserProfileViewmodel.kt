package com.example.encryptedtrans.Viewmodels

import androidx.lifecycle.ViewModel
import com.example.encryptedtrans.Auth

class UserProfileViewmodel(private val auth: Auth): ViewModel() {
    fun logout() {
        auth.logoutUser()
    }

}