package com.example.onepass

import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityNodeInfo

object DelayHelper {
    private const val MIN_DELAY = 200L
    private const val MAX_DELAY = 1000L
    private const val DEFAULT_DELAY = 300L
    
    /**
     * 自适应延迟：根据设备性能动态调整延迟时间
     * @param baseDelay 基础延迟时间
     * @return 调整后的延迟时间
     */
    fun getAdaptiveDelay(baseDelay: Long = DEFAULT_DELAY): Long {
        // 根据系统可用内存动态调整延迟
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        val totalMemory = runtime.totalMemory()
        val availableMemory = maxMemory - totalMemory
        val memoryRatio = availableMemory.toFloat() / maxMemory
        
        // 内存充足时使用较短延迟，内存不足时使用较长延迟
        val memoryFactor = when {
            memoryRatio > 0.5f -> 0.8f  // 内存充足，减少延迟
            memoryRatio > 0.3f -> 1.0f  // 内存一般，使用默认延迟
            memoryRatio > 0.1f -> 1.3f  // 内存不足，增加延迟
            else -> 1.5f  // 内存严重不足，大幅增加延迟
        }
        
        val adaptiveDelay = (baseDelay * memoryFactor).toLong()
        return adaptiveDelay.coerceIn(MIN_DELAY, MAX_DELAY)
    }
    
    /**
     * 等待界面渲染完成的自适应延迟
     * @param rootNode 根节点，用于判断界面复杂度
     * @return 延迟时间
     */
    fun waitForRender(rootNode: AccessibilityNodeInfo? = null): Long {
        val baseDelay = if (rootNode != null && isComplexUI(rootNode)) {
            500L  // 复杂界面需要更长时间
        } else {
            300L  // 简单界面使用默认时间
        }
        return getAdaptiveDelay(baseDelay)
    }
    
    /**
     * 判断界面是否复杂
     * @param node 根节点
     * @return 是否为复杂界面
     */
    private fun isComplexUI(node: AccessibilityNodeInfo): Boolean {
        var childCount = 0
        countChildren(node, childCount)
        return childCount > 20  // 子节点超过20个认为是复杂界面
    }
    
    /**
     * 递归计算子节点数量
     */
    private fun countChildren(node: AccessibilityNodeInfo?, count: Int): Int {
        if (node == null || count > 30) return count  // 限制递归深度
        var total = count
        for (i in 0 until node.childCount) {
            total++
            total = countChildren(node.getChild(i), total)
        }
        return total
    }
    
    /**
     * 带超时的等待
     * @param condition 等待条件
     * @param timeoutMs 超时时间（毫秒）
     * @return 是否等待成功
     */
    fun waitForCondition(condition: () -> Boolean, timeoutMs: Long): Boolean {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (condition()) {
                return true
            }
            Thread.sleep(50)  // 每50ms检查一次
        }
        return false
    }
}