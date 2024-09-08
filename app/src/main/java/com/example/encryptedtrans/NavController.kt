package com.example.encryptedtrans

import com.example.encryptedtrans.Page.FileShareApp
import LoginViewModel
import com.example.encryptedtrans.Viewmodels.RegisterViewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.encryptedtrans.Page.Folder
import com.example.encryptedtrans.Page.Login
import com.example.encryptedtrans.Page.Register
import com.example.encryptedtrans.Page.UserProfile
import com.example.encryptedtrans.Viewmodels.UserProfileViewmodel
import com.google.firebase.firestore.auth.User

enum class EncryptedTransScreen {
    Login,
    Main,
    Register,
    Folder,
    Home,
    Account
}

@Composable
fun NavControl(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    auth: Auth
) {
    val check_login = if (auth.isUserLoggedIn()) {
        EncryptedTransScreen.Home.name
    } else {
        EncryptedTransScreen.Login.name
    }
    NavHost(
        navController = navController,
        startDestination = check_login,
        builder = {
            composable(EncryptedTransScreen.Login.name) {
                Login(navController, viewModel = viewModel { LoginViewModel(Auth()) })
            }
            composable(EncryptedTransScreen.Home.name) {
                FileShareApp(modifier, navController)
            }
            composable(EncryptedTransScreen.Register.name) {
                Register(
                    viewModel = viewModel { RegisterViewModel(Auth()) },
                    navController
                )
            }
            composable(EncryptedTransScreen.Folder.name) {
                Folder()
            }
            composable(EncryptedTransScreen.Account.name) {
                UserProfile(viewmodel = UserProfileViewmodel(Auth()), navController = navController)
            }
        })
}

