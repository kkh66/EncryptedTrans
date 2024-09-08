package com.example.encryptedtrans.Page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.encryptedtrans.Auth
import com.example.encryptedtrans.EncryptedTransScreen
import com.example.encryptedtrans.Viewmodels.UserProfileViewmodel
import com.example.encryptedtrans.ui.theme.EncryptedTransTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileShareApp(modifier: Modifier = Modifier, navController: NavHostController) {
    Scaffold(contentWindowInsets = WindowInsets(top = (0.dp)), topBar = {
        CenterAlignedTopAppBar(
            title = {
                Text("Protect,Encrypt,Deliver", fontSize = 20.sp)
            },
            modifier
                .padding(0.dp)
                .fillMaxWidth()
                .statusBarsPadding(),
            windowInsets = WindowInsets(top = (0.dp))
        )
    }, bottomBar = {
        BottomAppBar(
            modifier.background(color = Color.Transparent), containerColor = Color.Transparent
        ) {
            Column {
                HorizontalDivider(
                    color = Color.Gray, thickness = 1.dp
                )
                ButtonUse(modifier, navController)
            }
        }
    }

    ) { innerPadding ->
        Surface(modifier.padding(innerPadding)) {
            UserProfile(modifier, viewmodel = UserProfileViewmodel(auth = Auth()), navController)
        }

    }
}


@Composable
fun ButtonUse(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {

    Row {
        Column(
            modifier.width(130.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { navController.navigate(EncryptedTransScreen.Folder.name) },
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
            modifier.size(130.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,

            ) {
            Button(
                onClick = { EncryptedTransScreen.Home.name },
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
            modifier.width(130.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { EncryptedTransScreen.Account.name },
                modifier
                    .width(140.dp)
                    .padding(0.dp),
                colors = ButtonDefaults.buttonColors(Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.Filled.Person, contentDescription = "Account",
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

