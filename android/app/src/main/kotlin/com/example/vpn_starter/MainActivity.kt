package com.example.vpn_starter

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import androidx.activity.result.contract.ActivityResultContracts
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private lateinit var coreManager: CoreManager
    private lateinit var vpnManager: VpnManager
    private lateinit var logStreamHandler: LogStreamHandler

    private var pendingPermissionResult: MethodChannel.Result? = null

    private val vpnPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val methodResult = pendingPermissionResult
            pendingPermissionResult = null

            val granted = result.resultCode == Activity.RESULT_OK
            logStreamHandler.log("[vpn] permission result granted=$granted")

            methodResult?.success(granted)
        }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        coreManager = CoreManager(applicationContext)
        logStreamHandler = LogStreamHandler()
        vpnManager = VpnManager(
            activity = this,
            appContext = applicationContext,
            logger = logStreamHandler,
        )

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CORE_CHANNEL,
        ).setMethodCallHandler { call, result ->
            handleCoreCall(call, result)
        }

        EventChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            LOGS_CHANNEL,
        ).setStreamHandler(logStreamHandler)

        logStreamHandler.log("[boot] native bridge ready")
    }

    private fun handleCoreCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getCores" -> {
                result.success(coreManager.getCores())
            }

            "installCore" -> {
                val name = call.argument<String>("name")
                if (name.isNullOrBlank()) {
                    result.error("invalid_args", "Core name is required", null)
                    return
                }

                try {
                    coreManager.installCore(name, logStreamHandler)
                    result.success(null)
                } catch (e: Exception) {
                    result.error("install_failed", e.message, null)
                }
            }

            "updateCore" -> {
                val name = call.argument<String>("name")
                if (name.isNullOrBlank()) {
                    result.error("invalid_args", "Core name is required", null)
                    return
                }

                try {
                    coreManager.updateCore(name, logStreamHandler)
                    result.success(null)
                } catch (e: Exception) {
                    result.error("update_failed", e.message, null)
                }
            }

            "requestVpnPermission" -> {
                requestVpnPermission(result)
            }

            "startCore" -> {
                val profileName = call.argument<String>("profileName")
                val coreName = call.argument<String>("coreName")

                if (profileName.isNullOrBlank() || coreName.isNullOrBlank()) {
                    result.error("invalid_args", "profileName and coreName are required", null)
                    return
                }

                when (val startResult = vpnManager.start(profileName, coreName)) {
                    is VpnStartResult.Started -> result.success(true)
                    is VpnStartResult.PermissionRequired -> {
                        result.error(
                            "vpn_permission_required",
                            "VPN permission is required before start",
                            null,
                        )
                    }

                    is VpnStartResult.Error -> {
                        result.error("vpn_start_failed", startResult.message, null)
                    }
                }
            }

            "stopCore" -> {
                try {
                    vpnManager.stop()
                    result.success(true)
                } catch (e: Exception) {
                    result.error("vpn_stop_failed", e.message, null)
                }
            }

            else -> result.notImplemented()
        }
    }

    private fun requestVpnPermission(result: MethodChannel.Result) {
        if (pendingPermissionResult != null) {
            result.error("busy", "Another VPN permission request is already active", null)
            return
        }

        val prepareIntent: Intent? = VpnService.prepare(this)
        if (prepareIntent == null) {
            logStreamHandler.log("[vpn] permission already granted")
            result.success(true)
            return
        }

        logStreamHandler.log("[vpn] opening permission dialog")
        pendingPermissionResult = result
        vpnPermissionLauncher.launch(prepareIntent)
    }

    companion object {
        private const val CORE_CHANNEL = "vpn_starter/core"
        private const val LOGS_CHANNEL = "vpn_starter/logs"
    }
}
