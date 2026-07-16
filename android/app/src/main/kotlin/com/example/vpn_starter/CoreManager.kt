package com.example.vpn_starter

import android.content.Context
import java.io.File

class CoreManager(
    private val context: Context,
) {
    private val coresDir: File by lazy {
        File(context.filesDir, "cores").apply { mkdirs() }
    }

    fun getCores(): List<Map<String, Any?>> {
        val singBoxInstalled = File(coresDir, "sing-box").exists()
        val xrayInstalled = File(coresDir, "xray").exists()

        return listOf(
            mapOf(
                "name" to "sing-box",
                "installed" to singBoxInstalled,
                "version" to if (singBoxInstalled) "mock-1.0.0" else null,
            ),
            mapOf(
                "name" to "xray",
                "installed" to xrayInstalled,
                "version" to if (xrayInstalled) "mock-1.0.0" else null,
            ),
        )
    }

    fun installCore(name: String, logger: LogStreamHandler) {
        val file = File(coresDir, name)
        if (!file.exists()) {
            file.writeText("mock core binary for $name")
        }
        logger.log("[core] installed: $name")
    }

    fun updateCore(name: String, logger: LogStreamHandler) {
        val file = File(coresDir, name)
        if (!file.exists()) {
            file.writeText("mock core binary for $name")
            logger.log("[core] installed during update: $name")
        } else {
            file.writeText("mock core binary for $name updated at ${System.currentTimeMillis()}")
            logger.log("[core] updated: $name")
        }
    }
}
