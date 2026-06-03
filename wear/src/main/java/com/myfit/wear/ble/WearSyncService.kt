package com.myfit.wear.ble

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WearSyncService : Service() {

    @Inject lateinit var bleManager: WearBleManager

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIF_ID, buildNotification(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        runCatching { bleManager.startAdvertising() }
    }

    override fun onDestroy() {
        bleManager.stopAdvertising()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val channelId = "myfit_sync"
        val mgr = getSystemService(NotificationManager::class.java)
        if (mgr.getNotificationChannel(channelId) == null) {
            mgr.createNotificationChannel(
                NotificationChannel(channelId, "MyFit Sync", NotificationManager.IMPORTANCE_LOW)
            )
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("MyFit")
            .setContentText("Ready to sync")
            .setSmallIcon(android.R.drawable.ic_menu_upload)
            .build()
    }

    companion object {
        private const val NOTIF_ID = 1

        fun start(context: Context) {
            context.startForegroundService(Intent(context, WearSyncService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, WearSyncService::class.java))
        }
    }
}
