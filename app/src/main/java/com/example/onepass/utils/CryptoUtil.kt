package com.example.onepass.utils

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-GCM 加密工具类
 * 用于加密敏感数据（如手机号）
 */
object CryptoUtil {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    // 使用固定的密钥（实际项目中应使用 Android Keystore 存储）
    // 这里使用一个预共享密钥，对抗普通攻击者
    private val SECRET_KEY by lazy {
        val keyBytes = getOrCreateKey()
        SecretKeySpec(keyBytes, "AES")
    }

    /**
     * 获取或创建加密密钥
     * 实际项目中应使用 Android Keystore
     */
    private fun getOrCreateKey(): ByteArray {
        // 32字节密钥 (256位)
        // 注意：生产环境应使用 Android Keystore
        return byteArrayOf(
            0x4A.toByte(), 0x3B.toByte(), 0x2C.toByte(), 0x1D.toByte(),
            0x5C.toByte(), 0x6D.toByte(), 0x7E.toByte(), 0x8F.toByte(),
            0x1A.toByte(), 0x2B.toByte(), 0x3C.toByte(), 0x4D.toByte(),
            0x5E.toByte(), 0x6F.toByte(), 0x7A.toByte(), 0x8B.toByte(),
            0x9C.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xCF.toByte(),
            0xD1.toByte(), 0xE2.toByte(), 0xF3.toByte(), 0xA4.toByte(),
            0xB5.toByte(), 0xC6.toByte(), 0xD7.toByte(), 0xE8.toByte(),
            0xF9.toByte(), 0xA0.toByte(), 0xB1.toByte(), 0xC2.toByte()
        )
    }

    /**
     * 加密字符串
     * @param plainText 明文
     * @return Base64编码的密文 (IV + 密文)
     */
    fun encrypt(plainText: String?): String? {
        if (plainText.isNullOrEmpty()) return null

        return try {
            // 生成随机IV
            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)

            // 创建GCM参数
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)

            // 初始化加密器
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY, gcmSpec)

            // 加密
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            // 组合 IV + 密文 并 Base64 编码
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            Logger.e("CryptoUtil", "加密失败", e)
            null
        }
    }

    /**
     * 解密字符串
     * @param encryptedText Base64编码的密文
     * @return 明文
     */
    fun decrypt(encryptedText: String?): String? {
        if (encryptedText.isNullOrEmpty()) return null

        return try {
            // Base64 解码
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)

            // 分离 IV 和 密文
            val iv = ByteArray(GCM_IV_LENGTH)
            val encryptedBytes = ByteArray(combined.size - GCM_IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, iv.size)
            System.arraycopy(combined, iv.size, encryptedBytes, 0, encryptedBytes.size)

            // 创建GCM参数
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)

            // 初始化解密器
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY, gcmSpec)

            // 解密
            String(cipher.doFinal(encryptedBytes), Charsets.UTF_8)
        } catch (e: Exception) {
            Logger.e("CryptoUtil", "解密失败", e)
            null
        }
    }
}
