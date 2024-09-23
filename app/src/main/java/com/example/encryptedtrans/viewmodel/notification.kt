package com.example.encryptedtrans.viewmodel

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.encryptedtrans.R


class notification(private val context: Context) {
    private val scanChannel = "file scan"
    private val downloadChannel = "file download"
    private val scanNotificationId = 1
    private val downloadNotificationId = 2

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val scanChannel = NotificationChannel(
            scanChannel,
            "File Scan Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for file scanning progress"
        }
        val downloadChannel = NotificationChannel(
            downloadChannel,
            "File Download Notifications",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notifications for file download progress"
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(scanChannel)
        notificationManager.createNotificationChannel(downloadChannel)
    }

    fun showScanNotification(title: String, content: String, progress: Int = -1) {
        showNotification(scanChannel, scanNotificationId, title, content, progress)
    }

    fun showDownloadNotification(title: String, content: String, progress: Int = -1) {
        showNotification(downloadChannel, downloadNotificationId, title, content, progress)
    }

    private fun showNotification(
        channelId: String,
        notificationId: Int,
        title: String,
        content: String,
        progress: Int = -1
    ) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo_use) // 图标
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
        
        if (progress >= 0) {
            builder.setProgress(100, progress, false)
        } else {
            builder.setProgress(0, 0, false)
        }

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(notificationId, builder.build())
        }
    }
}