package com.example.onepass.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * 图片处理工具类
 */
object ImageUtils {
    /**
     * ByteArray转Bitmap
     */
    fun byteArrayToBitmap(byteArray: ByteArray): Bitmap? {
        return try {
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Bitmap转ByteArray
     */
    fun bitmapToByteArray(bitmap: Bitmap, quality: Int = 70): ByteArray {
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream)
        return outputStream.toByteArray()
    }
}
