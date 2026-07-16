package com.example.vpn_starter

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val methodChannelName = "vpn_starter/core"
    private val logsChannelName = "vpn_starter/logs"

    private val vpnPermissionRequestCode = 1001
    private var pendingPermissionResult: MethodChannel.Result? = null
    private var logsSink: EventChannel.EventSink? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            methodChannelName
        ).setMethodCallHandler { call, result ->
            handleMethodCall(call, result)
        }

        EventChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            logsChannelName
        ).setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                logsSink = events
                logsSink?.success("Native log stream attached")
            }

            override fun onCancel(arguments: Any?) {
                logsSink = null
            }
        })
    }

    private fun handleMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "requestVpnPermission" -> requestVpnPermission(result)
            "startCore" -> {
                logsSink?.success("startCore called")
                result.success(true)
            }
            "stopCore" -> {
                logsSink?.success("stopCore called")
                result.success(true)
            }
            "getInstalledCores" -> {
                result.success(
                    listOf(
                        mapOf(
                            "name" to "sing-box",
                            "installed" to false,
                            "version" to null,
                            "updateAvailable" to false,
                            "downloading" to false
                        )
                    )
                )
            }
            "installCore" -> {
                val name = call.argument<String>("name") ?: "unknown"
                logsSink?.success("installCore called for $name")
                result.success(null)
            }
            "updateCore" -> {
                val name = call.argument<String>("name") ?: "unknown"
                logsSink?.success("updateCore called for $name")
                result.success(null)
            }
            else -> result.notImplemented()
        }
    }

    private fun requestVpnPermission(result: MethodChannel.Result) {
        val intent = VpnService.prepare(this)
        if (intent == null) {
            result.success(true)
            return
        }

        pendingPermissionResult = result
        startActivityForResult(intent, vpnPermissionRequestCode)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == vpnPermissionRequestCode) {
            val granted = resultCode == Activity.RESULT_OK
            pendingPermissionResult?.success(granted)
            pendingPermissionResult = null
            logsSink?.success("VPN permission result: $granted")
        }
    }
}
