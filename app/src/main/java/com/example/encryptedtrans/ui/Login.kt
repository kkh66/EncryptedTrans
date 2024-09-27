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
    viewModel: LoginViewModel,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    val loginSuccessful = stringResource(id = R.string.login_successful)
    val resetSuccess = stringResource(R.string.password_reset_email_sent)
    val googleFail = stringResource(R.string.google_failed)
    val scrollState = rememberScrollState()

    //Base on System Theme change logo
    val logoUse = if (isDarkTheme) {
        R.drawable.logo_use
    } else {
        R.drawable.logo_company_removebg_preview
    }


    LaunchedEffect(viewModel.loginState) {
        when {
            viewModel.loginState.isLoginSuccessful -> {
                Toast.makeText(context, loginSuccessful, Toast.LENGTH_SHORT).show()
                navController.navigate(EncryptedTransScreen.Main.name)
            }

            viewModel.loginState.isPasswordResetSent -> {
                Toast.makeText(context, resetSuccess, Toast.LENGTH_SHORT).show()
            }

            viewModel.loginState.errorMessage != null -> {
                Toast.makeText(context, viewModel.loginState.errorMessage, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

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

    // Google sign-in use
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val email = account?.email
            val idToken = account?.idToken
            if (email != null && idToken != null) {
                viewModel.signInWithGoogle(email, idToken)
            } else {
                Toast.makeText(context, "Google Sign-In failed: Missing email or ID token.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(context, googleFail, Toast.LENGTH_SHORT).show()
        }
    }


    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxHeight()
            .verticalScroll(scrollState),
    ) {
        Column(modifier = Modifier.padding(top = 60.dp)) {
            Image(
                painter = painterResource(id = logoUse),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier
                    .size(350.dp)
                    .fillMaxSize()
                    .padding(top = 60.dp)
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
                            onClick = {
                                viewModel.login()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(0.dp)
                                .height(50.dp)
                        ) {
                            Text(stringResource(id = R.string.login))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            TextButton(
                onClick = { navController.navigate(EncryptedTransScreen.Register.name) },
                modifier = Modifier
                    .height(50.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(stringResource(id = R.string.sign_up))
            }

            HorizontalDivider(thickness = 2.dp)

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
                    contentDescription = stringResource(R.string.google),
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    stringResource(id = R.string.continue_with_google),
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }
    }
}