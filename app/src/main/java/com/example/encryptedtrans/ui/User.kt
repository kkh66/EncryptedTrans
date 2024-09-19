package com.example.encryptedtrans.ui

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.encryptedtrans.EncryptedTransScreen
import com.example.encryptedtrans.R
import com.example.encryptedtrans.viewmodel.UserProfileViewModel

@Composable
fun UserUi(
    modifier: Modifier = Modifier,
    viewModel: UserProfileViewModel,
    navController: NavController,
    mainNavController: NavController
) {
    val context = LocalContext.current


    Box(
        modifier
            .padding(top = 0.dp, bottom = 5.dp, start = 3.dp, end = 3.dp)
            .border(width = 2.dp, color = Color.White)
    ) {
        Column(
            modifier
                .fillMaxSize()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier.padding(5.dp))
            Text("My Profile", fontSize = 24.sp, color = Color.White)

            HorizontalDivider(
                modifier = Modifier
                    .padding(start = 30.dp, end = 30.dp, top = 5.dp, bottom = 10.dp)
                    .fillMaxWidth(),
                color = Color.White, thickness = 2.dp
            )

            Button(
                onClick = { mainNavController.navigate(EncryptedTransScreen.EditUser.name) },
                modifier
                    .padding(end = 16.dp)
                    .align(Alignment.End),
                colors = ButtonDefaults.buttonColors(Color.Transparent)
            ) {
                Text("Edit", color = Color.White)
            }

            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Profile Picture",
                tint = Color.White,
                modifier = Modifier.size(180.dp)
            )


            Text(
                text = viewModel.username,
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier.padding(top = 10.dp)
            )

            Row(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.email_user),
                    color = Color.White,
                    fontSize = 18.sp
                )
                Text(text = viewModel.email, color = Color.White, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.logout()
                    Toast.makeText(context, "Logout Successful", Toast.LENGTH_SHORT).show()
                    navController.navigate(EncryptedTransScreen.Login.name) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
            ) {
                Text("Log Out", fontSize = 18.sp)
            }
        }
    }
}