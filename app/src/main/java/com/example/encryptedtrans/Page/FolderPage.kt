package com.example.encryptedtrans.Page

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContentResolverCompat
import com.example.encryptedtrans.utils.BorderColum
import com.example.encryptedtrans.utils.Utils

@Composable
fun Folder(modifier: Modifier=Modifier){
    // This state holds the file URI that the user selects
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }

    // Set up the ActivityResultLauncher to handle the file picking intent
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(), // ACTION_GET_CONTENT
        onResult = { uri: Uri? ->
            selectedFileUri = uri

            // Optionally, get file details like name and size
            uri?.let {
                //fileName = it.getFileName() // Fetch the file name
            }
        }
    )
    Box(modifier.fillMaxSize()){
        Column(verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { /*TODO*/ }) {
                Text("Upload File")
            }
        }
    }
}

