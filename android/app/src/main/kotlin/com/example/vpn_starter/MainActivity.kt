package com.example.vpn_starter

import android.content.Intent
import android.net.VpnService
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import com.example.vpn_starter.vpn.MyVpnService

class MainActivity : FlutterActivity() {

    private val channelName = "com.example.vpn_starter/vpn"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channelName)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "prepareVpn" -> {
                        val intent = VpnService.prepare(this)
                        if (intent != null) {
                            startActivity(intent)
                            result.success(false)
                        } else {
                            result.success(true)
                        }
                    }

                    "startVpn" -> {
                        val configJson = call.argument<String>("config") ?: ""
                        if (configJson.isBlank()) {
                            result.error("INVALID_CONFIG", "config is empty", null)
                            return@setMethodCallHandler
                        }

                        val serviceIntent = Intent(this, MyVpnService::class.java).apply {
                            action = MyVpnService.ACTION_START
                            putExtra(MyVpnService.EXTRA_CONFIG_JSON, configJson)
                        }
                        startService(serviceIntent)
                        result.success(true)
                    }

                    "stopVpn" -> {
                        val serviceIntent = Intent(this, MyVpnService::class.java).apply {
                            action = MyVpnService.ACTION_STOP
                        }
                        startService(serviceIntent)
                        result.success(true)
                    }

                    "vpnStatus" -> {
                        result.success(MyVpnService.isRunningGlobal)
                    }

                    else -> result.notImplemented()
                }
            }
    }
}
