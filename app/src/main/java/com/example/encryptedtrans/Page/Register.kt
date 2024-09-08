package com.example.encryptedtrans.Page

import com.example.encryptedtrans.Viewmodels.RegisterViewModel
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.encryptedtrans.R

@Composable
fun Register(
    viewModel: RegisterViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(viewModel.isRegistrationSuccessful) {
        if (viewModel.isRegistrationSuccessful) {
            Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
            navController.navigate("login")
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier
                .padding(16.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_company_removebg_preview),
                contentDescription = "Company Logo",
                modifier.size(300.dp)
            )
            OutlinedTextField(
                value = viewModel.username,
                onValueChange = { viewModel.username = it },
                modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25),
                label = { Text("Username") }
            )
            Spacer(modifier.height(5.dp))
            OutlinedTextField(
                value = viewModel.email,
                onValueChange = { viewModel.email = it },
                modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25),
                label = { Text("Email") }
            )
            Spacer(modifier.height(5.dp))
            OutlinedTextField(
                value = viewModel.password,
                onValueChange = { viewModel.password = it },
                modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25),
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier.height(5.dp))
            OutlinedTextField(
                value = viewModel.confirmPassword,
                onValueChange = { viewModel.confirmPassword = it },
                modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25),
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation()
            )
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(top = 10.dp)
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(40.dp) // Adjust size as needed
                    )
                } else {
                    Button(
                        onClick = { viewModel.register() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Register")
                    }
                }
            }
        }
        Column(
            modifier
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextButton(onClick = { navController.navigate("login") }) {
                Text("Already have an Account? Sign In")
            }
        }
    }


}
