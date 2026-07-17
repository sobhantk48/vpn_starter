package com.example.vpn_starter.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.vpn_starter.MainActivity
import com.example.vpn_starter.R

class MyVpnService : VpnService() {

    companion object {
        private const val TAG = "MyVpnService"

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
        Log.d(TAG, "onStartCommand called, action=${intent?.action}, startId=$startId")

        when (intent?.action) {
            ACTION_START -> {
                val configJson = intent.getStringExtra(EXTRA_CONFIG_JSON)
                Log.d(TAG, "ACTION_START received, config length=${configJson?.length ?: 0}")

                if (configJson.isNullOrBlank()) {
                    Log.e(TAG, "Config is null or blank, stopping service")
                    stopSelf()
                    return START_NOT_STICKY
                }

                runCatching {
                    startVpn(configJson)
                }.onFailure { e ->
                    Log.e(TAG, "startVpn failed", e)
                    stopVpnInternal()
                    stopSelf()
                }
            }

            ACTION_STOP -> {
                Log.d(TAG, "ACTION_STOP received")
                stopVpnInternal()
                stopSelf()
            }

            else -> {
                Log.w(TAG, "Unknown or null action received: ${intent?.action}")
            }
        }

        return START_STICKY
    }

    private fun startVpn(configJson: String) {
        if (isRunningGlobal) {
            Log.w(TAG, "VPN is already running, ignoring start request")
            return
        }

        Log.d(TAG, "Creating notification channel")
        createNotificationChannel()

        Log.d(TAG, "Starting foreground notification")
        startForeground(NOTIF_ID, buildNotification("VPN is starting..."))

        Log.d(TAG, "Establishing TUN interface")
        val tun = Builder()
            .setSession("V2ray STK")
            .setMtu(1500)
            .addAddress("10.10.0.2", 30)
            .addDnsServer("1.1.1.1")
            .addDnsServer("8.8.8.8")
            .addRoute("0.0.0.0", 0)
            .establish() ?: throw IllegalStateException("Failed to establish TUN")

        tunInterface = tun
        val tunFd = tun.fd
        Log.d(TAG, "TUN established successfully, fd=$tunFd")

        Log.d(TAG, "Initializing bridge")
        bridge.init(filesDir.absolutePath, cacheDir.absolutePath)

        Log.d(TAG, "Starting bridge with config")
        bridge.start(configJson, tunFd)

        isRunningGlobal = true
        Log.i(TAG, "VPN started successfully")

        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIF_ID, buildNotification("VPN connected"))
    }

    private fun stopVpnInternal() {
        Log.d(TAG, "Stopping VPN")

        runCatching {
            bridge.stop()
            Log.d(TAG, "Bridge stopped")
        }.onFailure {
            Log.e(TAG, "Failed to stop bridge", it)
        }

        runCatching {
            tunInterface?.close()
            Log.d(TAG, "TUN interface closed")
        }.onFailure {
            Log.e(TAG, "Failed to close TUN interface", it)
        }

        tunInterface = null
        isRunningGlobal = false

        runCatching {
            stopForeground(STOP_FOREGROUND_REMOVE)
            Log.d(TAG, "Foreground service stopped")
        }.onFailure {
            Log.e(TAG, "Failed to stop foreground", it)
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy called")
        stopVpnInternal()
        super.onDestroy()
    }

    override fun onRevoke() {
        Log.w(TAG, "VPN permission revoked by system")
        stopVpnInternal()
        stopSelf()
        super.onRevoke()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIF_CHANNEL_ID,
                "VPN Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setContentTitle("V2ray STK")
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}
