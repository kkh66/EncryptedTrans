package com.example.encryptedtrans.Page

import android.text.style.BackgroundColorSpan
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Label
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.encryptedtrans.ui.theme.EncryptedTransTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(modifier: Modifier = Modifier){
    var Username by remember { mutableStateOf("") }
    var Password by remember { mutableStateOf("") }
    Column(
        modifier
            .background(color = Color.Black)
            .padding(40.dp)
            .fillMaxHeight()
            .then(
                Modifier.wrapContentSize(
                    Alignment.Center
                )
            )) {

        OutlinedTextField(  value = Username , onValueChange ={Username=it}, label = { Text(color = Color.White, text = "Username")} )
        Spacer(modifier . height(5.dp))
        OutlinedTextField(value = Password, onValueChange = {Password=it}, label = { ( Text(color = Color.White, text = "Password"))})
        Spacer(modifier . height(5.dp))
        Button(onClick = { /*TODO*/ },
            modifier
                .width(350.dp)) {
            Text("Login")
        }
        Spacer(modifier . height(6.dp))
        HorizontalDivider(color = Color.White, thickness = 1.dp)
        Spacer(modifier . height(5.dp))
        Button(onClick = { /*TODO*/ }) {
            
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