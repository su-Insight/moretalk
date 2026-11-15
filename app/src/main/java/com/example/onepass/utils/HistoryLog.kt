package com.example.onepass.utils

import com.example.onepass.OnePassApplication
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class LogHistoryEntry(
    val timestamp: Long,
    val level: String,
    val tag: String,
    val message: String
)

object HistoryLog {
    private const val PREFS_NAME = "log_history_prefs"
    private const val KEY_LAST_CLEANUP_DAY = "last_cleanup_day"
    private const val FILE_NAME = "log_history.json"
    private const val MAX_LOG_COUNT = 2000

    private val gson = Gson()
    private val listType = object : TypeToken<List<LogHistoryEntry>>() {}.type
    private val lock = Any()

    fun d(tag: String, message: String): Int {
        val result = android.util.Log.d(tag, message)
        append("D", tag, message)
        return result
    }

    fun i(tag: String, message: String): Int {
        val result = android.util.Log.i(tag, message)
        append("I", tag, message)
        return result
    }

    fun w(tag: String, message: String): Int {
        val result = android.util.Log.w(tag, message)
        append("W", tag, message)
        return result
    }

    fun v(tag: String, message: String): Int {
        val result = android.util.Log.v(tag, message)
        append("V", tag, message)
        return result
    }

    fun e(tag: String, message: String): Int {
        val result = android.util.Log.e(tag, message)
        append("E", tag, message)
        return result
    }

    fun e(tag: String, message: String, throwable: Throwable): Int {
        val result = android.util.Log.e(tag, message, throwable)
        append("E", tag, "$message\n${android.util.Log.getStackTraceString(throwable)}")
        return result
    }

    fun getLogs(): List<LogHistoryEntry> {
        val context = OnePassApplication.app()
        cleanupIfNeeded()
        synchronized(lock) {
            return readEntries(getLogFile(context))
                .sortedByDescending { it.timestamp }
        }
    }

    fun cleanupIfNeeded() {
        val context = OnePassApplication.app()
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val todayKey = dayKey(System.currentTimeMillis())
        if (prefs.getString(KEY_LAST_CLEANUP_DAY, null) == todayKey) {
            return
        }

        synchronized(lock) {
            val file = getLogFile(context)
            val startOfToday = startOfTodayMillis()
            val filtered = readEntries(file).filter { it.timestamp >= startOfToday }
            writeEntries(file, filtered)
        }

        prefs.edit().putString(KEY_LAST_CLEANUP_DAY, todayKey).apply()
    }

    private fun append(level: String, tag: String, message: String) {
        runCatching {
            cleanupIfNeeded()
            val context = OnePassApplication.app()
            synchronized(lock) {
                val file = getLogFile(context)
                val entries = readEntries(file).toMutableList()
                entries.add(
                    LogHistoryEntry(
                        timestamp = System.currentTimeMillis(),
                        level = level,
                        tag = tag,
                        message = message
                    )
                )
                val trimmed = if (entries.size > MAX_LOG_COUNT) {
                    entries.takeLast(MAX_LOG_COUNT)
                } else {
                    entries
                }
                writeEntries(file, trimmed)
            }
        }
    }

    private fun getLogFile(context: android.content.Context): File {
        return File(context.filesDir, FILE_NAME)
    }

    private fun readEntries(file: File): List<LogHistoryEntry> {
        if (!file.exists()) {
            return emptyList()
        }
        val content = file.readText(Charsets.UTF_8)
        if (content.isBlank()) {
            return emptyList()
        }
        return runCatching {
            gson.fromJson<List<LogHistoryEntry>>(content, listType) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    private fun writeEntries(file: File, entries: List<LogHistoryEntry>) {
        file.writeText(gson.toJson(entries), Charsets.UTF_8)
    }

    private fun dayKey(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
    }

    private fun startOfTodayMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
