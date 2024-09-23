package com.example.encryptedtrans.ui

import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.encryptedtrans.viewmodel.FileViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.encryptedtrans.R
import com.example.encryptedtrans.data.FileRecord
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformDirectory
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileUi(
    viewModel: FileViewModel = viewModel(),
    platformSettings: FileKitPlatformSettings?,
    onFabClick: (() -> Unit) -> Unit
) {
    val context = LocalContext.current
    val fileState by viewModel.fileState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredFiles by viewModel.filteredFilesList.collectAsState()


    var isSearchActive by remember { mutableStateOf(false) }

    var files: Set<PlatformFile> by remember { mutableStateOf(emptySet()) }
    val directory: PlatformDirectory? by remember { mutableStateOf(null) }
    var selectedFileForSharing by remember { mutableStateOf<FileRecord?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }

    var useCustomPin by remember { mutableStateOf(false) }
    var customPin by remember { mutableStateOf("") }
    var useTimeLimitedSharing by remember { mutableStateOf(false) }
    var expirationDate by remember { mutableStateOf<Date?>(null) }
    var generateQRCode by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var shareUrl by remember { mutableStateOf("") }
    var showShareUrlDialog by remember { mutableStateOf(false) }
    var showUserSelectionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onFabClick {
            viewModel.refreshFiles()
        }
    }

    LaunchedEffect(fileState.isLoading) {
        if (fileState.isLoading) {
            snackbarHostState.showSnackbar(
                "Loading files...",
                duration = SnackbarDuration.Indefinite
            )
        } else {
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }


    val singleFilePicker = rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("pdf", "docx", "txt")),
        title = "Single file picker",
        mode = PickerMode.Single,
        initialDirectory = directory?.path,
        onResult = { file ->
            file?.let {
                files += it
                viewModel.scanFile(context, it.uri)
            }
        },
        platformSettings = platformSettings
    )

    Column(
        modifier = Modifier.padding(0.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp, start = 5.dp, end = 5.dp)
                .wrapContentHeight(),
            query = searchQuery,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            onSearch = { isSearchActive = false },
            active = isSearchActive,
            onActiveChange = { isSearchActive = it },
            windowInsets = WindowInsets(top = 0.dp),
            placeholder = {
                Text(
                    stringResource(id = R.string.search_field)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = stringResource(id = R.string.search_icon)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        viewModel.updateSearchQuery("")
                        isSearchActive = false
                    }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            }
        ) {
            if (filteredFiles.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.search_empty),
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Box {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredFiles) { fileRecord ->
                                FileCard(
                                    fileRecord = fileRecord,
                                    onDownload = {
                                        viewModel.downloadFile(context, fileRecord)
                                    },
                                    onOpenFile = {
                                        viewModel.openFile(context, fileRecord)
                                    },
                                    onDelete = {
                                        viewModel.deleteFile(context, fileRecord)
                                    },
                                    onShare = {
                                        selectedFileForSharing = fileRecord
                                        showShareDialog = true
                                    },
                                    onUpdate = { newFileName ->
                                        viewModel.updateFileName(fileRecord, newFileName)
                                    }
                                )
                            }
                        }
                    }


                }

            }
        }
        Box(
            modifier = Modifier
                .padding(5.dp)
                .border(2.dp, color = MaterialTheme.colorScheme.outline)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopStart),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.folder),
                    modifier = Modifier.padding(top = 5.dp),
                    fontSize = 30.sp
                )

                HorizontalDivider(
                    modifier = Modifier
                        .padding(start = 30.dp, end = 30.dp, top = 0.dp, bottom = 0.dp)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.outline, thickness = 2.dp
                )

                when {
                    fileState.isLoading -> {
                        // Display the progress indicator as a snackbar
                        Box(modifier = Modifier.fillMaxSize()) {
                            SnackbarHost(
                                hostState = snackbarHostState,
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                // Custom snackbar with loading progress
                                Card(
                                    modifier = Modifier.size(96.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.DarkGray,
                                        contentColor = Color.White
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                                ) {
                                    // Infinite animation for the progress indicator
                                    val progress by rememberInfiniteTransition().animateFloat(
                                        initialValue = 0f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000, easing = LinearEasing),
                                            repeatMode = RepeatMode.Restart
                                        )
                                    )

                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            progress = progress,
                                            strokeWidth = 4.dp,
                                            strokeCap = StrokeCap.Round,
                                            color = Color.Yellow,
                                            modifier = Modifier
                                                .size(50.dp)
                                                .rotate(360 * progress) // Rotate based on progress
                                        )
                                    }
                                }
                            }
                        }
                    }

                    fileState.filesList.isEmpty() -> {
                        Text(
                            text = "No files available in the folder",
                            color = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(fileState.filesList) { fileRecord ->
                                FileCard(
                                    fileRecord = fileRecord,
                                    onDownload = {
                                        viewModel.downloadFile(context, fileRecord)
                                    },
                                    onOpenFile = {
                                        viewModel.openFile(context, fileRecord)
                                    },
                                    onDelete = {
                                        viewModel.deleteFile(context, fileRecord)
                                    },
                                    onShare = {
                                        selectedFileForSharing = fileRecord
                                        showShareDialog = true
                                    },
                                    onUpdate = { newFileName ->
                                        viewModel.updateFileName(fileRecord, newFileName)
                                    }
                                )
                            }
                        }
                    }
                }
            }


            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter),
                verticalArrangement = Arrangement.Bottom
            ) {
                Button(
                    onClick = {
                        singleFilePicker.launch()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(16.dp)
                ) {
                    Text(stringResource(R.string.upload))
                }
            }
        }
    }

    if (showShareDialog) {
        val lengthFilename = selectedFileForSharing?.filename?.let {
            if (it.length > 5) it.take(5) + "..." else it
        }
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share File: $lengthFilename") },
            text = {
                Column {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Custom PIN Encrypt")
                        Switch(
                            checked = useCustomPin,
                            onCheckedChange = { useCustomPin = it }
                        )
                    }
                    if (useCustomPin) {
                        OutlinedTextField(
                            value = customPin,
                            onValueChange = { customPin = it },
                            label = { Text("Enter PIN") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Time-Limited Sharing")
                        Switch(
                            checked = useTimeLimitedSharing,
                            onCheckedChange = { useTimeLimitedSharing = it }
                        )
                    }
                    if (useTimeLimitedSharing) {
                        Button(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = expirationDate?.let {
                                    "Expiration Date: ${
                                        SimpleDateFormat(
                                            "dd/MM/yyyy",
                                            Locale.getDefault()
                                        ).format(it)
                                    }"
                                } ?: "Select Expiration Date"
                            )
                        }
                    }

                    if (showDatePicker) {
                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth)
                                expirationDate = calendar.time
                                showDatePicker = false
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).apply {
                            datePicker.minDate = calendar.timeInMillis
                            show()
                        }
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Generate QR Code")
                        Switch(
                            checked = generateQRCode,
                            onCheckedChange = { generateQRCode = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showShareDialog = false
                    showUserSelectionDialog = true
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Share Now")
                }
            }
        )
    }

    if (showUserSelectionDialog) {
        UserSelectionDialog(
            viewModel = viewModel,
            onDismiss = { showUserSelectionDialog = false },
            onConfirm = { selectedUserIds ->
                viewModel.shareFile(
                    selectedFileForSharing,
                    selectedUserIds,
                    if (useCustomPin) customPin else null,
                    expirationDate
                )
                showUserSelectionDialog = false
                // Optionally show a confirmation dialog or message
                shareUrl = selectedFileForSharing?.downloadUrl ?: ""
                showShareUrlDialog = true
            }
        )
    }

    if (generateQRCode && selectedFileForSharing?.downloadUrl != null) {
        showShareDialog = false
        val downloadUrl = selectedFileForSharing!!.downloadUrl!!
        val qrCodeBitmap = generateQRCodeBitmap(downloadUrl)
        AlertDialog(
            onDismissRequest = { generateQRCode = false },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.qr),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()
                ) {
                    qrCodeBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = stringResource(R.string.qr),
                            modifier = Modifier.size(200.dp)
                        )
                    } ?: Text(
                        stringResource(R.string.fail_generate_qr),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { generateQRCode = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        )
    }

    if (showShareUrlDialog) {
        AlertDialog(
            onDismissRequest = { showShareUrlDialog = false },
            title = { Text("File Shared Successfully") },
            text = {
                Column {
                    Text("Your file has been shared. Use the following URL to access the file:")
                    Spacer(modifier = Modifier.height(8.dp))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboardManager =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData = ClipData.newPlainText("Shared File URL", shareUrl)
                        clipboardManager.setPrimaryClip(clipData)
                        Toast.makeText(context, "URL copied to clipboard", Toast.LENGTH_SHORT)
                            .show()
                        showShareUrlDialog = false
                    }
                ) {
                    Text("Copy URL")
                }
            },
            dismissButton = {
                Button(onClick = { showShareUrlDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun generateQRCodeBitmap(content: String): Bitmap? {
    return try {
        val multiFormatWriter = MultiFormatWriter()
        val bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, 500, 500)
        val barcodeEncoder = BarcodeEncoder()
        barcodeEncoder.createBitmap(bitMatrix)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun UserSelectionDialog(
    viewModel: FileViewModel = viewModel(),
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val usersList by viewModel.usersList.collectAsState()
    var selectedUserIds by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.getUsers()
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Select Users to Share With") },
        text = {
            Column {
                LazyColumn {
                    items(usersList) { user ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedUserIds.contains(user.id),
                                onCheckedChange = { isChecked ->
                                    selectedUserIds = if (isChecked) {
                                        selectedUserIds + user.id
                                    } else {
                                        selectedUserIds - user.id
                                    }
                                }
                            )
                            Text(text = "${user.name} (${user.email})")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(selectedUserIds)
            }) {
                Text("Share")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}