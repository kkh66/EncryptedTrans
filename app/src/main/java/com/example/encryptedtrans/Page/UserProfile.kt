package com.example.encryptedtrans.Page

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.encryptedtrans.EncryptedTransScreen
import com.example.encryptedtrans.Viewmodels.UserProfileViewmodel
import com.example.encryptedtrans.ui.theme.EncryptedTransTheme

@Composable
fun UserProfile(
    modifier: Modifier = Modifier,
    viewmodel: UserProfileViewmodel,
    navController: NavHostController
) {
    Box(
        modifier
            .padding(top = 0.dp, bottom = 5.dp, start = 3.dp, end = 3.dp)
            .border(width = 2.dp, Color.White)
    ) {
        Column(
            modifier
                .fillMaxSize()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier.padding(3.dp))
            Text("My Profile")
            HorizontalDivider(
                modifier
                    .padding(start = 30.dp, end = 30.dp)
                    .fillMaxWidth(),
                color = Color.White, thickness = 5.dp
            )

            Button(
                onClick = { /*TODO*/ },
                modifier
                    .padding(end = 16.dp)
                    .align(Alignment.End),
                colors = ButtonDefaults.buttonColors(Color.Transparent)
            ) {
                Text("Edit", color = Color.White)
            }
        }
        Column(
            modifier.align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            Button(
                onClick = {
                    viewmodel.logout()
                    navController.navigate(EncryptedTransScreen.Login.name)
                },
                modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            ) {
                Text("Logout")
            }
        }
    }

}
