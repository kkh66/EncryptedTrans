package com.example.encryptedtrans.Page

import LoginViewModel
import android.text.style.BackgroundColorSpan
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import com.example.encryptedtrans.EncryptedTransScreen
import com.example.encryptedtrans.R
import com.example.encryptedtrans.ui.theme.EncryptedTransTheme

@Composable
fun Login(
    navController: NavController, viewModel: LoginViewModel, modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(viewModel.isLoginSuccessful) {
        if (viewModel.isLoginSuccessful) {
            Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
            navController.navigate("Home")
        }
    }
    Box(
        modifier
            .padding(16.dp)
            .fillMaxHeight()
            .fillMaxWidth(),
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.logo_company_removebg_preview),
                contentDescription = "Company Logo",
                modifier = Modifier
                    .size(300.dp)
                    .fillMaxSize()
                    .alpha(0.5f)
            )
            Column() {
                OutlinedTextField(modifier = Modifier.fillMaxWidth(),
                    value = viewModel.email,
                    shape = RoundedCornerShape(25),
                    onValueChange = { viewModel.email = it },
                    label = { Text(text = "Email") })
                Spacer(modifier.height(5.dp))
                OutlinedTextField(modifier = Modifier.fillMaxWidth(),
                    value = viewModel.password,
                    shape = RoundedCornerShape(25),
                    visualTransformation = PasswordVisualTransformation(),
                    onValueChange = { viewModel.password = it },
                    label = { (Text(text = "Password")) })
                Spacer(modifier.height(5.dp))
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(50.dp) // Adjust size as needed
                        )
                    } else {
                        Button(
                            onClick = { viewModel.login() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Login")
                        }
                    }
                }
            }
            Spacer(modifier.height(6.dp))
            TextButton(
                onClick = { navController.navigate(EncryptedTransScreen.Register.name) },
                modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("No Account? Sign Up")
            }
            HorizontalDivider(color = Color.White, thickness = 2.dp)
            Button(
                onClick = {  },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(top = 10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "Google",
                    modifier.size(30.dp)
                )
                Text("Continue with Google", modifier.padding(start = 10.dp))
            }
            Button(onClick = {},
                modifier
                    .padding(top = 10.dp)
                    .height(55.dp)
                    .fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable._968764),
                    contentDescription = "Facebook",
                    modifier
                        .size(30.dp)

                        .fillMaxWidth()
                )
                Text("Continue with Facebook", modifier.padding(start = 10.dp))
            }
        }


    }
}