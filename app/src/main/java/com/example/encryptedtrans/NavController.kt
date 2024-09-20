package com.example.encryptedtrans


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.encryptedtrans.ui.LoginUi
import com.example.encryptedtrans.ui.MainUi
import com.example.encryptedtrans.ui.Register
import com.example.encryptedtrans.viewmodel.LoginViewModel
import com.example.encryptedtrans.viewmodel.RegisterViewModel

enum class EncryptedTransScreen {
    Login,
    Main,
    Register,
    Folder,
    Home,
    Account,
    EditUser,
    Result
}

@Composable
fun NavControl(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    auth: Auth = Auth()
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
                    viewModel = viewModel { LoginViewModel(auth) }
                )
            }
            composable(EncryptedTransScreen.Main.name) {
                MainUi(modifier, navController)
            }
            composable(EncryptedTransScreen.Register.name) {
                Register(
                    viewModel = viewModel { RegisterViewModel(auth) },
                    navController
                )
            }
        }
    )
}

