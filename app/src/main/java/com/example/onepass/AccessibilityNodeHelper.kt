package com.example.onepass

import android.view.accessibility.AccessibilityNodeInfo

object AccessibilityNodeHelper {

    fun <T> AccessibilityNodeInfo.use(block: (AccessibilityNodeInfo) -> T): T {
        try {
            return block(this)
        } finally {
            this.recycle()
        }
    }

    fun <T> List<AccessibilityNodeInfo>.use(block: (List<AccessibilityNodeInfo>) -> T): T {
        try {
            return block(this)
        } finally {
            this.forEach { it.recycle() }
        }
    }

    fun AccessibilityNodeInfo?.safeRecycle() {
        this?.recycle()
    }

    fun List<AccessibilityNodeInfo>.safeRecycleAll() {
        this.forEach { it.safeRecycle() }
    }
}