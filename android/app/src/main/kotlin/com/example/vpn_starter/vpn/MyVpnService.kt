package com.example.vpn_starter.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.example.vpn_starter.MainActivity
import com.example.vpn_starter.R

class MyVpnService : VpnService() {

    companion object {
        const val ACTION_START = "com.example.vpn_starter.vpn.START"
        const val ACTION_STOP = "com.example.vpn_starter.vpn.STOP"
        const val EXTRA_CONFIG_JSON = "extra_config_json"

        private const val NOTIF_CHANNEL_ID = "vpn_channel"
        private const val NOTIF_ID = 1001

        @Volatile
        var isRunningGlobal: Boolean = false
            private set
    }

    private val bridge = Libv2rayBridge()
    private var tunInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val configJson = intent.getStringExtra(EXTRA_CONFIG_JSON)
                if (configJson.isNullOrBlank()) {
                    stopSelf()
                    return START_NOT_STICKY
                }
                startVpn(configJson)
            }

            ACTION_STOP -> {
                stopVpn()
            }
        }
        return START_STICKY
    }

    private fun startVpn(configJson: String) {
        if (isRunningGlobal) return

        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("VPN is starting..."))

        val builder = Builder()
            .setSession("V2ray STK")
            .setMtu(1500)
            .addAddress("10.10.0.2", 30)
            .addDnsServer("1.1.1.1")
            .addDnsServer("8.8.8.8")
            .addRoute("0.0.0.0", 0)

        tunInterface = builder.establish()
        val pfd = tunInterface ?: throw IllegalStateException("Failed to establish VPN interface")
        val tunFd = pfd.fd

        bridge.init(filesDir.absolutePath, cacheDir.absolutePath)
        bridge.start(configJson, tunFd)

        isRunningGlobal = true

        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIF_ID, buildNotification("VPN is connected"))
    }

    private fun stopVpn() {
        try {
            bridge.stop()
        } catch (_: Throwable) {
        }

        try {
            tunInterface?.close()
        } catch (_: Throwable) {
        } finally {
            tunInterface = null
        }

        isRunningGlobal = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIF_CHANNEL_ID,
                "VPN Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(content: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setContentTitle("V2ray STK")
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .build()
    }
}
