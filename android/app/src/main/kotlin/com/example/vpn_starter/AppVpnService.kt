package com.example.vpn_starter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.ParcelFileDescriptor
import android.net.VpnService
import androidx.core.app.NotificationCompat

class AppVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val profileName = intent.getStringExtra(EXTRA_PROFILE_NAME).orEmpty()
                val coreName = intent.getStringExtra(EXTRA_CORE_NAME).orEmpty()
                startVpn(profileName, coreName)
            }

            ACTION_STOP -> {
                stopVpn()
            }
        }
        return START_STICKY
    }

    private fun startVpn(profileName: String, coreName: String) {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("VPN running: $profileName / $coreName"))

        if (vpnInterface != null) {
            return
        }

        val builder = Builder()
            .setSession("vpn_starter")
            .addAddress("10.10.0.2", 24)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("1.1.1.1")

        vpnInterface = builder.establish()
    }

    private fun stopVpn() {
        try {
            vpnInterface?.close()
        } catch (_: Exception) {
        }
        vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        try {
            vpnInterface?.close()
        } catch (_: Exception) {
        }
        vpnInterface = null
        super.onDestroy()
    }

    private fun buildNotification(content: String): Notification {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            1001,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or pendingIntentImmutableFlag(),
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VPN Starter")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "VPN Starter",
            NotificationManager.IMPORTANCE_LOW,
        )
        manager.createNotificationChannel(channel)
    }

    private fun pendingIntentImmutableFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }

    companion object {
        const val ACTION_START = "com.example.vpn_starter.action.START"
        const val ACTION_STOP = "com.example.vpn_starter.action.STOP"

        const val EXTRA_PROFILE_NAME = "extra_profile_name"
        const val EXTRA_CORE_NAME = "extra_core_name"

        private const val CHANNEL_ID = "vpn_starter_service"
        private const val NOTIFICATION_ID = 2001
    }
}
