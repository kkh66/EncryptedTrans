package com.example.encryptedtrans


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.encryptedtrans.Page.MainPage
import com.example.encryptedtrans.ui.LoginUi
import com.example.encryptedtrans.ui.Register
import com.example.encryptedtrans.viewmodel.LoginViewModel
import com.example.encryptedtrans.viewmodel.RegisterViewModel

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
        EncryptedTransScreen.Main.name
    } else {
        EncryptedTransScreen.Login.name
    }
    NavHost(
        navController = navController,
        startDestination = check_login,
        builder = {
            composable(EncryptedTransScreen.Login.name) {
                LoginUi(navController,
                    viewModel = viewModel { LoginViewModel(Auth()) }
                )
            }
            composable(EncryptedTransScreen.Main.name) {
                MainPage(modifier, navController)
            }
            composable(EncryptedTransScreen.Register.name) {
                Register(
                    viewModel = viewModel { RegisterViewModel(Auth()) },
                    navController
                )
            }
        }
    )
}

