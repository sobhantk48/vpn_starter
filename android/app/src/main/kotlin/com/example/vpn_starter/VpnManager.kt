package com.example.vpn_starter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService

sealed class VpnStartResult {
    data object Started : VpnStartResult()
    data object PermissionRequired : VpnStartResult()
    data class Error(val message: String) : VpnStartResult()
}

class VpnManager(
    private val activity: Activity,
    private val appContext: Context,
    private val logger: LogStreamHandler,
) {
    fun start(profileName: String, coreName: String): VpnStartResult {
        val permissionIntent = VpnService.prepare(activity)
        if (permissionIntent != null) {
            logger.log("[vpn] start blocked: permission required")
            return VpnStartResult.PermissionRequired
        }

        return try {
            val intent = Intent(appContext, AppVpnService::class.java).apply {
                action = AppVpnService.ACTION_START
                putExtra(AppVpnService.EXTRA_PROFILE_NAME, profileName)
                putExtra(AppVpnService.EXTRA_CORE_NAME, coreName)
            }

            appContext.startService(intent)
            logger.log("[vpn] start requested profile=$profileName core=$coreName")
            VpnStartResult.Started
        } catch (e: Exception) {
            logger.log("[vpn] start failed: ${e.message}")
            VpnStartResult.Error(e.message ?: "Unknown start error")
        }
    }

    fun stop() {
        val intent = Intent(appContext, AppVpnService::class.java).apply {
            action = AppVpnService.ACTION_STOP
        }
        appContext.startService(intent)
        logger.log("[vpn] stop requested")
    }
}
