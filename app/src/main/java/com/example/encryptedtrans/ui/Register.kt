package com.example.encryptedtrans.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.encryptedtrans.EncryptedTransScreen
import com.example.encryptedtrans.R
import com.example.encryptedtrans.viewmodel.RegisterViewModel

@Composable
fun Register(
    viewModel: RegisterViewModel,
    navController: NavController,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    val registerSuccessful = stringResource(R.string.registration_successful)

    LaunchedEffect(viewModel.registerState) {
        viewModel.registerState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
        if (viewModel.registerState.isRegistrationSuccessful) {
            Toast.makeText(context, registerSuccessful, Toast.LENGTH_SHORT).show()
            navController.navigate(EncryptedTransScreen.Login.name)
        }
    }

    val logoUse = if (isDarkTheme) {
        R.drawable.logo_use
    } else {
        R.drawable.logo_company_removebg_preview
    }

    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = logoUse),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(280.dp)
            )
            OutlinedTextField(value = viewModel.username,
                onValueChange = { viewModel.updateUsername(it) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25),
                label = { Text(stringResource(R.string.username)) })
            Spacer(modifier = Modifier.height(5.dp))
            OutlinedTextField(value = viewModel.email,
                onValueChange = { viewModel.updateEmail(it) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25),
                label = { Text(stringResource(R.string.email)) })
            Spacer(modifier = Modifier.height(5.dp))
            OutlinedTextField(
                value = viewModel.password,
                onValueChange = { viewModel.updatePassword(it) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25),
                label = { Text(stringResource(R.string.password)) },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(5.dp))
            OutlinedTextField(
                value = viewModel.confirmPassword,
                onValueChange = { viewModel.updateConfirmPassword(it) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25),
                label = { Text(stringResource(R.string.confirm_password)) },
                visualTransformation = PasswordVisualTransformation()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(top = 10.dp)
            ) {
                if (viewModel.registerState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(40.dp)
                    )
                } else {
                    Button(
                        onClick = { viewModel.register() },
                        modifier = Modifier
                            .height(50.dp)
                            .fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.register))
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .height(50.dp)
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextButton(onClick = { navController.navigate(EncryptedTransScreen.Login.name) }) {
                Text(stringResource(R.string.already_have_account))
            }
        }
    }
}