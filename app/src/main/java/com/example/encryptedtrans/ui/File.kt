package com.example.encryptedtrans.ui

import android.provider.MediaStore.Downloads
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.encryptedtrans.viewmodel.FileViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.encryptedtrans.data.FileRecord
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformDirectory
import io.github.vinceglb.filekit.core.PlatformFile

@Composable
fun FileUi(
    viewModel: FileViewModel = viewModel(),
    platformSettings: FileKitPlatformSettings?
) {
    val context = LocalContext.current
    val fileState by viewModel.fileState.collectAsState()

    var files: Set<PlatformFile> by remember { mutableStateOf(emptySet()) }
    val directory: PlatformDirectory? by remember { mutableStateOf(null) }

    val infiniteTransition = rememberInfiniteTransition()
    val singleFilePicker = rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("pdf", "docx")),
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

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    Box(
        modifier = Modifier
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
                "Folder", modifier = Modifier.padding(3.dp), color = Color.White
            )
            HorizontalDivider(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                color = Color.Gray,
                thickness = 2.dp
            )

            when {
                fileState.isLoading -> {
                    // Show loading indicator if loading
                    CircularProgressIndicator(
                        strokeWidth = 4.dp,
                        strokeCap = StrokeCap.Round,
                        color = Color.Yellow,
                        modifier = Modifier.size(50.dp)
                    )
                }

                fileState.filesList.isEmpty() -> {
                    // Show empty state if no files are found
                    Text(
                        text = "No files available in this folder",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
                    // Show files if available
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
                                }
                            )
                        }
                    }
                }
            }
        }

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
                    .padding(10.dp)
            ) {
                Text("Upload File")
            }
        }
    }
}

@Composable
fun FileCard(
    fileRecord: FileRecord,
    onDownload: () -> Unit,
    onOpenFile: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

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
                    text = fileRecord.filename,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
                Text(
                    text = "Date: ${fileRecord.timeUpload}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Box {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Download File") },
                        onClick = {
                            onDownload()
                            showMenu = false // Close the menu
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Open File") },
                        onClick = {
                            onOpenFile()
                            showMenu = false // Close the menu
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete File") },
                        onClick = {
                            onDelete()
                            showMenu = false // Close the menu
                        }
                    )
                }
            }
        }
    }
}
