package com.example.vpn_starter

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val channelName = "vpn_starter/vpn"
    private val vpnRequestCode = 9001
    private var pendingResult: MethodChannel.Result? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channelName)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "startVpn" -> {
                        tryStartVpn(result)
                    }
                    "stopVpn" -> {
                        stopVpn()
                        result.success(true)
                    }
                    else -> result.notImplemented()
                }
            }
    }

    private fun tryStartVpn(result: MethodChannel.Result) {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            pendingResult = result
            startActivityForResult(intent, vpnRequestCode)
        } else {
            startVpnService()
            result.success(true)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == vpnRequestCode) {
            val r = pendingResult
            pendingResult = null

            if (resultCode == Activity.RESULT_OK) {
                startVpnService()
                r?.success(true)
            } else {
                r?.error("VPN_PERMISSION_DENIED", "User denied VPN permission", null)
            }
        }
    }

    private fun startVpnService() {
        val intent = Intent(this, AppVpnService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopVpn() {
        val intent = Intent(this, AppVpnService::class.java)
        stopService(intent)
    }
}
