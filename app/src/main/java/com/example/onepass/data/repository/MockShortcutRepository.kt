package com.example.onepass.data.repository

import com.example.onepass.data.model.AppInfo
import com.example.onepass.data.source.StaticAppsDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

data class UserProfile(val userId: String, val isVip: Boolean)

class MockShortcutRepository(private val dataSource: StaticAppsDataSource) {
    private val userShortcuts = mutableMapOf<String, MutableSet<String>>()

    suspend fun getAvailableApps(user: UserProfile): List<AppInfo> {
        return dataSource.fetchAvailableApps(user.userId).filter { it.canLaunch }
    }

    suspend fun getHomeShortcuts(user: UserProfile): List<AppInfo> {
        val all = getAvailableApps(user)
        val enabled = userShortcuts[user.userId] ?: mutableSetOf()
        return all.filter { enabled.contains(it.packageName) }
    }

    suspend fun setHomeShortcutEnabled(user: UserProfile, packageName: String, enabled: Boolean): Boolean {
        val maxDisplay = if (user.isVip) 10 else 6
        val set = (userShortcuts[user.userId] ?: mutableSetOf())

        if (enabled) {
            if (!set.contains(packageName) && set.size >= maxDisplay) return false
            set.add(packageName)
        } else {
            set.remove(packageName)
        }
        userShortcuts[user.userId] = set
        return true
    }
}
