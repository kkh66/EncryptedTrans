package com.example.encryptedtrans.Page

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.encryptedtrans.ui.theme.EncryptedTransTheme


@Composable
fun Folder(modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(top = 0.dp, bottom = 5.dp, start = 3.dp, end = 3.dp)
            .border(width = 2.dp, Color.White)
            .fillMaxSize()
    ) {
        Column(
            modifier
                .align(Alignment.TopStart),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Folder",
                modifier
                    .padding(top = 3.dp),
                color = Color.White,
            )
            HorizontalDivider(
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 0.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                color = Color.Gray, thickness = 2.dp
            )
        }
        Column(
            modifier.align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Button(
                onClick = { },
                modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, bottom = 3.dp)
            ) {
                Text("Upload File")
            }
        }
    }
}


@Preview
@Composable
fun FolderPreview() {
    EncryptedTransTheme {
        Folder()
    }
}



