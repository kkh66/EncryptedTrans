package com.example.encryptedtrans.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.encryptedtrans.Auth
import com.example.encryptedtrans.EncryptedTransScreen
import com.example.encryptedtrans.Page.Folder
import com.example.encryptedtrans.viewmodel.FileViewModel
import com.example.encryptedtrans.viewmodel.UserProfileViewModel
import io.github.vinceglb.filekit.core.FileKitPlatformSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainUi(
    modifier: Modifier,
    navController: NavController,
    platformSettings: FileKitPlatformSettings? = null,

    ) {
    val mainNavController = rememberNavController()
    val userProfileViewModel: UserProfileViewModel = viewModel { UserProfileViewModel(Auth()) }
    val context = LocalContext.current
    val fileViewModel: FileViewModel = viewModel { FileViewModel(Auth(), context) }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Protect,Encrypt,Deliver", fontSize = 20.sp)
                },
                modifier
                    .fillMaxWidth()
                    .padding(0.dp)
                    .background(color = Color.Transparent)
                    .statusBarsPadding(),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            BottomAppBar(
                modifier.background(color = Color.Transparent), containerColor = Color.Transparent
            ) {
                Column {
                    HorizontalDivider(
                        color = Color.Gray, thickness = 1.dp
                    )
                    Row(
                        modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = { mainNavController.navigate(EncryptedTransScreen.Folder.name) },
                                modifier
                                    .padding(0.dp),
                                colors = ButtonDefaults.buttonColors(Color.Transparent)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Folder,
                                    contentDescription = "Folder",
                                    modifier
                                        .padding(bottom = 0.dp)
                                        .size(40.dp),
                                    tint = Color.White
                                )
                            }
                            Text("Folder")
                        }
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Button(
                                onClick = { mainNavController.navigate(EncryptedTransScreen.Home.name) },
                                modifier
                                    .padding(0.dp),
                                colors = ButtonDefaults.buttonColors(Color.Transparent)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Home,
                                    contentDescription = "Home",
                                    modifier
                                        .padding(0.dp)
                                        .size(40.dp),
                                    tint = Color.White
                                )
                            }
                            Text("Home")
                        }
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = { mainNavController.navigate(EncryptedTransScreen.Account.name) },
                                modifier
                                    .width(140.dp)
                                    .padding(0.dp),
                                colors = ButtonDefaults.buttonColors(Color.Transparent)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Account",
                                    modifier
                                        .padding(0.dp)
                                        .size(40.dp),
                                    tint = Color.White
                                )
                            }
                            Text("Account")
                        }
                    }
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
                FileUi(viewModel = fileViewModel, platformSettings = platformSettings)
            }
            composable(EncryptedTransScreen.Home.name) {
                HomeUi()
            }
            composable(EncryptedTransScreen.Account.name) {
                UserUi(
                    viewModel = userProfileViewModel,
                    navController = navController,
                    mainNavController = mainNavController
                )
            }
            composable(EncryptedTransScreen.EditUser.name) {
                EditUserUi(viewModel = userProfileViewModel, navController = mainNavController)
            }
        }
    }
}