package com.example.encryptedtrans.viewmodel

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.encryptedtrans.data.FileData

@Dao
interface FileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(fileData: FileData)

    @Query("SELECT * FROM FileData WHERE filename = :filename LIMIT 1")
    suspend fun getFileByFilename(filename: String): FileData?

    @Delete
    suspend fun deleteFile(fileData: FileData)
}