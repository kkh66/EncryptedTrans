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
     */
    suspend fun registerUser(email: String, password: String, username: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                firebaseUser.updateProfile(userProfileChangeRequest {
                    displayName = username
                }).await()

                val userDoc = hashMapOf(
                    "username" to username,
                    "email" to email,
                    "isGoogleAccount" to false
                )
                db.collection("users").document(firebaseUser.uid).set(userDoc).await()

                AuthResult.Success(firebaseUser)
            } ?: AuthResult.Error("Registration failed: User is null")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "An unknown error occurred")
        }
    }

    /**
     * Login Account using email and password
     */
    suspend fun loginUser(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()

            val user = result.user ?: throw Exception("User is null")

            val userDocRef = db.collection("users").document(user.uid)
            userDocRef.update("email", user.email).await()

            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun checkEmailAndGoogleAccount(email: String): Pair<Boolean, Boolean> {
        return try {
            val querySnapshot = db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                Pair(false, false)
            } else {
                val document = querySnapshot.documents.first()
                val isGoogleAccount = document.getBoolean("isGoogleAccount") ?: false
                Pair(true, isGoogleAccount)
            }
        } catch (e: Exception) {
            Pair(false, false)
        }
    }

    suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val googleUser = result.user ?: throw Exception("Google Sign-In failed: User is null")

            val userDoc = db.collection("users").document(googleUser.uid).get().await()

            if (!userDoc.exists()) {
                val newUser = hashMapOf(
                    "username" to (googleUser.displayName ?: "Unknown User"),
                    "email" to (googleUser.email ?: ""),
                    "isGoogleAccount" to true
                )
                db.collection("users").document(googleUser.uid).set(newUser).await()
            } else {
                val existingEmail = userDoc.getString("email")
                if (existingEmail != googleUser.email) {
                    db.collection("users").document(googleUser.uid)
                        .update("email", googleUser.email, "isGoogleAccount", true).await()
                }
            }

            if (googleUser.displayName.isNullOrEmpty()) {
                googleUser.updateProfile(userProfileChangeRequest {
                    displayName = "Unknown User"
                }).await()
            }

            AuthResult.Success(googleUser)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "An unknown error occurred during Google Sign-In.")
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

            if (newEmail == user.email) {
                return AuthResult.Error("The new email is the same as the current email.")
            }

            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()

            val (emailExists, _) = checkEmailAndGoogleAccount(newEmail)
            if (emailExists) {
                return AuthResult.Error("This email is already in use by another account.")
            }

            user.verifyBeforeUpdateEmail(newEmail).await()

            val userDocRef = db.collection("users").document(user.uid)
            userDocRef.update("email", newEmail).await()

            AuthResult.Success(user)
        } catch (e: Exception) {
            return when (e) {
                is FirebaseAuthInvalidCredentialsException -> AuthResult.Error("Invalid email or password.")
                is FirebaseAuthUserCollisionException -> AuthResult.Error("This email is already in use by another account.")
                else -> AuthResult.Error("An unknown error occurred: ${e.message}")
            }
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