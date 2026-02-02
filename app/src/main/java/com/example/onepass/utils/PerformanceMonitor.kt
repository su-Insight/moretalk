package com.example.onepass.utils

import android.util.Log

object PerformanceMonitor {
    private const val TAG = "PerfMonitor"
    private val timers = mutableMapOf<String, Long>()

    fun startTimer(name: String) {
        timers[name] = System.currentTimeMillis()
        Log.d(TAG, "[$name] 开始计时")
    }

    fun endTimer(name: String): Long {
        val startTime = timers.remove(name) ?: return -1
        val duration = System.currentTimeMillis() - startTime
        Log.d(TAG, "[$name] 耗时: ${duration}ms")
        return duration
    }

    fun measure(name: String, block: () -> Unit) {
        startTimer(name)
        try {
            block()
        } finally {
            endTimer(name)
        }
    }

    fun clearAll() {
        timers.clear()
    }
}
