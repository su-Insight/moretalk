package com.example.onepass

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WechatAccessibilityService : AccessibilityService() {
    companion object {
        private const val TAG = "WechatAccessibility"
    }
    // 协程作用域
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    // 🔥 新增：标记是否已经完成了启动时的5秒等待
    private var hasWaitedForDualApp = false
    // 🔥 新增：标记是否正在倒计时中，防止重复启动协程
    private var isWaitingNow = false

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val currentActivity = event?.className ?: return

        // 可选：过滤一些无关事件，防止日志刷屏
        // if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) return

        // 调试日志
        Log.d(TAG, "Current Activity: $currentActivity, Step: ${WeChatData.index}")

        // =========================================================================
        // 步骤 1: 启动微信 -> 等待双开选择 -> 检测是否在首页
        // =========================================================================
        if (WeChatData.index == 1) {

            // --- 阶段 A: 首次启动的 5 秒强制等待 (处理双开弹窗) ---
//            if (!hasWaitedForDualApp) {
//                if (!isWaitingNow) {
//                    Log.d(TAG, "检测到步骤1，开始5秒暂停，请手动选择微信双开...")
//                    isWaitingNow = true
//
//                    serviceScope.launch {
//                        // 挂起 5 秒，不阻塞主线程
//                        delay(5000)
//
//                        Log.d(TAG, ">>>>> 5秒时间到！ <<<<<")
//                        hasWaitedForDualApp = true
//                        isWaitingNow = false
//
//                        // 【关键】醒来后，主动去检测一次当前界面
//                        // 因为倒计时结束时屏幕可能是静止的，不会触发 onAccessibilityEvent
//                        checkIfAtHomeAndProceed()
//                    }
//                }
//                // 在等待期间，直接返回，不执行任何后续操作
//                return
//            }

            // --- 阶段 B: 5秒后的常规检测逻辑 ---
            Log.d(TAG, "步骤1检测: 当前界面: $currentActivity")
            checkIfAtHomeAndProceed()
        }
        
        if (WeChatData.index == 2) {
            Log.d(TAG, "步骤2: 点击搜索, 当前界面: $currentActivity")
            
            // 先检查是否在首页
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                // 中文标签检测
                val hasWechatTab = findNodeByText(rootNode, "微信")
                val hasContactsTab = findNodeByText(rootNode, "通讯录")
                val hasDiscoverTab = findNodeByText(rootNode, "发现")
                val hasMeTab = findNodeByText(rootNode, "我")
                
                // 英文标签检测
                val hasChatsTab = findNodeByText(rootNode, "Chats")
                val hasContactsEngTab = findNodeByText(rootNode, "Contacts")
                val hasDiscoverEngTab = findNodeByText(rootNode, "Discover")
                val hasMeEngTab = findNodeByText(rootNode, "Me")
                
                val isChineseHomePage = hasWechatTab && hasContactsTab && hasDiscoverTab && hasMeTab
                val isEnglishHomePage = hasChatsTab && hasContactsEngTab && hasDiscoverEngTab && hasMeEngTab
                
                if (isChineseHomePage || isEnglishHomePage) {
                    Log.d(TAG, "确认在首页，尝试查找搜索图标")
                    // 尝试通过ID查找搜索图标
                    val searchIconById = rootNode.findAccessibilityNodeInfosByViewId(WeChatId.SEARCH.id)
                    Log.d(TAG, "通过ID找到搜索图标数量: ${searchIconById.size}")
                    
                    if (searchIconById.isNotEmpty()) {
                        searchIconById.first().click()
                        Thread.sleep(500)
                        WeChatData.updateIndex(3)
                        Log.d(TAG, "点击搜索成功，进入步骤3")
                    } else {
                        // 尝试通过文本查找搜索图标
                        val searchIconByText = rootNode.findAccessibilityNodeInfosByText("搜索")
                        Log.d(TAG, "通过文本找到搜索图标数量: ${searchIconByText.size}")
                        
                        if (searchIconByText.isNotEmpty()) {
                            searchIconByText.first().click()
                            Thread.sleep(500)
                            WeChatData.updateIndex(3)
                            Log.d(TAG, "点击搜索成功，进入步骤3")
                        } else {
                            // 尝试通过内容描述查找搜索图标
                            val searchIconByDesc = findNodeByContentDescription(rootNode, "搜索")
                            if (searchIconByDesc != null) {
                                searchIconByDesc.click()
                                Thread.sleep(500)
                                WeChatData.updateIndex(3)
                                Log.d(TAG, "点击搜索成功，进入步骤3")
                            } else {
                                Log.d(TAG, "在首页但未找到搜索图标，等待下一次事件触发")
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "不在首页，返回步骤1")
                    WeChatData.updateIndex(1)
                }
            } else {
                Log.d(TAG, "rootNode 为 null，等待下一次事件触发")
            }
        }
        
        if (WeChatData.index == 3) {
            Log.d(TAG, "步骤3: 输入联系人昵称, 当前界面: $currentActivity")
            Log.d(TAG, "目标界面: ${WeChatActivity.SEARCH.id}")
            val input = rootInActiveWindow?.findAccessibilityNodeInfosByViewId(WeChatId.INPUT.id)
            Log.d(TAG, "找到输入框数量: ${input?.size ?: 0}")
            if (input != null && input.isNotEmpty()) {
                val result = input.first().input(WeChatData.value)
                Log.d(TAG, "输入结果: $result, 内容: ${WeChatData.value}")
                Thread.sleep(1000)
                WeChatData.updateIndex(4)
                Log.d(TAG, "输入成功，进入步骤4")
            } else {
                Log.d(TAG, "未找到输入框")
            }
        }
        
        if (WeChatData.index == 4) {
            if (currentActivity == WeChatActivity.SEARCH.id) {
                val contact = rootInActiveWindow?.findAccessibilityNodeInfosByViewId(WeChatId.LIST.id)
                if (contact != null && contact.isNotEmpty()) {
                    contact.first().click()
                    Thread.sleep(500)
                    WeChatData.updateIndex(5)
                }
            }
        }
        
        if (WeChatData.index == 5) {
            val more = rootInActiveWindow?.findAccessibilityNodeInfosByViewId(WeChatId.MORE.id)
            if (more != null && more.isNotEmpty()) {
                more.first().click()
                Thread.sleep(1000)
                WeChatData.updateIndex(6)
            }
        }
        
        if (WeChatData.index == 6) {
            if (currentActivity == WeChatActivity.CHAT.id) {
                val menu = rootInActiveWindow?.findAccessibilityNodeInfosByText(WeChatData.findText(false))
                if (menu != null && menu.isNotEmpty()) {
                    val rect = Rect()
                    menu.first().getBoundsInScreen(rect)
                    performClick(rect.exactCenterX(), rect.exactCenterY())
                    Thread.sleep(500)
                    WeChatData.updateIndex(7)
                }
            }
        }
        
        if (WeChatData.index == 7) {
            if (currentActivity.contains(WeChatActivity.DIALOG.id) 
                || currentActivity == WeChatActivity.DIALOG_OLD.id) {
                val options = rootInActiveWindow?.findAccessibilityNodeInfosByText(WeChatData.findText(true))
                if (options != null && options.isNotEmpty()) {
                    options.first().click()
                    Thread.sleep(500)
                    WeChatData.updateIndex(0)
                }
            }
        }
    }

    /**
     * 核心逻辑：检测当前是否在微信首页
     * 如果是 -> 跳转步骤 2
     * 如果否 -> 执行全局返回
     */
    private fun checkIfAtHomeAndProceed() {
        val rootNode = rootInActiveWindow
        if (rootNode == null) {
            Log.d(TAG, "rootNode为空，跳过检测")
            return
        }

        // 1. 检测底部标签 (使用计数法，更稳健)
        var matchCount = 0
        if (findNodeByText(rootNode, "微信")) matchCount++
        if (findNodeByText(rootNode, "通讯录")) matchCount++
        if (findNodeByText(rootNode, "发现")) matchCount++
        if (findNodeByText(rootNode, "我")) matchCount++

        // 英文适配
        var engMatchCount = 0
        if (findNodeByText(rootNode, "Chats")) engMatchCount++
        if (findNodeByText(rootNode, "Contacts")) engMatchCount++
        if (findNodeByText(rootNode, "Discover")) engMatchCount++
        if (findNodeByText(rootNode, "Me")) engMatchCount++

        Log.d(TAG, "首页特征匹配: 中文=$matchCount, 英文=$engMatchCount")

        // 只要匹配到 2 个及以上，就认为是首页 (防止遮挡导致误判)
        val isHome = matchCount >= 2 || engMatchCount >= 2

        if (isHome) {
            Log.d(TAG, ">>> 判定为微信首页，跳转步骤2 <<<")
            WeChatData.updateLanguage(engMatchCount >= 2)
            WeChatData.updateIndex(2)
        } else {
            // 不在首页的处理逻辑
            Log.d(TAG, ">>> 不在首页，尝试返回 <<<")

            // 避免在 Service/Payment 页面死循环，稍微延时一下再按返回
            serviceScope.launch {
                delay(500) // 小延时
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
        }
    }
    private fun AccessibilityNodeInfo?.click(): Boolean {
        this ?: return false
        return if (isClickable) {
            performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            parent?.click() == true
        }
    }

    private fun AccessibilityNodeInfo?.input(text: String): Boolean {
        this ?: return false
        return if (isEditable) {
            val arguments: Bundle = Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        } else {
            parent?.input(text) == true
        }
    }

    private fun performClick(x: Float, y: Float) {
        val gestureBuilder = GestureDescription.Builder()
        val path = Path()
        path.moveTo(x, y)
        gestureBuilder.addStroke(StrokeDescription(path, 0, 1))
        val gestureDescription = gestureBuilder.build()
        dispatchGesture(gestureDescription, null, null)
    }

    private fun findNodeByText(node: AccessibilityNodeInfo?, text: String): Boolean {
        if (node == null) return false
        
        if (node.text?.toString()?.contains(text) == true) {
            return true
        }
        
        if (node.contentDescription?.toString()?.contains(text) == true) {
            return true
        }
        
        for (i in 0 until node.childCount) {
            if (findNodeByText(node.getChild(i), text)) {
                return true
            }
        }
        
        return false
    }
    
    private fun findNodeByContentDescription(node: AccessibilityNodeInfo?, description: String): AccessibilityNodeInfo? {
        if (node == null) return null
        
        if (node.contentDescription?.toString()?.contains(description) == true) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            val foundNode = findNodeByContentDescription(node.getChild(i), description)
            if (foundNode != null) {
                return foundNode
            }
        }
        
        return null
    }

    override fun onInterrupt() {
        Log.d(TAG, "无障碍服务被中断")
        WeChatData.updateIndex(0)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "无障碍服务已连接")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "无障碍服务已断开")
        return super.onUnbind(intent)
    }
}