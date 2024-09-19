package com.example.encryptedtrans.ui

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.encryptedtrans.viewmodel.UserProfileViewModel

@Composable
fun EditUserUi(
    modifier: Modifier = Modifier,
    viewModel: UserProfileViewModel,
    navController: NavController,
) {
    val context = LocalContext.current
    var changeEmail by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.profileState.errorMessage) {
        viewModel.profileState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(viewModel.profileState.isUpdateSuccessful) {
        if (viewModel.profileState.isUpdateSuccessful) {
            Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            viewModel.resetUpdateSuccessState()
            navController.popBackStack()
        }
    }

    LaunchedEffect(viewModel.profileState.isEmailChangeSent) {
        if (viewModel.profileState.isEmailChangeSent) {
            Toast.makeText(context, "Please check your email to confirm the change", Toast.LENGTH_LONG).show()
            viewModel.resetUpdateSuccessState()
        }
    }

    Box(
        modifier
            .fillMaxSize()
            .padding(top = 0.dp, bottom = 5.dp, start = 3.dp, end = 3.dp)
            .border(width = 2.dp, Color.White)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text("Edit Profile", fontSize = 24.sp, color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Profile Picture",
                    tint = Color.White,
                    modifier = Modifier.size(180.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                OutlinedTextField(
                    value = viewModel.username,
                    onValueChange = { viewModel.updateUsername(it) },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Change Email?", color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = changeEmail,
                        onCheckedChange = { changeEmail = it }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (changeEmail) {
                item {
                    OutlinedTextField(
                        value = viewModel.email,
                        onValueChange = { },
                        label = { Text("Current Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = viewModel.newEmail,
                        onValueChange = { viewModel.updateNewEmail(it) },
                        label = { Text("New Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = viewModel.password,
                        onValueChange = { viewModel.updatePassword(it) },
                        label = { Text("Password (required for email change)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    if (viewModel.profileState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(50.dp)
                        )
                    } else {
                        Button(
                            onClick = {
                                if (changeEmail) {
                                    viewModel.sendEmailChangeRequest()
                                } else {
                                    viewModel.saveChanges()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = viewModel.username.isNotEmpty() &&
                                    (!changeEmail || (viewModel.newEmail.isNotEmpty() && viewModel.password.isNotEmpty()))
                        ) {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }
}
