package com.example.encryptedtrans.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.example.encryptedtrans.EncryptedTransScreen
import com.example.encryptedtrans.R
import com.example.encryptedtrans.viewmodel.UserProfileViewModel
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.core.PickerType

@Composable
fun EditUserUi(
    modifier: Modifier = Modifier,
    viewModel: UserProfileViewModel,
    navController: NavController,
    mainHostController: NavController,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    platformSettings: FileKitPlatformSettings?
) {
    val context = LocalContext.current
    var changeEmail by remember { mutableStateOf(false) }
    var changePassword by remember { mutableStateOf(false) }

    val currentProfileImageUri by viewModel.currentProfileImageUri.collectAsState()
    val isProfileImageLoading by viewModel.isProfileImageLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfileImage()
    }


    LaunchedEffect(viewModel.profileState.errorMessage) {
        viewModel.profileState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    val imagePicker = rememberFilePickerLauncher(
        type = PickerType.Image,
        title = "Select Profile Picture",
        onResult = { file ->
            file?.let { viewModel.saveProfileImage(it.uri) }
        },
        platformSettings = platformSettings
    )

    LaunchedEffect(viewModel.profileState.isUpdateSuccessful) {
        if (viewModel.profileState.isUpdateSuccessful) {
            Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            viewModel.resetUpdateSuccessState()
            navController.popBackStack()
        }
    }

    LaunchedEffect(viewModel.profileState.isEmailChangeSent) {
        if (viewModel.profileState.isEmailChangeSent) {
            Toast.makeText(
                context,
                "A email have sent to you please check.Please Login Again",
                Toast.LENGTH_SHORT
            ).show()
            viewModel.logout()
            mainHostController.navigate(EncryptedTransScreen.Login.name) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    Box(
        modifier
            .fillMaxSize()
            .padding(top = 5.dp, bottom = 5.dp, start = 3.dp, end = 3.dp)
            .border(width = 2.dp, color = MaterialTheme.colorScheme.outline)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 0.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier.padding(5.dp))
                Text(
                    stringResource(id = R.string.edit_profile),
                    fontSize = 24.sp
                )
                HorizontalDivider(
                    modifier = Modifier
                        .padding(start = 30.dp, end = 30.dp, top = 0.dp, bottom = 0.dp)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.outline, thickness = 2.dp
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .padding(16.dp)
                        .clickable { imagePicker.launch() }
                ) {
                    when {
                        isProfileImageLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        currentProfileImageUri != null -> {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(context)
                                        .data(currentProfileImageUri)
                                        .crossfade(true)
                                        .transformations(CircleCropTransformation())
                                        .build()
                                ),
                                contentDescription = stringResource(R.string.profile_picture),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        else -> {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = stringResource(R.string.profile_picture),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
                Button(
                    onClick = { viewModel.deleteProfileImage() },
                    enabled = currentProfileImageUri != null
                ) {
                    Text(stringResource(R.string.remove_profile_picture))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                OutlinedTextField(
                    value = viewModel.username,
                    onValueChange = { viewModel.updateUsername(it) },
                    label = { Text(stringResource(R.string.username)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.change_email))
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
                        label = { Text(stringResource(R.string.current_email)) },
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
                        label = { Text(stringResource(R.string.new_email)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = viewModel.password,
                        onValueChange = { viewModel.updatePassword(it) },
                        label = { Text(stringResource(R.string.password_email_change)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.change_password))
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = changePassword,
                        onCheckedChange = { changePassword = it }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (changePassword) {
                item {
                    OutlinedTextField(
                        value = viewModel.currentPassword,
                        onValueChange = { viewModel.updateCurrentPassword(it) },
                        label = { Text(stringResource(R.string.current_password)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = viewModel.newPassword,
                        onValueChange = { viewModel.updateNewPassword(it) },
                        label = { Text(stringResource(R.string.new_password)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = viewModel.confirmNewPassword,
                        onValueChange = { viewModel.updateConfirmNewPassword(it) },
                        label = { Text(stringResource(R.string.confirm_new_password)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.dark_theme))
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onThemeChange(!isDarkTheme) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }


            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 0.dp)
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
                                when {
                                    changeEmail -> viewModel.sendEmailChangeRequest()
                                    changePassword -> viewModel.changePassword()
                                    else -> viewModel.saveChanges()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp)
                                .padding(bottom = 5.dp),
                            enabled = viewModel.username.isNotEmpty() &&
                                    (!changeEmail || (viewModel.newEmail.isNotEmpty() && viewModel.password.isNotEmpty())) &&
                                    (!changePassword || (viewModel.currentPassword.isNotEmpty() && viewModel.newPassword.isNotEmpty() && viewModel.confirmNewPassword.isNotEmpty()))
                        ) {
                            Text(stringResource(id = R.string.update))
                        }
                    }
                }
            }
        }
    }
}
