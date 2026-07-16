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

    private lateinit var logStreamHandler: LogStreamHandler
    private lateinit var coreManager: CoreManager
    private lateinit var vpnManager: VpnManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logStreamHandler = LogStreamHandler()
        coreManager = CoreManager(applicationContext)
        vpnManager = VpnManager(this, applicationContext, logStreamHandler)
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
        ).setStreamHandler(logStreamHandler)
    }

    private fun handleMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "requestVpnPermission" -> {
                requestVpnPermission(result)
            }

            "getCores" -> {
                result.success(coreManager.getCores())
            }

            "installCore" -> {
                val name = call.argument<String>("name")
                if (name.isNullOrBlank()) {
                    result.error("INVALID_ARGUMENT", "Missing core name", null)
                    return
                }
                coreManager.installCore(name, logStreamHandler)
                result.success(null)
            }

            "updateCore" -> {
                val name = call.argument<String>("name")
                if (name.isNullOrBlank()) {
                    result.error("INVALID_ARGUMENT", "Missing core name", null)
                    return
                }
                coreManager.updateCore(name, logStreamHandler)
                result.success(null)
            }

            "startCore" -> {
                val profileName = call.argument<String>("profileName").orEmpty()
                val coreName = call.argument<String>("coreName").orEmpty()

                val startResult = vpnManager.start(
                    profileName = profileName.ifBlank { "Default Profile" },
                    coreName = coreName.ifBlank { "sing-box" },
                )

                when (startResult) {
                    is VpnStartResult.Started -> result.success(true)
                    is VpnStartResult.PermissionRequired -> result.success(false)
                    is VpnStartResult.Error -> {
                        result.error("VPN_START_ERROR", startResult.message, null)
                    }
                }
            }

            "stopCore" -> {
                vpnManager.stop()
                result.success(true)
            }

            else -> result.notImplemented()
        }
    }

    private fun requestVpnPermission(result: MethodChannel.Result) {
        val intent = VpnService.prepare(this)
       if (intent == null) {
            logStreamHandler.log("[vpn] permission already granted")
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
            logStreamHandler.log("[vpn] permission result: $granted")
            pendingPermissionResult?.success(granted)
            pendingPermissionResult = null
        }
    }
}
