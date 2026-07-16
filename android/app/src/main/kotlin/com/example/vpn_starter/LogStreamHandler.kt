package com.example.vpn_starter

import android.os.Handler
import android.os.Looper
import io.flutter.plugin.common.EventChannel

class LogStreamHandler : EventChannel.StreamHandler {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var sink: EventChannel.EventSink? = null
    private val backlog = ArrayDeque<String>()

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        sink = events
        while (backlog.isNotEmpty()) {
            val line = backlog.removeFirst()
            emit(line)
        }
    }

    override fun onCancel(arguments: Any?) {
        sink = null
    }

    fun log(message: String) {
        val line = "${System.currentTimeMillis()} $message"
        if (sink == null) {
            if (backlog.size >= 200) {
                backlog.removeFirst()
            }
            backlog.addLast(line)
            return
        }
        emit(line)
    }

    private fun emit(message: String) {
        mainHandler.post {
            sink?.success(message)
        }
    }
}
