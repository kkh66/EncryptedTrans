package com.example.encryptedtrans.ui

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import com.example.encryptedtrans.ui.theme.EncryptedTransTheme
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter


private fun generateQRCode(content: String, size: Int): Bitmap {
    val bitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
        }
    }
    return bitmap
}


@Composable
fun QRCodeImage(url: String, modifier: Modifier = Modifier) {
    val bitmap = remember(url) { generateQRCode(url, 512) }
    Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = modifier)
}


@Composable
fun UsingQr() {
    val url = "https://www.ggwp.tech/"
    QRCodeImage(url)
}


@Preview(showBackground = true)
@Composable
fun QrPreview() {
    EncryptedTransTheme {
        UsingQr()
    }
}

