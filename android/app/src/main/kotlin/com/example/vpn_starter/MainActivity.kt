package com.example.vpn_starter

import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val CHANNEL_CORE = "vpn_starter/core"
    }

    private var pendingVpnPermissionResult: MethodChannel.Result? = null

    private val vpnPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val result = pendingVpnPermissionResult
            pendingVpnPermissionResult = null

            if (result == null) {
                Log.w(TAG, "VPN permission result arrived with no pending Flutter callback")
                return@registerForActivityResult
            }

            val granted = VpnService.prepare(this) == null
            Log.d(TAG, "VPN permission activity finished, granted=$granted")
            result.success(granted)
        }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        Log.d(TAG, "configureFlutterEngine called")

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL_CORE)
            .setMethodCallHandler { call, result ->
                Log.d(TAG, "Core channel call received: ${call.method}")

                when (call.method) {
                    "requestVpnPermission" -> handleRequestVpnPermission(result)
                    "startCore" -> handleStartCore(call, result)
                    "stopCore" -> handleStopCore(result)
                    "vpnStatus" -> result.success(AppVpnService.isRunningGlobal)
                    "getCores" -> handleGetCores(result)
                    "installCore" -> {
                        val name = call.argument<String>("name").orEmpty()
                        Log.d(TAG, "installCore called for name=$name (stub)")
                        result.success(null)
                    }
                    "updateCore" -> {
                        val name = call.argument<String>("name").orEmpty()
                        Log.d(TAG, "updateCore called for name=$name (stub)")
                        result.success(null)
                    }
                    else -> {
                        Log.w(TAG, "Method not implemented: ${call.method}")
                        result.notImplemented()
                    }
                }
            }
    }

    private fun handleRequestVpnPermission(result: MethodChannel.Result) {
        if (pendingVpnPermissionResult != null) {
            result.error(
                "VPN_PERMISSION_IN_PROGRESS",
                "A VPN permission request is already in progress",
                null
            )
            return
        }

        val intent = VpnService.prepare(this)
        if (intent == null) {
            Log.d(TAG, "VPN permission already granted")
            result.success(true)
            return
        }

        Log.d(TAG, "Launching VPN permission activity")
        pendingVpnPermissionResult = result
        vpnPermissionLauncher.launch(intent)
    }

    private fun handleStartCore(
        call: MethodChannel.MethodCall,
        result: MethodChannel.Result
    ) {
        val profileName = call.argument<String>("profileName").orEmpty()
        val coreName = call.argument<String>("coreName").orEmpty()

        Log.d(TAG, "startCore called, profileName=$profileName, coreName=$coreName")

        if (profileName.isBlank()) {
            result.error("INVALID_PROFILE", "profileName is empty", null)
            return
        }

        if (coreName.isBlank()) {
            result.error("INVALID_CORE", "coreName is empty", null)
            return
        }

        if (VpnService.prepare(this) != null) {
            result.error("VPN_PERMISSION_REQUIRED", "VPN permission has not been granted", null)
            return
        }

        val serviceIntent = Intent(this, AppVpnService::class.java).apply {
            action = AppVpnService.ACTION_START
            putExtra(AppVpnService.EXTRA_PROFILE_NAME, profileName)
            putExtra(AppVpnService.EXTRA_CORE_NAME, coreName)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start AppVpnService", e)
            result.error("START_CORE_FAILED", e.message, null)
        }
    }

    private fun handleStopCore(result: MethodChannel.Result) {
        Log.d(TAG, "stopCore called")

        val serviceIntent = Intent(this, AppVpnService::class.java).apply {
            action = AppVpnService.ACTION_STOP
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop AppVpnService", e)
            result.error("STOP_CORE_FAILED", e.message, null)
        }
    }

    private fun handleGetCores(result: MethodChannel.Result) {
        val cores = listOf(
            mapOf(
                "name" to "sing-box",
                "version" to null,
                "installed" to false,
                "updateAvailable" to false,
                "downloading" to false
            ),
            mapOf(
                "name" to "xray",
                "version" to null,
                "installed" to false,
                "updateAvailable" to false,
                "downloading" to false
            )
        )
        result.success(cores)
    }
}
