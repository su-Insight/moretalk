package com.example.onepass

import android.app.Application

class OnePassApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private lateinit var instance: OnePassApplication

        fun app(): OnePassApplication = instance
    }
}
