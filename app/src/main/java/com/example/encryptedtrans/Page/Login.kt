package com.example.encryptedtrans.Page

import android.text.style.BackgroundColorSpan
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import com.example.encryptedtrans.R
import com.example.encryptedtrans.ui.theme.EncryptedTransTheme

@Composable
fun Login(modifier: Modifier = Modifier) {
    var Username by remember { mutableStateOf("") }
    var Password by remember { mutableStateOf("") }
    Column(
        modifier
            .background(Color.Black)

            .padding(16.dp)
            .fillMaxHeight()
            .fillMaxWidth()
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_company_removebg_preview),
            contentDescription = "Company Logo",
            modifier = Modifier
                .size(300.dp)
                .fillMaxSize()
                .alpha(0.5f)

        )
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = Username,
                onValueChange = { Username = it },
                label = { Text(text = "Username", color = Color.White) })
            Spacer(modifier.height(5.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = Password,
                visualTransformation = PasswordVisualTransformation(),
                onValueChange = { Password = it },
                label = { (Text(text = "Password", color = Color.White)) })
            Spacer(modifier.height(5.dp))
            Button(
                onClick = { /*TODO*/ },
                modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.onPrimary)

            ) {
                Text("Login")
            }
        }
        Spacer(modifier.height(6.dp))
        HorizontalDivider(color = Color.White, thickness = 1.dp)
        Spacer(modifier.height(5.dp))
        Button(onClick = {}) {
            Text("Continue with Facebook")
        }

    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    EncryptedTransTheme {
        Login()
    }
}