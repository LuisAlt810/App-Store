package com.appstore.appstore.download

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.appstore.appstore.R

class DownloadForegroundService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var progress = 0
    private var appName: String = "App-Store"

    private val progressRunnable = object : Runnable {
        override fun run() {
            if (progress >= 100) {
                val completed = NotificationCompat.Builder(this@DownloadForegroundService, "downloads")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Download finished")
                    .setContentText("$appName has been downloaded and is ready to install.")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setAutoCancel(true)
                    .build()

                stopForeground(STOP_FOREGROUND_REMOVE)
                startForeground(1002, completed)
                handler.postDelayed({ stopSelf() }, 1500)
                return
            }

            progress += 10
            val notification = NotificationCompat.Builder(this@DownloadForegroundService, "downloads")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Downloading $appName")
                .setContentText("Background download is in progress")
                .setProgress(100, progress, false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

            startForeground(1001, notification)
            handler.postDelayed(this, 2000)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        appName = intent?.getStringExtra("app_name") ?: "App-Store"
        progress = 0
        handler.removeCallbacksAndMessages(null)
        handler.post(progressRunnable)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
