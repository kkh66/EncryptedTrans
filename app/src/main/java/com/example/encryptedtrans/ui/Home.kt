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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.encryptedtrans.R
import com.example.encryptedtrans.viewmodel.HomeFileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeUi(viewModel: HomeFileViewModel, onFabClick: (() -> Unit) -> Unit) {
    val context = LocalContext.current
    val homeState by viewModel.homeState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredSharedFiles by viewModel.filteredSharedFilesList.collectAsState()
    val pinRequired by viewModel.pinRequired.collectAsState()

    var isSearchActive by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.getSharedFiles()
        onFabClick {
            viewModel.getSharedFiles()
        }
    }

    LaunchedEffect(pinRequired) {
        if (pinRequired != null) {
            showPinDialog = true
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
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
                windowInsets = WindowInsets(top = 0.dp),
                onActiveChange = { isSearchActive = it },
                placeholder = {
                    Text(stringResource(id = R.string.search_field))
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
                if (filteredSharedFiles.isEmpty()) {
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
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredSharedFiles) { sharedFileWithDetails ->
                                    HomeCard(
                                        sharedFileWithDetails = sharedFileWithDetails,
                                        onAccess = {
                                            viewModel.accessFile(
                                                context,
                                                sharedFileWithDetails
                                            )
                                        },
                                        onOpen = {
                                            sharedFileWithDetails.fileRecord?.let { fileRecord ->
                                                viewModel.openFile(context, fileRecord)
                                            }
                                        },
                                        onDelete = { viewModel.deleteFile(sharedFileWithDetails) }
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
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.home),
                        modifier = Modifier.padding(top = 5.dp),
                        fontSize = 30.sp
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(horizontal = 30.dp)
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.outline,
                        thickness = 2.dp
                    )
                    if (!isSearchActive) {
                        when {
                            homeState.isLoading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Card(
                                        modifier = Modifier.size(96.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.DarkGray,
                                            contentColor = Color.White
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                                    ) {
                                        val progress by rememberInfiniteTransition().animateFloat(
                                            initialValue = 0f,
                                            targetValue = 1f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(1000, easing = LinearEasing),
                                                repeatMode = RepeatMode.Restart
                                            ), label = ""
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
                                                    .rotate(360 * progress)
                                            )
                                        }
                                    }
                                }
                            }

                            homeState.sharedFiles.isEmpty() -> {
                                Text(
                                    "No shared files found",
                                    modifier = Modifier.padding(16.dp)
                                )
                            }

                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(homeState.sharedFiles) { sharedFileWithDetails ->
                                        HomeCard(
                                            sharedFileWithDetails = sharedFileWithDetails,
                                            onAccess = {
                                                viewModel.accessFile(
                                                    context,
                                                    sharedFileWithDetails
                                                )
                                            },
                                            onOpen = {
                                                sharedFileWithDetails.fileRecord?.let { fileRecord ->
                                                    viewModel.openFile(context, fileRecord)
                                                }
                                            },
                                            onDelete = { viewModel.deleteFile(sharedFileWithDetails) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
    LaunchedEffect(homeState.errorMessage, homeState.successMessage) {
        val message = homeState.errorMessage ?: homeState.successMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
            viewModel.clearMessages()
        }
    }


    if (showPinDialog && pinRequired != null) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Enter PIN") },
            text = {
                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            enteredPin = it
                        }
                    },
                    label = { Text("PIN") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.accessFile(context, pinRequired!!, enteredPin)
                    enteredPin = ""
                    showPinDialog = false
                    viewModel.clearPinRequired()
                }) {
                    Text("Submit")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showPinDialog = false
                    viewModel.clearPinRequired()
                }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }
}