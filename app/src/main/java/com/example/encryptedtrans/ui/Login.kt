package com.example.encryptedtrans.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.encryptedtrans.EncryptedTransScreen
import com.example.encryptedtrans.R
import com.example.encryptedtrans.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginUi(
    navController: NavController,
    viewModel: LoginViewModel
) {
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }

    // Password reset dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(id = R.string.reset_password)) },
            text = {
                OutlinedTextField(
                    value = viewModel.resetEmail,
                    onValueChange = { viewModel.updateResetEmail(it) },
                    label = { Text(stringResource(id = R.string.email)) }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.sendPasswordResetEmail()
                    showResetDialog = false
                }) {
                    Text(stringResource(id = R.string.send_reset_email))
                }
            },
            dismissButton = {
                Button(onClick = { showResetDialog = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }

    // Handle different login states in a single LaunchedEffect
    LaunchedEffect(viewModel.loginState) {
        when {
            viewModel.loginState.isLoginSuccessful -> {
                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                navController.navigate(EncryptedTransScreen.Main.name)
            }

            viewModel.loginState.isPasswordResetSent -> {
                Toast.makeText(context, "Password reset email sent!", Toast.LENGTH_SHORT).show()
            }

            viewModel.loginState.errorMessage != null -> {
                Toast.makeText(context, viewModel.loginState.errorMessage, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    // Google sign-in launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { token ->
                viewModel.signInWithGoogle(token)
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Google sign-in failed", Toast.LENGTH_SHORT).show()
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxHeight()
            .verticalScroll(scrollState),
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.logo_use),
                contentDescription = "Company Logo",
                modifier = Modifier
                    .size(300.dp)
                    .fillMaxSize()
                    .padding(top = 20.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = viewModel.email,
                    shape = RoundedCornerShape(25),
                    onValueChange = { viewModel.updateEmail(it) },
                    label = { Text(stringResource(id = R.string.email)) }
                )

                Spacer(modifier = Modifier.height(5.dp))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = viewModel.password,
                    shape = RoundedCornerShape(25),
                    visualTransformation = PasswordVisualTransformation(),
                    onValueChange = { viewModel.updatePassword(it) },
                    label = { Text(stringResource(id = R.string.password)) }
                )

                TextButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier
                        .height(50.dp)
                        .align(Alignment.End)
                ) {
                    Text(stringResource(id = R.string.forgot_password))
                }

                Spacer(modifier = Modifier.height(5.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    if (viewModel.loginState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(50.dp)
                        )
                    } else {
                        Button(
                            onClick = { viewModel.login() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(stringResource(id = R.string.login))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Navigation to Register screen
            TextButton(
                onClick = { navController.navigate(EncryptedTransScreen.Register.name) },
                modifier = Modifier
                    .height(50.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("No Account? Sign Up")
            }

            HorizontalDivider(color = Color.White, thickness = 2.dp)

            // Google sign-in button
            Button(
                onClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(top = 10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "Google",
                    modifier = Modifier.size(30.dp)
                )
                Text("Continue with Google", modifier = Modifier.padding(start = 10.dp))
            }

            // Facebook sign-in button
            Button(
                onClick = {
                    // Handle Facebook sign-in logic
                },
                modifier = Modifier
                    .padding(top = 10.dp)
                    .height(55.dp)
                    .fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable._968764),
                    contentDescription = "Facebook",
                    modifier = Modifier.size(30.dp)
                )
                Text("Continue with Facebook", modifier = Modifier.padding(start = 10.dp))
            }
        }
    }
}