package com.example.vpn_starter

import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.util.Log
import com.example.vpn_starter.vpn.MyVpnService
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val channelName = "com.example.vpn_starter/vpn"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        Log.d(TAG, "configureFlutterEngine called")

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channelName)
            .setMethodCallHandler { call, result ->
                Log.d(TAG, "MethodChannel call received: ${call.method}")

                when (call.method) {
                    "prepareVpn" -> {
                        val intent = VpnService.prepare(this)
                        if (intent != null) {
                            Log.d(TAG, "VPN permission required, launching prepare intent")
                            startActivity(intent)
                            result.success(false)
                        } else {
                            Log.d(TAG, "VPN permission already granted")
                            result.success(true)
                        }
                    }

                    "startVpn" -> {
                        val config = call.argument<String>("config").orEmpty()
                        Log.d(TAG, "startVpn called, config length=${config.length}")

                        if (config.isBlank()) {
                            Log.e(TAG, "Config is empty")
                            result.error("INVALID_CONFIG", "config is empty", null)
                            return@setMethodCallHandler
                        }

                        val serviceIntent = Intent(this, MyVpnService::class.java).apply {
                            action = MyVpnService.ACTION_START
                            putExtra(MyVpnService.EXTRA_CONFIG_JSON, config)
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Log.d(TAG, "Starting foreground service")
                            startForegroundService(serviceIntent)
                        } else {
                            Log.d(TAG, "Starting normal service")
                            startService(serviceIntent)
                        }

                        result.success(true)
                    }

                    "stopVpn" -> {
                        Log.d(TAG, "stopVpn called")

                        val serviceIntent = Intent(this, MyVpnService::class.java).apply {
                            action = MyVpnService.ACTION_STOP
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(serviceIntent)
                        } else {
                            startService(serviceIntent)
                        }

                        result.success(true)
                    }

                    "vpnStatus" -> {
                        Log.d(TAG, "vpnStatus called -> ${MyVpnService.isRunningGlobal}")
                        result.success(MyVpnService.isRunningGlobal)
                    }

                    else -> {
                        Log.w(TAG, "Method not implemented: ${call.method}")
                        result.notImplemented()
                    }
                }
            }
    }
}
