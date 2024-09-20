package com.example.encryptedtrans.ui

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import com.example.encryptedtrans.R
import com.example.encryptedtrans.data.FileRecord
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformDirectory
import io.github.vinceglb.filekit.core.PlatformFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileUi(
    viewModel: FileViewModel = viewModel(),
    platformSettings: FileKitPlatformSettings?
) {
    val context = LocalContext.current
    val fileState by viewModel.fileState.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredFiles by viewModel.filteredFilesList.collectAsState()

    var isSearchActive by remember { mutableStateOf(false) }

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
                    stringResource(id = R.string.search_field),
                    color = Color.Black
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = stringResource(id = R.string.search_icon),
                    tint = Color.Black
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
            },
            colors = SearchBarDefaults.colors(
                containerColor = Color.White,
                inputFieldColors = TextFieldDefaults.colors(
                    Color.Black
                )
            ),
        ) {
            if (filteredFiles.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.search_empty),
                    color = Color.Black,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Box {
                    Column {
                        Text(
                            text = stringResource(id = R.string.search_result),
                            color = Color.Black,
                            modifier = Modifier
                                .padding(5.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(start = 10.dp, end = 10.dp)
                                .fillMaxWidth(),
                            color = Color.Gray,
                            thickness = 2.dp
                        )
                    }
                }
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
                            onShare = {}
                        )
                    }
                }

            }
        }


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
                    stringResource(R.string.folder),
                    modifier = Modifier.padding(3.dp),
                    color = Color.White
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
                                    onShare = {}
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
}

@Composable
fun FileCard(
    fileRecord: FileRecord,
    onDownload: () -> Unit,
    onOpenFile: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val maxFilenameLength = 15


    val limitFilename = if (fileRecord.filename.length > maxFilenameLength) {
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
                    text = limitFilename,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
                Text(
                    text = "Date: ${fileRecord.timeUpload}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(onClick = { onShare() }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color(0xFF000080) // Dark blue icon
                    )
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
                            text = { Text("Download File") },
                            onClick = {
                                onDownload()
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
}
