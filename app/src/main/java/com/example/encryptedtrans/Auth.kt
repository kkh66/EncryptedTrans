package com.example.encryptedtrans

import android.content.Intent
import androidx.core.content.ContextCompat.getString
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class Auth {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    sealed class AuthResult {
        data class Success(val user: FirebaseUser) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    suspend fun registerUser(email: String, password: String, username: String): AuthResult {
        return try {
            //Auth create
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                firebaseUser.updateProfile(userProfileChangeRequest {
                    displayName = username
                }).await()
                AuthResult.Success(firebaseUser)
            } ?: AuthResult.Error("Registration failed: User is null")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun LoginUser(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email,password).await()
            AuthResult.Success(result.user ?: throw Exception("User is null"))
        }catch(e: Exception) {
            AuthResult.Error(e.message ?: "An unknown error occurred")
        }
    }



    fun logoutUser() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}