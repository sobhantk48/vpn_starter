package com.example.vpn_starter

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

class AppVpnService : VpnService() {

    companion object {
        private const val TAG = "AppVpnService"

        const val ACTION_START = "com.example.vpn_starter.action.START"
        const val ACTION_STOP = "com.example.vpn_starter.action.STOP"

        const val EXTRA_PROFILE_NAME = "extra_profile_name"
        const val EXTRA_CORE_NAME = "extra_core_name"

        private const val CHANNEL_ID = "vpn_starter_service"
        private const val NOTIFICATION_ID = 2001

        @Volatile
        var isRunningGlobal: Boolean = false
            private set
    }

    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand action=${intent?.action} startId=$startId")

        when (intent?.action) {
            ACTION_START -> {
                val profileName = intent.getStringExtra(EXTRA_PROFILE_NAME).orEmpty()
                val coreName = intent.getStringExtra(EXTRA_CORE_NAME).orEmpty()
                startVpn(profileName, coreName)
            }

            ACTION_STOP -> {
                stopVpnInternal()
                stopSelf()
            }

            else -> {
                Log.w(TAG, "Unknown or null action: ${intent?.action}")
            }
        }

        return START_STICKY
    }

    private fun startVpn(profileName: String, coreName: String) {
        if (profileName.isBlank()) {
            Log.e(TAG, "Cannot start VPN: profileName is blank")
            stopSelf()
            return
        }

        if (coreName.isBlank()) {
            Log.e(TAG, "Cannot start VPN: coreName is blank")
            stopSelf()
            return
        }

        if (isRunningGlobal && vpnInterface != null) {
            Log.w(TAG, "VPN already running, ignoring duplicate start")
            return
        }

        createNotificationChannel()

        startForeground(
            NOTIFICATION_ID,
            buildNotification("VPN starting: $profileName / $coreName")
        )

        try {
            val builder = Builder()
                .setSession("vpn_starter")
                .setMtu(1500)
                .addAddress("10.10.0.2", 24)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("1.1.1.1")
                .addDnsServer("8.8.8.8")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                builder.setMetered(false)
            }

            val established = builder.establish()
            if (established == null) {
                Log.e(TAG, "builder.establish() returned null")
                stopVpnInternal()
                stopSelf()
                return
            }

            vpnInterface = established
            isRunningGlobal = true

            Log.i(TAG, "VPN established successfully")

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(
                NOTIFICATION_ID,
                buildNotification("VPN connected: $profileName / $coreName")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to establish VPN", e)
            stopVpnInternal()
            stopSelf()
        }
    }

    private fun stopVpnInternal() {
        Log.d(TAG, "Stopping VPN")

        try {
            vpnInterface?.close()
            Log.d(TAG, "VPN interface closed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to close VPN interface", e)
        }

        vpnInterface = null
        isRunningGlobal = false

        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop foreground service", e)
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
}
