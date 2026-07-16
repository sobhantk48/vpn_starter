package com.example.vpn_starter.vpn

import android.util.Log
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray
import libv2ray.ProcessFinder

class Libv2rayBridge {

    companion object {
        private const val TAG = "Libv2rayBridge"
    }

    @Volatile
    private var controller: CoreController? = null

    private val callbackHandler = object : CoreCallbackHandler {
        override fun onEmitStatus(code: Long, message: String?): Long {
            Log.d(TAG, "onEmitStatus code=$code message=$message")
            return 0L
        }

        override fun startup(): Long {
            Log.d(TAG, "startup() called")
            return 0L
        }

        override fun shutdown(): Long {
            Log.d(TAG, "shutdown() called")
            return 0L
        }
    }

    private val processFinder = object : ProcessFinder {
        override fun findProcessByConnection(
            network: String?,
            source: String?,
            sourcePort: Long,
            target: String?,
            targetPort: Long
        ): Long {
            return 0L
        }
    }

    @Synchronized
    fun init(filesDir: String, cacheDir: String) {
        Libv2ray.touch()
        Libv2ray.initCoreEnv(filesDir, cacheDir)

        if (controller == null) {
            controller = Libv2ray.newCoreController(callbackHandler)
            controller?.registerProcessFinder(processFinder)
        }
    }

    @Synchronized
    fun start(configJson: String, tunFd: Int) {
        val c = controller ?: error("Bridge not initialized")
        c.startLoop(configJson, tunFd)
    }

    @Synchronized
    fun stop() {
        controller?.stopLoop()
    }

    fun isRunning(): Boolean {
        return controller?.isRunning ?: false
    }

    fun version(): String {
        return Libv2ray.checkVersionX()
    }

    fun measureDelay(url: String): Long {
        val c = controller ?: error("Bridge not initialized")
        return c.measureDelay(url)
    }

    fun queryAllOutboundTrafficStats(): String {
        return controller?.queryAllOutboundTrafficStats() ?: "{}"
    }

    fun queryStats(tag: String, direction: String): Long {
        return controller?.queryStats(tag, direction) ?: 0L
    }
}
