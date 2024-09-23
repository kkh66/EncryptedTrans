package com.example.encryptedtrans.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.encryptedtrans.R
import com.example.encryptedtrans.data.FileRecord

/**
 * File inside card
 * **/
@Composable
fun FileCard(
    fileRecord: FileRecord,
    onDownload: () -> Unit,
    onOpenFile: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onUpdate: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf(fileRecord.filename) }

    val maxFilenameLength = 15

    val limitFilename = if (fileRecord.filename.length > maxFilenameLength) {
        fileRecord.filename.take(maxFilenameLength) + "..."
    } else {
        fileRecord.filename
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = limitFilename
                )
                Text(
                    text = "Date: ${fileRecord.timeUpload}"
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onShare() }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Download File") },
                            onClick = {
                                onDownload()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Update File") },
                            onClick = {
                                showUpdateDialog = true
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Open File") },
                            onClick = {
                                onOpenFile()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete File") },
                            onClick = {
                                onDelete()
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }

    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("Update File Name") },
            text = {
                OutlinedTextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    label = { Text("New File Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdate(newFileName)
                        showUpdateDialog = false
                    }
                ) {
                    Text(stringResource(R.string.update))
                }
            },
            dismissButton = {
                Button(onClick = { showUpdateDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}