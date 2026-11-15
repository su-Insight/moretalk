package com.example.onepass.utils

object Logger {
    private const val TAG = "OnePass"
    
    fun d(message: String) {
        HistoryLog.d(TAG, message)
    }
    
    fun d(tag: String, message: String) {
        HistoryLog.d(tag, message)
    }
    
    fun e(message: String) {
        HistoryLog.e(TAG, message)
    }
    
    fun e(tag: String, message: String) {
        HistoryLog.e(tag, message)
    }
    
    fun e(message: String, throwable: Throwable) {
        HistoryLog.e(TAG, message, throwable)
    }
    
    fun e(tag: String, message: String, throwable: Throwable) {
        HistoryLog.e(tag, message, throwable)
    }
    
    fun i(message: String) {
        HistoryLog.i(TAG, message)
    }
    
    fun i(tag: String, message: String) {
        HistoryLog.i(tag, message)
    }
    
    fun w(message: String) {
        HistoryLog.w(TAG, message)
    }
    
    fun w(tag: String, message: String) {
        HistoryLog.w(tag, message)
    }
    
    fun v(message: String) {
        HistoryLog.v(TAG, message)
    }
    
    fun v(tag: String, message: String) {
        HistoryLog.v(tag, message)
    }
}
