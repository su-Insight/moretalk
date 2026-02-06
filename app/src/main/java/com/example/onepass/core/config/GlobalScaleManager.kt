package com.example.onepass.core.config

import android.content.Context

object GlobalScaleManager {
    private const val PREFS_NAME = "OnePassPrefs"
    private const val KEY_SCALE_PERCENTAGE = "scale_percentage"
    private const val DEFAULT_SCALE_PERCENTAGE = 80
    private const val MIN_SCALE_PERCENTAGE = 60
    private const val MAX_SCALE_PERCENTAGE = 100

    /**
     * 获取当前缩放比例百分比
     * @return 缩放比例百分比 (60-100)
     */
    fun getScalePercentage(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_SCALE_PERCENTAGE, DEFAULT_SCALE_PERCENTAGE)
    }

    /**
     * 设置缩放比例百分比
     * @param percentage 缩放比例百分比 (会被限制在60-100之间)
     */
    fun setScalePercentage(context: Context, percentage: Int) {
        val clampedPercentage = percentage.coerceIn(MIN_SCALE_PERCENTAGE, MAX_SCALE_PERCENTAGE)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_SCALE_PERCENTAGE, clampedPercentage).apply()
    }

    /**
     * 获取缩放比例因子
     * @return 缩放比例因子 (0.6-1.0)
     */
    fun getScaleFactor(context: Context): Float {
        val percentage = getScalePercentage(context)
        return percentage / 100.0f
    }

    /**
     * 根据缩放比例计算新值
     * @param originalValue 原始值
     * @return 缩放后的值
     */
    fun getScaledValue(context: Context, originalValue: Float): Float {
        return originalValue * getScaleFactor(context)
    }

    /**
     * 根据缩放比例计算新值（整数）
     * @param originalValue 原始值
     * @return 缩放后的值
     */
    fun getScaledValue(context: Context, originalValue: Int): Int {
        return (originalValue * getScaleFactor(context)).toInt()
    }

    /**
     * 重置缩放比例到默认值
     */
    fun resetToDefault(context: Context) {
        setScalePercentage(context, DEFAULT_SCALE_PERCENTAGE)
    }
}
