package com.example.encryptedtrans.Page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Label
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.encryptedtrans.ui.theme.EncryptedTransTheme

@Composable
fun Register(modifier: Modifier = Modifier) {
    var Username by remember { mutableStateOf("") }
    var Email by remember {
        mutableStateOf("")
    }
    var New_Password by remember { mutableStateOf("") }
    var Confirm_Password by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        OutlinedTextField(
            value = Username,
            onValueChange = { Username = it },
            label = { Text("Username") }
        )
        OutlinedTextField(
            value = Email,
            onValueChange = { Email = it },
            label = { Text("Email") })
        OutlinedTextField(
            value = New_Password,
            onValueChange = { New_Password = it },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
        )
        OutlinedTextField(
            value = Confirm_Password,
            onValueChange = { Confirm_Password = it },
            label = { Text("Confirm Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Button(onClick = { /*TODO*/ }
        ) {
            Text("Register")

        }
    }
}


@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    EncryptedTransTheme {
        Register()
    }
}