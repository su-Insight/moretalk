package com.example.onepass

import android.util.Log

object Logger {
    private const val TAG = "OnePass"
    
    fun d(message: String) {
        Log.d(TAG, message)
    }
    
    fun d(tag: String, message: String) {
        Log.d(tag, message)
    }
    
    fun e(message: String) {
        Log.e(TAG, message)
    }
    
    fun e(tag: String, message: String) {
        Log.e(tag, message)
    }
    
    fun e(message: String, throwable: Throwable) {
        Log.e(TAG, message, throwable)
    }
    
    fun e(tag: String, message: String, throwable: Throwable) {
        Log.e(tag, message, throwable)
    }
    
    fun i(message: String) {
    }
    
    fun i(tag: String, message: String) {
    }
    
    fun w(message: String) {
    }
    
    fun w(tag: String, message: String) {
    }
    
    fun v(message: String) {
    }
    
    fun v(tag: String, message: String) {
    }
}