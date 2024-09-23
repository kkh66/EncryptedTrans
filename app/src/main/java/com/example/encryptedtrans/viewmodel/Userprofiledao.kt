package com.example.encryptedtrans.viewmodel

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.encryptedtrans.data.UserProfileImage

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile_image WHERE userId = :userId LIMIT 1")
    suspend fun getUserProfile(userId: String): UserProfileImage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfilePicture(userProfileImage: UserProfileImage)

    @Update
    suspend fun updateProfilePicture(userProfileImage: UserProfileImage)

    @Query("DELETE FROM user_profile_image WHERE userId = :userId")
    suspend fun deleteProfilePicture(userId: String)

    @Query("SELECT profileImagePath FROM user_profile_image WHERE userId = :userId")
    suspend fun getProfileImagePath(userId: String): String?
}
