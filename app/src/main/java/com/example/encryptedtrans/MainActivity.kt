package com.example.encryptedtrans

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.encryptedtrans.ui.theme.EncryptedTransTheme
import io.github.vinceglb.filekit.core.FileKit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FileKit.init(this)
        enableEdgeToEdge()
        setContent {
            EncryptedTransTheme() {
                Surface(modifier = Modifier.padding(top = 0.dp)) {
                    NavControl(
                        navController = rememberNavController(), auth = Auth()
                    )
                }
            }
        }
    }
}



