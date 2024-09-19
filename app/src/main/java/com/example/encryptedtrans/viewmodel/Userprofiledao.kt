package com.example.encryptedtrans.viewmodel

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.encryptedtrans.data.UserProfileImage

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile_image WHERE userId = :userId LIMIT 1")
    suspend fun getUserProfile(userId: String): UserProfileImage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfilePicture(userProfilePicture: UserProfileImage)
}
