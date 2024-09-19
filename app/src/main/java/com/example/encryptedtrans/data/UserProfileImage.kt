package com.example.encryptedtrans.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile_image")
data class UserProfileImage(
    @PrimaryKey val userId: String,
    val profileImagePath: String
)