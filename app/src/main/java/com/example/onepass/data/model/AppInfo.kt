package com.example.onepass.data.model

/**
 * 应用信息数据模型
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val iconBytes: ByteArray? = null,
    val isEnabled: Boolean = true,
    val order: Int = 0,
    val installTime: Long = 0,
    val lastUpdateTime: Long = 0
) {
    // Compatibility: default to launchable for all apps unless overridden in future data sources
    val canLaunch: Boolean
        get() = true

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AppInfo
        return packageName == other.packageName
    }

    override fun hashCode(): Int {
        return packageName.hashCode()
    }
}
