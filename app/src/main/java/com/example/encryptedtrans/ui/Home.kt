package com.example.encryptedtrans.ui

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.encryptedtrans.data.SharedFileWithDetails
import com.example.encryptedtrans.viewmodel.FileViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.encryptedtrans.data.FileRecord
import com.example.encryptedtrans.viewmodel.HomeFileViewModel
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeUi(viewModel: HomeFileViewModel, onFabClick: (() -> Unit) -> Unit) {
    val context = LocalContext.current
    val sharedFiles by viewModel.sharedFiles.collectAsState()
    //val fileState by viewModel.fileState.collectAsState()

    // State variables for PIN dialog and error messages
    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var currentSharedFile: SharedFileWithDetails? by remember { mutableStateOf(null) }

    // SnackbarHostState for Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Fetch shared files when the composable enters the composition
    LaunchedEffect(Unit) {
        viewModel.fetchSharedFiles()
    }

    LaunchedEffect(Unit) {
        onFabClick {

        }
    }

    // Display error messages using Snackbar
    /*if (fileState.errorMessage != null) {
        LaunchedEffect(fileState.errorMessage) {
            snackbarHostState.showSnackbar(fileState.errorMessage ?: "")
            viewModel.clearErrorMessage()
        }
    }
    Column {


        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            content = { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(5.dp)
                        .border(2.dp, Color.White)
                        .fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.TopStart),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Shared Files", modifier = Modifier.padding(3.dp), color = Color.White
                        )
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(start = 10.dp, end = 10.dp)
                                .fillMaxWidth(),
                            color = Color.Gray,
                            thickness = 2.dp
                        )

                        if (sharedFiles.isEmpty()) {
                            // Show a message if there are no shared files
                            Text(
                                text = "No shared files available",
                                color = Color.White,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            // Display the list of shared files
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(sharedFiles) { sharedFileWithDetails ->
                                    val fileRecord = sharedFileWithDetails.fileRecord
                                    if (fileRecord != null) {
                                        FileCardtest(
                                            fileRecord = fileRecord,
                                            onAccess = {
                                                handleSharedFileAccess(
                                                    context,
                                                    sharedFileWithDetails,
                                                    //viewModel,
                                                    onShowPinDialog = {
                                                        currentSharedFile = sharedFileWithDetails
                                                        showPinDialog = true
                                                    },
                                                    onExpired = {
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar("This file has expired.")
                                                        }
                                                    }
                                                )
                                            },
                                            onDelete = {},
                                            onShare = {},
                                            onUpdate = {},
                                            showShareButton = false
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // PIN entry dialog
                    if (showPinDialog) {
                        AlertDialog(
                            onDismissRequest = { showPinDialog = false },
                            title = { Text("Enter PIN") },
                            text = {
                                OutlinedTextField(
                                    value = enteredPin,
                                    onValueChange = { enteredPin = it },
                                    label = { Text("PIN") },
                                    singleLine = true
                                )
                            },
                            confirmButton = {
                                Button(onClick = {
                                    if (enteredPin == currentSharedFile?.sharedFile?.pin) {
                                        // PIN is correct; proceed to download
                                        viewModel.downloadFile(
                                            context,
                                            currentSharedFile?.fileRecord!!
                                        )
                                        showPinDialog = false
                                        enteredPin = ""
                                    } else {
                                        // Incorrect PIN; show error message
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Incorrect PIN.")
                                        }
                                    }
                                }) {
                                    Text("Confirm")
                                }
                            },
                            dismissButton = {
                                Button(onClick = {
                                    showPinDialog = false
                                    enteredPin = ""
                                }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        )
    }*/
}

// Helper function to handle access control
fun handleSharedFileAccess(
    context: Context,
    sharedFileWithDetails: SharedFileWithDetails,
    viewModel: FileViewModel,
    onShowPinDialog: () -> Unit,
    onExpired: () -> Unit
) {
    val sharedFile = sharedFileWithDetails.sharedFile
    val currentTime = Date()

    if (sharedFile.expirationDate != null && currentTime.after(sharedFile.expirationDate)) {
        // File has expired
        onExpired()
    } else if (sharedFile.pin != null) {
        // File is protected with a PIN
        onShowPinDialog()
    } else {
        // No PIN or expiration; proceed to download
        viewModel.downloadFile(context, sharedFileWithDetails.fileRecord!!)
    }
}


@Composable
fun FileCardtest(
    fileRecord: FileRecord,
    onAccess: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onUpdate: (String) -> Unit,
    showShareButton: Boolean = true
) {
    var showMenu by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf(fileRecord.filename) }

    val maxFilenameLength = 15

    val limitedFilename = if (fileRecord.filename.length > maxFilenameLength) {
        fileRecord.filename.take(maxFilenameLength) + "..."
    } else {
        fileRecord.filename
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    text = limitedFilename,
                    color = Color.Black
                )
                Text(
                    text = "Date: ${fileRecord.timeUpload}",
                    color = Color.Gray
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Access button (Download or Open)
                IconButton(onClick = { onAccess() }) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Access File",
                        tint = Color(0xFF000080)
                    )
                }

                if (showShareButton) {
                    IconButton(onClick = { onShare() }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color(0xFF000080)
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color(0xFF000080)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Update File") },
                            onClick = {
                                showUpdateDialog = true
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
                    label = { Text("New File Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdate(newFileName)
                        showUpdateDialog = false
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                Button(onClick = { showUpdateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
