package com.example.encryptedtrans.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.encryptedtrans.Auth
import com.example.encryptedtrans.EncryptedTransScreen
import com.example.encryptedtrans.R
import com.example.encryptedtrans.viewmodel.FileViewModel
import com.example.encryptedtrans.viewmodel.HomeFileViewModel
import com.example.encryptedtrans.viewmodel.UserProfileViewModel
import io.github.vinceglb.filekit.core.FileKitPlatformSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainUi(
    modifier: Modifier,
    navController: NavController,
    platformSettings: FileKitPlatformSettings? = null,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val mainNavController = rememberNavController()
    val context = LocalContext.current
    val userProfileViewModel: UserProfileViewModel =
        viewModel { UserProfileViewModel(Auth(), context) }
    var fabAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val fileViewModel: FileViewModel = viewModel { FileViewModel(Auth(), context) }
    var selectedScreen by remember { mutableStateOf(EncryptedTransScreen.Home) }
    val slogonUse = if (isDarkTheme) {
        R.drawable.sfts_mobile_removebg_preview
    } else {
        R.drawable.sfts_mobile__3__removebg_preview
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(slogonUse),
                        contentDescription = stringResource(id = R.string.company_slogan),
                        modifier
                            .size(150.dp)
                            .height(50.dp)
                            .padding(0.dp),
                    )
                },
                modifier
                    .fillMaxWidth()
                    .padding(0.dp)
                    .height(50.dp)
                    .background(color = Color.Transparent)
                    .statusBarsPadding(),
                windowInsets = WindowInsets(top = 0.dp, bottom = 0.dp),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            BottomAppBar(
                modifier.background(color = Color.Transparent),
                containerColor = Color.Transparent
            ) {
                Column {
                    HorizontalDivider(thickness = 1.dp)
                    Row(
                        modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            Triple(
                                Icons.Outlined.Folder to Icons.Filled.Folder,
                                R.string.folder,
                                EncryptedTransScreen.Folder
                            ),
                            Triple(
                                Icons.Outlined.Home to Icons.Filled.Home,
                                R.string.home,
                                EncryptedTransScreen.Home
                            ),
                            Triple(
                                Icons.Outlined.Person to Icons.Filled.Person,
                                R.string.account,
                                EncryptedTransScreen.Account
                            )
                        ).forEach { (icons, labelResId, screen) ->
                            var isHovered by remember { mutableStateOf(false) }
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .pointerInput(Unit) {
                                        awaitPointerEventScope {
                                            while (true) {
                                                val event = awaitPointerEvent()
                                                when (event.type) {
                                                    PointerEventType.Enter -> isHovered = true
                                                    PointerEventType.Exit -> isHovered = false
                                                }
                                            }
                                        }
                                    }
                            ) {
                                Button(
                                    onClick = {
                                        selectedScreen = screen
                                        mainNavController.navigate(screen.name)
                                    },
                                    modifier = Modifier.padding(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when {
                                            selectedScreen == screen -> MaterialTheme.colorScheme.primaryContainer
                                            isHovered -> MaterialTheme.colorScheme.secondaryContainer
                                            else -> Color.Transparent
                                        }
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (selectedScreen == screen) icons.second else icons.first,
                                        contentDescription = stringResource(id = labelResId),
                                        modifier = Modifier
                                            .padding(0.dp)
                                            .size(40.dp),
                                        tint = if (selectedScreen == screen)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.outline
                                    )
                                }
                                Text(
                                    text = stringResource(id = labelResId),
                                    color = if (selectedScreen == screen)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedScreen == EncryptedTransScreen.Folder || selectedScreen == EncryptedTransScreen.Home) {
                FloatingActionButton(onClick = { fabAction?.invoke() }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.refresh)
                    )
                }
            }
        }

    ) { paddingValues ->
        NavHost(
            navController = mainNavController,
            startDestination = EncryptedTransScreen.Home.name,
            modifier = Modifier
                .padding(start = 5.dp, end = 5.dp)
                .padding(paddingValues)
        ) {
            composable(EncryptedTransScreen.Folder.name) {
                FileUi(
                    viewModel = fileViewModel,
                    platformSettings = platformSettings,
                    onFabClick = { fabAction = it })
            }
            composable(EncryptedTransScreen.Home.name) {
                HomeUi(
                    viewModel = HomeFileViewModel(Auth(), context),
                    onFabClick = { fabAction = it })
            }
            composable(EncryptedTransScreen.Account.name) {
                UserUi(
                    viewModel = userProfileViewModel,
                    navController = navController,
                    mainNavController = mainNavController
                )
            }
            composable(EncryptedTransScreen.EditUser.name) {
                EditUserUi(
                    viewModel = userProfileViewModel,
                    navController = mainNavController,
                    mainHostController = navController,
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange,
                    platformSettings = platformSettings
                )
            }
        }
    }
}