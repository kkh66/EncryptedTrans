package com.example.encryptedtrans.Page

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.encryptedtrans.ui.theme.EncryptedTransTheme

@Composable
fun HomePage(modifier: Modifier = Modifier) {
    var Search by remember {
        mutableStateOf("")
    }

}

@Composable
fun Button() {
    Row {
        androidx.compose.material3.Button(onClick = { /*TODO*/ }) {

        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePagePreview() {
    EncryptedTransTheme {
        HomePage()
    }
}