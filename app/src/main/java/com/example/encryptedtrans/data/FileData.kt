package com.example.encryptedtrans.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FileData")
data class FileData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val filename: String,
    val filePath: String,
)
