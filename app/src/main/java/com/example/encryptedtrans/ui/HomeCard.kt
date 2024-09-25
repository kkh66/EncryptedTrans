package com.example.encryptedtrans.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.encryptedtrans.R
import com.example.encryptedtrans.data.SharedFileWithDetails

@Composable
fun HomeCard(
    sharedFileWithDetails: SharedFileWithDetails,
    onDownload: (SharedFileWithDetails, String?) -> Unit,
    onOpen: (SharedFileWithDetails, String?) -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinAction by remember { mutableStateOf<(String?) -> Unit>({}) }
    var enteredPin by remember { mutableStateOf("") }

    val maxFilenameLength = 15
    val fileRecord = sharedFileWithDetails.fileRecord

    val limitFilename = fileRecord?.filename?.let {
        if (it.length > maxFilenameLength) it.take(maxFilenameLength) + "..." else it
    } ?: "Unknown File"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
                    text = limitFilename,
                )
                Text(
                    text = "Shared by: ${sharedFileWithDetails.sharerUsername}"
                )
                fileRecord?.timeUpload?.let {
                    Text(
                        text = "Uploaded: $it",
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu"
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Download File") },
                            onClick = {
                                showPinDialog = true
                                pinAction = { pin ->
                                    onDownload(sharedFileWithDetails, pin) // Pass SharedFileWithDetails and PIN
                                }
                                showMenu = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Open File") },
                            onClick = {
                                showPinDialog = true
                                pinAction = { pin ->
                                    onOpen(sharedFileWithDetails, pin) // Pass SharedFileWithDetails and PIN
                                }
                                showMenu = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Remove Share") },
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

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                enteredPin = ""
            },
            title = { Text("Enter PIN") },
            text = {
                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = { enteredPin = it },
                    label = { Text("PIN") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                Button(onClick = {
                    pinAction(enteredPin)
                    enteredPin = ""
                    showPinDialog = false
                }) {
                    Text("Submit")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showPinDialog = false
                    enteredPin = ""
                }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }
}