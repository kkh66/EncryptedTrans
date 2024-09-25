package com.example.encryptedtrans.ui

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
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
    val profileState by viewModel.profileState.collectAsState()

    val profileImage by produceState<ImageRequest?>(null) {
        value = viewModel.profileImagePath?.let { path ->
            ImageRequest.Builder(context)
                .data(path)
                .crossfade(true)
                .transformations(CircleCropTransformation())
                .build()
        }
    }

    Box(
        modifier
            .padding(top = 5.dp, bottom = 5.dp, start = 3.dp, end = 3.dp)
            .border(width = 2.dp, color = MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier
                .fillMaxSize()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier.padding(5.dp))
            Text(stringResource(id = R.string.user_profile), fontSize = 24.sp)

            HorizontalDivider(
                modifier = Modifier
                    .padding(start = 30.dp, end = 30.dp, top = 0.dp, bottom = 0.dp)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.outline, thickness = 2.dp
            )

            Button(
                onClick = { mainNavController.navigate(EncryptedTransScreen.EditUser.name) },
                modifier
                    .padding(end = 16.dp)
                    .align(Alignment.End),
                colors = ButtonDefaults.buttonColors(Color.Transparent)
            ) {
                Text(stringResource(R.string.edit), color = MaterialTheme.colorScheme.outline)
            }

            if (profileImage != null) {
                Image(
                    painter = rememberAsyncImagePainter(profileImage),
                    contentDescription = stringResource(R.string.profile_picture),
                    modifier = Modifier
                        .size(180.dp)
                        .padding(8.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = stringResource(R.string.profile_picture),
                    modifier = Modifier.size(180.dp)
                )
            }


            Text(
                text = viewModel.username,
                fontSize = 20.sp,
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
                    fontSize = 18.sp
                )
                Text(text = viewModel.email, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.weight(1f))

            if (profileState.isLoggedOut) {
                LaunchedEffect(Unit) {
                    navController.navigate(EncryptedTransScreen.Login.name) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            } else {
                Button(
                    onClick = {
                        viewModel.logout() // Trigger logout
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                ) {
                    Text(stringResource(R.string.logout), fontSize = 18.sp)
                }
            }
        }
    }
}