package com.example.onepass.data.source

import com.example.onepass.data.model.AppInfo

interface StaticAppsDataSource {
    suspend fun fetchAvailableApps(userId: String): List<AppInfo>
}
