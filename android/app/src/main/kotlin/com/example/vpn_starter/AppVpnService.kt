package com.example.vpn_starter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import libv2ray.Libv2ray

class AppVpnService : VpnService() {

    companion object {
        private const val CHANNEL_ID = "vpn_service_channel"
        private const val NOTIFICATION_ID = 1001
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    @Volatile
    private var running = false
    private var coreThread: Thread? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("onStartCommand called")

        createNotificationChannelIfNeeded()
        startForeground(NOTIFICATION_ID, buildNotification("VPN is starting..."))

        if (running) {
            log("Service already running, ignoring duplicate start")
            return START_STICKY
        }
        running = true

        try {
            vpnInterface = establishVpnInterface()
            val fd = vpnInterface?.fd ?: throw IllegalStateException("vpnInterface fd is null")
            log("VPN interface established, fd=$fd")

            coreThread = Thread({
                try {
                    val configPath = prepareConfigFile()
                    log("Config prepared at: $configPath")

                    startCoreWithReflection(configPath, fd)

                    log("Core start invoked successfully")
                    updateNotification("VPN connected")
                } catch (t: Throwable) {
                    log("Core thread error: ${t.stackTraceToString()}")
                    updateNotification("VPN failed")
                    stopSelf()
                }
            }, "vpn-core-thread").also { it.start() }

        } catch (t: Throwable) {
            log("onStartCommand fatal error: ${t.stackTraceToString()}")
            updateNotification("VPN failed")
            stopSelf()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        log("onDestroy called")
        running = false

        try {
            val stopMethod = Libv2ray::class.java.methods.firstOrNull {
                it.name.equals("stop", ignoreCase = true) && it.parameterCount == 0
            }
            stopMethod?.invoke(null)
            log("Libv2ray.stop() invoked (if existed)")
        } catch (t: Throwable) {
            log("stop core failed (ignored): ${t.message}")
        }

        try {
            coreThread?.interrupt()
            coreThread = null
        } catch (_: Throwable) {}

        try {
            vpnInterface?.close()
            vpnInterface = null
            log("vpnInterface closed")
        } catch (t: Throwable) {
            log("close vpnInterface failed: ${t.message}")
        }

        super.onDestroy()
    }

    override fun onRevoke() {
        log("onRevoke called by system")
        stopSelf()
        super.onRevoke()
    }

    private fun establishVpnInterface(): ParcelFileDescriptor {
        val builder = Builder()
            .setSession("VPN Starter")
            .addAddress("10.10.0.2", 32)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("1.1.1.1")
            .addDnsServer("8.8.8.8")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }

        return builder.establish()
            ?: throw IllegalStateException("Builder.establish() returned null")
    }

    private fun prepareConfigFile(): String {
        // فعلاً مینیمال؛ بعداً با کانفیگ واقعی sing-box/v2ray جایگزین می‌کنیم
        val minimalConfig = """
            {
              "log": { "level": "debug" }
            }
        """.trimIndent()

        val file = File(filesDir, "config.json")
        file.writeText(minimalConfig)
        return file.absolutePath
    }

    private fun startCoreWithReflection(configPath: String, fd: Int) {
        val clazz = Libv2ray::class.java
        val methods = clazz.methods.sortedBy { it.name.lowercase(Locale.US) }

        log("Libv2ray methods count=${methods.size}")
        methods.forEach { m ->
            log("method: ${m.name}(${m.parameterTypes.joinToString { p -> p.simpleName }})")
        }

        val candidates = methods.filter {
            val n = it.name.lowercase(Locale.US)
            n.contains("start") || n.contains("run") || n.contains("init")
        }

        for (m in candidates) {
            try {
                val p = m.parameterTypes
                val args: Array<Any?> = when {
                    p.size == 2 &&
                        p[0] == String::class.java &&
                        (p[1] == Int::class.javaPrimitiveType || p[1] == Integer::class.java) ->
                        arrayOf(configPath, fd)

                    p.size == 1 && p[0] == String::class.java ->
                        arrayOf(configPath)

                    p.isEmpty() ->
                        emptyArray()

                    else -> continue
                }

                log("Trying method: ${m.name} with (${p.joinToString { it.simpleName }})")
                m.invoke(null, *args)
                log("Method success: ${m.name}")
                return
            } catch (t: Throwable) {
                log("Method failed: ${m.name} -> ${t.message}")
            }
        }

        throw IllegalStateException("No suitable start/run/init method found in Libv2ray")
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VPN Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "VPN foreground service"
            }
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VPN Starter")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_vpn_ic)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification(text))
    }

    private fun log(message: String) {
        val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
        val line = "[$ts] $message\n"

        // 1) لاگ داخل حافظه خصوصی اپ
        try {
            File(filesDir, "vpn_debug.txt").appendText(line)
        } catch (_: Throwable) {}

        // 2) لاگ داخل حافظه عمومی: Documents/vpn_starter_logs/vpn_debug.txt
        // روی اندروید 10+ بهتره از SAF استفاده بشه، ولی برای تست سریع این روش رو هم می‌زنیم.
        try {
            val docs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val dir = File(docs, "vpn_starter_logs")
            if (!dir.exists()) dir.mkdirs()
            File(dir, "vpn_debug.txt").appendText(line)
        } catch (_: Throwable) {}
    }
}
