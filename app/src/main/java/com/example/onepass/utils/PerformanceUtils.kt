package com.example.onepass.utils

import android.graphics.Bitmap

/**
 * 性能优化工具类
 */
object PerformanceUtils {
    /**
     * 压缩Bitmap
     */
    fun compressBitmap(bitmap: Bitmap, quality: Int = 70): ByteArray {
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream)
        return outputStream.toByteArray()
    }
}
