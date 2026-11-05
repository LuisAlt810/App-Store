package com.appstore.appstore

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppStoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channels = listOf(
            NotificationChannel(
                "downloads",
                "Downloads",
                android.app.NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Track download progress and resumed activity"
                setSound(null, null)
            },
            NotificationChannel(
                "installs",
                "Installs",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Installation results and APK verification status"
            },
            NotificationChannel(
                "updates",
                "Updates",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Available app updates and update-all actions"
            }
        )

        notificationManager.createNotificationChannels(channels)
    }
}
