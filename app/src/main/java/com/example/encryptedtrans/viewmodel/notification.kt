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
    private val scanChannelId = "file_scan_channel"
    private val downloadChannelId = "file_download_channel"
    private val scanNotificationId = 1
    private val downloadNotificationId = 2

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val scanChannel = NotificationChannel(
            scanChannelId,
            "File Scan Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for file scanning progress"
        }
        val downloadChannel = NotificationChannel(
            downloadChannelId,
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
        showNotification(scanChannelId, scanNotificationId, title, content, progress)
    }

    fun showDownloadNotification(title: String, content: String, progress: Int = -1) {
        showNotification(downloadChannelId, downloadNotificationId, title, content, progress)
    }

    private fun showNotification(
        channelId: String,
        notificationId: Int,
        title: String,
        content: String,
        progress: Int = -1
    ) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.download)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)

        if (progress >= 0) {
            builder.setProgress(100, progress, false)
        }

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(notificationId, builder.build())
        }
    }

    fun cancelScanNotification() {
        cancelNotification(scanNotificationId)
    }

    fun cancelDownloadNotification() {
        cancelNotification(downloadNotificationId)
    }

    private fun cancelNotification(notificationId: Int) {
        with(NotificationManagerCompat.from(context)) {
            cancel(notificationId)
        }
    }
}