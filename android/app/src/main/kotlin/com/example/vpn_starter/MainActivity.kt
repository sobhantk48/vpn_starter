package com.example.vpn_starter

import android.content.Intent
import android.net.VpnService
import com.example.vpn_starter.vpn.MyVpnService
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

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
                        val config = call.argument<String>("config").orEmpty()
                        if (config.isBlank()) {
                            result.error("INVALID_CONFIG", "config is empty", null)
                            return@setMethodCallHandler
                        }

                        startService(
                            Intent(this, MyVpnService::class.java).apply {
                                action = MyVpnService.ACTION_START
                                putExtra(MyVpnService.EXTRA_CONFIG_JSON, config)
                            }
                        )
                        result.success(true)
                    }

                    "stopVpn" -> {
                        startService(
                            Intent(this, MyVpnService::class.java).apply {
                                action = MyVpnService.ACTION_STOP
                            }
                        )
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
