package com.example.encryptedtrans.Page


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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.encryptedtrans.data.FileRecord
import com.example.encryptedtrans.viewmodel.FileViewModel
import com.example.modified_snackbar.presentation.rememberComposeModifiedSnackbarState
import com.facebook.bolts.Task.Companion.delay
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformDirectory
import io.github.vinceglb.filekit.core.PlatformFile


@Composable
fun Folder(
    viewmodel: FileViewModel = viewModel(),
    platformSettings: FileKitPlatformSettings?
) {

    val context = LocalContext.current
    var files: Set<PlatformFile> by remember { mutableStateOf(emptySet()) }
    val directory: PlatformDirectory? by remember { mutableStateOf(null) }

    val infiniteTransition = rememberInfiniteTransition("")
    val singleFilePicker = rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("pdf", "docx")),
        title = "Single file picker",
        mode = PickerMode.Single,
        initialDirectory = directory?.path,
        onResult = { file ->
            file?.let {
                files += it
                viewmodel.scanFile(context, it.uri)
            }
        },
        platformSettings = platformSettings
    )
    // animation for CircularProgressIndicator
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
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewmodel.filesList) { fileRecord ->
                    FileCard(fileRecord)
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


        Box(modifier = Modifier
            .fillMaxSize()) {
            when {
                viewmodel.isLoading -> {
                    Card(
                        modifier = Modifier
                            .size(250.dp)
                            .align(Alignment.Center),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.DarkGray,
                            contentColor = Color.White
                        ),
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    strokeWidth = 4.dp,
                                    strokeCap = StrokeCap.Round,
                                    color = Color.Yellow,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .rotate(rotationAngle)
                                )
                                Text(
                                    text = viewmodel.progressMessage ?: "",
                                    color = Color.White,
                                )
                            }

                        }
                    }
                }

                viewmodel.completionMessage?.isNotEmpty() == true -> {
                    Card(
                        modifier = Modifier
                            .size(150.dp)
                            .align(Alignment.Center),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.DarkGray,
                            contentColor = Color.White
                        ),
                    ) {
                        Box(
                            Modifier.fillMaxSize(),
                            Alignment.Center
                        ) {
                            Column(
                                Modifier.padding(2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Success",
                                    tint = Color.Green,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = viewmodel.completionMessage!!,
                                    color = Color.White,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                        // Auto-clear the completion message after 3 seconds
                        LaunchedEffect(viewmodel.completionMessage) {
                            delay(5000)
                            viewmodel.clearCompletionMessage()
                        }
                    }
                }

                viewmodel.errorMessage?.isNotEmpty() == true -> {
                    Card(
                        modifier = Modifier
                            .size(150.dp)
                            .align(Alignment.Center),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.DarkGray,
                            contentColor = Color.White
                        ),
                    ) {
                        Box(
                            Modifier.fillMaxSize()
                        ) {
                            Column(
                                Modifier.padding(2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Error",
                                    tint = Color.Red,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = viewmodel.errorMessage!!,
                                    color = Color.White,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                        // Auto-clear the error message after 3 seconds
                        LaunchedEffect(viewmodel.errorMessage) {
                            delay(5000)
                            viewmodel.clearErrorMessage()
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun FileCard(fileRecord: FileRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = fileRecord.filename,
                    color = Color.Black,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Date: ${fileRecord.timeUpload}",
                    color = Color.Gray,
                )
            }

            IconButton(onClick = {}

            ) {
                Icon(
                    imageVector = Icons.Default.Share, contentDescription = "Share File"
                )
            }
        }
    }
}
