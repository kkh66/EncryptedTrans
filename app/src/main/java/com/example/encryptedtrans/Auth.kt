package com.example.encryptedtrans

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class Auth {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    sealed class AuthResult {
        data class Success(val user: FirebaseUser) : AuthResult()
        data class PasswordResetSuccess(val email: String) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    /**
     * Register Account at the Authentication and then also at the Cloud Firestore
     **/
    suspend fun registerUser(email: String, password: String, username: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                firebaseUser.updateProfile(userProfileChangeRequest {
                    displayName = username
                }).await()

                val userDoc = hashMapOf(
                    "username" to username,
                    "email" to email
                )
                db.collection("users").document(firebaseUser.uid).set(userDoc).await()

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

            // Check if the user is already signed in
            val user = auth.currentUser
            if (user != null && user.email != null) {
                // If the user is already logged in link Google to the account
                user.linkWithCredential(credential).await()
                AuthResult.Success(user)
            } else {
                // Otherwise, sign in using Google
                val result = auth.signInWithCredential(credential).await()
                val googleUser = result.user ?: throw Exception("User is null")

                // Check if the user document exists in Firestore
                val userDoc = db.collection("users").document(googleUser.uid).get().await()
                if (!userDoc.exists()) {
                    val newUser = hashMapOf(
                        "username" to (googleUser.displayName ?: "Unknown User"),
                        "email" to (googleUser.email ?: "")
                    )
                    db.collection("users").document(googleUser.uid).set(newUser).await()
                }

                val username =
                    userDoc.getString("username") ?: googleUser.displayName ?: "Unknown User"
                googleUser.updateProfile(userProfileChangeRequest {
                    displayName = username
                }).await()

                AuthResult.Success(googleUser)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            auth.sendPasswordResetEmail(email).await()
            AuthResult.PasswordResetSuccess(email)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun updateUserProfile(username: String): AuthResult {
        return try {
            val user = auth.currentUser ?: throw Exception("User is not logged in")

            val profileUpdates = userProfileChangeRequest {
                displayName = username
            }
            user.updateProfile(profileUpdates).await()

            val userDocRef = db.collection("users").document(user.uid)
            userDocRef.update("username", username).await()

            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "An unknown error occurred while updating profile")
        }
    }

    suspend fun updateUserEmail(newEmail: String, email: String, password: String): AuthResult {
        return try {
            val user = auth.currentUser ?: throw Exception("User is not logged in")


            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()

            user.verifyBeforeUpdateEmail(newEmail)


            AuthResult.Success(user)
        } catch (e: Exception) {
            if (e is FirebaseAuthInvalidCredentialsException) {
                return AuthResult.Error("Please check your email and password")
            } else if (e is FirebaseAuthUserCollisionException) {
                return AuthResult.Error("This email is already in use by another account")
            }
            return AuthResult.Error("An unknown error occurred: ${e.message}")
        }
    }

    /**
    Changing Password Function at User Profile
     **/
    suspend fun changePassword(currentPassword: String, newPassword: String): AuthResult {
        return try {
            val user = auth.currentUser ?: throw Exception("User is not logged in")
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
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

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}