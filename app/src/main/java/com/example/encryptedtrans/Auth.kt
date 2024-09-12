package com.example.encryptedtrans

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.encryptedtrans.data.FileRecord
import com.example.encryptedtrans.data.VirusTotalAnalysisResult
import com.google.firebase.firestore.snapshots
import java.util.Date

class Auth {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    sealed class AuthResult {
        data class Success(val user: FirebaseUser) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    suspend fun registerUser(email: String, password: String, username: String): AuthResult {
        return try {
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

    suspend fun loginUser(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success(result.user ?: throw Exception("User is null"))
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("User is null")
            val userDoc = db.collection("users").document(user.uid).get().await()
            if (!userDoc.exists()) {
                // If user doesn't exist, create a new document
                val newUser = hashMapOf(
                    "username" to (user.displayName ?: "Unknown User"),
                    "email" to (user.email ?: "")
                )
                db.collection("users").document(user.uid).set(newUser).await()
            }

            val username = userDoc.getString("username") ?: user.displayName ?: "Unknown User"
            user.updateProfile(userProfileChangeRequest {
                displayName = username
            }).await()

            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "An unknown error occurred")
        }
    }

    fun logoutUser() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun getUsername(): String {
        return getCurrentUser()?.displayName ?: "Unknown User"
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}