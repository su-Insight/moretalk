package com.example.onepass

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

    // Handler用于延迟触发检查
    private val mainHandler = Handler(Looper.getMainLooper())

    // 状态管理
    private var isProcessing = false // 是否正在处理
    private var navigationAttempts = 0 // 导航尝试次数
    private val MAX_NAVIGATION_ATTEMPTS = 3 // 最大导航尝试次数
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val currentActivity = event?.className ?: return
        
        Log.d(TAG, "Current Activity: $currentActivity, Step: ${WeChatData.index}")
        
        // 如果正在处理，跳过新事件
        if (isProcessing) {
            Log.d(TAG, "正在处理中，跳过新事件")
            return
        }
        
        // 步骤1: 启动微信 -> 检测当前页面并导航到首页
        if (WeChatData.index == 1) {
            Log.d(TAG, "步骤1检测: 当前界面: $currentActivity")
            processStep1(currentActivity)
        }
        
        // 步骤2: 点击搜索
        else if (WeChatData.index == 2) {
            Log.d(TAG, "步骤2: 点击搜索, 当前界面: $currentActivity")
            processStep2(currentActivity)
        }
        
        // 步骤3: 输入联系人昵称
        else if (WeChatData.index == 3) {
            Log.d(TAG, "步骤3: 输入联系人昵称, 当前界面: $currentActivity")
            processStep3(currentActivity)
        }
        
        // 步骤4: 选择联系人
        else if (WeChatData.index == 4) {
            Log.d(TAG, "步骤4: 选择联系人, 当前界面: $currentActivity")
            processStep4(currentActivity)
        }
        
        // 步骤5: 点击更多按钮
        else if (WeChatData.index == 5) {
            Log.d(TAG, "步骤5: 点击更多按钮, 当前界面: $currentActivity")
            processStep5(currentActivity)
        }
        
        // 步骤6: 点击通话菜单
        else if (WeChatData.index == 6) {
            Log.d(TAG, "步骤6: 点击通话菜单, 当前界面: $currentActivity")
            processStep6(currentActivity)
        }
        
        // 步骤7: 点击确认通话
        else if (WeChatData.index == 7) {
            Log.d(TAG, "步骤7: 点击确认通话, 当前界面: $currentActivity")
            processStep7(currentActivity)
        }
    }
    
    /**
     * 步骤1: 检测当前页面并导航到首页
     */
    private fun processStep1(currentActivity: CharSequence) {
        serviceScope.launch {
            isProcessing = true
            try {
                delay(1000) // 短暂延迟，确保界面稳定
                
                val rootNode = rootInActiveWindow
                if (rootNode == null) {
                    Log.d(TAG, "rootNode为空，等待下一次事件")
                    return@launch
                }
                
                // 分析当前页面类型
                val pageType = analyzeCurrentPage(rootNode)
                Log.d(TAG, "当前页面类型: $pageType")

                when (pageType) {
                    PageType.HOME -> {
                        Log.d(TAG, ">>> 已在微信首页，进入步骤2 <<<")
                        resetNavigationAttempts()
                        // 直接在步骤1中完成搜索点击，避免依赖新事件
                        val searchNode = findSearchIcon(rootNode)
                        if (searchNode != null) {
                            Log.d(TAG, "步骤1中直接点击搜索图标")
                            searchNode.click()
                            // 立即更新到步骤3，不再延迟
                            Log.d(TAG, ">>> 进入步骤3: 输入联系人昵称 <<<")
                            WeChatData.updateIndex(3)
                            // 延迟后重置isProcessing，等待输入框出现
                            mainHandler.postDelayed({
                                isProcessing = false
                            }, 500)
                        } else {
                            Log.d(TAG, "未找到搜索图标")
                            isProcessing = false
                        }
                        return@launch
                    }
                    PageType.CONTACTS -> {
                        Log.d(TAG, ">>> 在通讯录页面，直接进入步骤2 <<<")
                        resetNavigationAttempts()
                        // 直接在步骤1中完成搜索点击，避免依赖新事件
                        val searchNode = findSearchIcon(rootNode)
                        if (searchNode != null) {
                            Log.d(TAG, "步骤1中直接点击搜索图标")
                            searchNode.click()
                            // 立即更新到步骤3，不再延迟
                            Log.d(TAG, ">>> 进入步骤3: 输入联系人昵称 <<<")
                            WeChatData.updateIndex(3)
                            // 延迟后重置isProcessing，等待输入框出现
                            mainHandler.postDelayed({
                                isProcessing = false
                            }, 500)
                        } else {
                            Log.d(TAG, "未找到搜索图标")
                            isProcessing = false
                        }
                        return@launch
                    }
                    PageType.ME -> {
                        Log.d(TAG, ">>> 在'我的'页面，尝试导航到首页 <<<")
                        navigateFromMePage(rootNode)
                    }
                    PageType.OTHER -> {
                        Log.d(TAG, ">>> 在其他页面，尝试智能导航 <<<")
                        smartNavigation(rootNode)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "处理步骤1失败", e)
            } finally {
                isProcessing = false
            }
        }
    }
    
    /**
     * 步骤2: 点击搜索图标
     */
    private fun processStep2(currentActivity: CharSequence) {
        serviceScope.launch {
            isProcessing = true
            try {
                delay(500)
                
                val rootNode = rootInActiveWindow
                if (rootNode == null) {
                    Log.d(TAG, "rootNode为空，等待下一次事件")
                    return@launch
                }
                
                // 查找搜索图标
                val searchNode = findSearchIcon(rootNode)
                if (searchNode != null) {
                    Log.d(TAG, "点击搜索图标")
                    searchNode.click()
                    delay(800)
                    WeChatData.updateIndex(3)
                } else {
                    Log.d(TAG, "未找到搜索图标，返回步骤1")
                    WeChatData.updateIndex(1)
                }
            } catch (e: Exception) {
                Log.e(TAG, "处理步骤2失败", e)
                WeChatData.updateIndex(1)
            } finally {
                isProcessing = false
            }
        }
    }
    
    /**
     * 步骤3: 输入联系人昵称
     */
    private fun processStep3(currentActivity: CharSequence) {
        serviceScope.launch {
            isProcessing = true
            try {
                delay(500)

                val rootNode = rootInActiveWindow
                if (rootNode == null) {
                    Log.d(TAG, "rootNode为空，等待下一次事件")
                    return@launch
                }

                // 查找输入框
                val inputNode = findInputField(rootNode)
                if (inputNode != null) {
                    Log.d(TAG, "输入联系人昵称: ${WeChatData.value}")
                    val result = inputNode.input(WeChatData.value)
                    if (result) {
                        Log.d(TAG, "输入成功，等待搜索结果出现")
                        // 增加延迟，等待搜索结果加载完成
                        delay(1500)
                        WeChatData.updateIndex(4)
                        Log.d(TAG, ">>> 进入步骤4: 选择联系人 <<<")
                    } else {
                        Log.d(TAG, "输入失败，重新尝试")
                    }
                } else {
                    Log.d(TAG, "未找到输入框，返回步骤2")
                    WeChatData.updateIndex(2)
                }
            } catch (e: Exception) {
                Log.e(TAG, "处理步骤3失败", e)
                WeChatData.updateIndex(2)
            } finally {
                isProcessing = false
            }
        }
    }
    
    /**
     * 步骤4: 选择联系人
     */
    private fun processStep4(currentActivity: CharSequence) {
        serviceScope.launch {
            isProcessing = true
            try {
                delay(500)

                val rootNode = rootInActiveWindow
                if (rootNode == null) {
                    Log.d(TAG, "rootNode为空，等待下一次事件")
                    return@launch
                }

                // 查找联系人列表（第一个搜索结果）
                val contactNode = findContactList(rootNode)
                if (contactNode != null) {
                    Log.d(TAG, "选择联系人")
                    contactNode.click()
                    delay(800)
                    WeChatData.updateIndex(5)
                    Log.d(TAG, ">>> 进入步骤5: 点击更多按钮 <<<")
                } else {
                    Log.d(TAG, "未找到联系人列表，搜索结果可能还未出现，保持步骤4继续等待")
                    // 不返回步骤3，保持index为4继续等待新事件
                }
            } catch (e: Exception) {
                Log.e(TAG, "处理步骤4失败", e)
                // 不返回步骤3，保持index为4继续等待
            } finally {
                isProcessing = false
            }
        }
    }
    
    /**
     * 步骤5: 点击更多按钮
     */
    private fun processStep5(currentActivity: CharSequence) {
        serviceScope.launch {
            isProcessing = true
            try {
                delay(500)
                
                val rootNode = rootInActiveWindow
                if (rootNode == null) {
                    Log.d(TAG, "rootNode为空，等待下一次事件")
                    return@launch
                }
                
                // 查找更多按钮
                val moreNode = findMoreButton(rootNode)
                if (moreNode != null) {
                    Log.d(TAG, "点击更多按钮")
                    moreNode.click()
                    delay(500)
                    WeChatData.updateIndex(6)
                } else {
                    Log.d(TAG, "未找到更多按钮，返回步骤4")
                    WeChatData.updateIndex(4)
                }
            } catch (e: Exception) {
                Log.e(TAG, "处理步骤5失败", e)
                WeChatData.updateIndex(4)
            } finally {
                isProcessing = false
            }
        }
    }
    
    /**
     * 步骤6: 点击通话菜单
     */
    private fun processStep6(currentActivity: CharSequence) {
        serviceScope.launch {
            isProcessing = true
            try {
                delay(500)
                
                val rootNode = rootInActiveWindow
                if (rootNode == null) {
                    Log.d(TAG, "rootNode为空，等待下一次事件")
                    return@launch
                }
                
                // 查找通话菜单
                val callNode = findCallMenu(rootNode)
                if (callNode != null) {
                    Log.d(TAG, "点击通话菜单")
                    callNode.click()
                    delay(500)
                    WeChatData.updateIndex(7)
                } else {
                    Log.d(TAG, "未找到通话菜单，返回步骤5")
                    WeChatData.updateIndex(5)
                }
            } catch (e: Exception) {
                Log.e(TAG, "处理步骤6失败", e)
                WeChatData.updateIndex(5)
            } finally {
                isProcessing = false
            }
        }
    }
    
    /**
     * 步骤7: 点击确认通话
     */
    private fun processStep7(currentActivity: CharSequence) {
        serviceScope.launch {
            isProcessing = true
            try {
                delay(500)
                
                val rootNode = rootInActiveWindow
                if (rootNode == null) {
                    Log.d(TAG, "rootNode为空，等待下一次事件")
                    return@launch
                }
                
                // 查找确认按钮
                val confirmNode = findConfirmButton(rootNode)
                if (confirmNode != null) {
                    Log.d(TAG, "点击确认通话")
                    confirmNode.click()
                    delay(1000)
                    Log.d(TAG, "通话操作完成")
                    WeChatData.updateIndex(0) // 重置步骤
                } else {
                    Log.d(TAG, "未找到确认按钮，返回步骤6")
                    WeChatData.updateIndex(6)
                }
            } catch (e: Exception) {
                Log.e(TAG, "处理步骤7失败", e)
                WeChatData.updateIndex(6)
            } finally {
                isProcessing = false
            }
        }
    }
    
    /**
     * 页面类型枚举
     */
    private enum class PageType {
        HOME,        // 微信首页
        CONTACTS,    // 通讯录页面
        ME,          // "我的"页面
        OTHER        // 其他页面
    }
    
    /**
     * 分析当前页面类型
     */
    private fun analyzeCurrentPage(rootNode: AccessibilityNodeInfo): PageType {
        // 1. 检测是否在微信首页
        if (isHomePage(rootNode)) {
            return PageType.HOME
        }
        
        // 2. 检测是否在通讯录页面
        if (isContactsPage(rootNode)) {
            return PageType.CONTACTS
        }
        
        // 3. 检测是否在"我的"页面
        if (isMePage(rootNode)) {
            return PageType.ME
        }
        
        // 4. 其他页面
        return PageType.OTHER
    }
    
    /**
     * 检测是否在微信首页
     */
    private fun isHomePage(rootNode: AccessibilityNodeInfo): Boolean {
        // 检测微信首页特征：底部有4个标签（微信、通讯录、发现、我）
        // 首先尝试查找搜索输入框（首页通常有搜索框）
        val hasSearchInput = findInputField(rootNode) != null

        // 检查底部标签数量（首页有4个标签）
        val bottomTabNodes = findBottomTabs(rootNode)
        val hasFourTabs = bottomTabNodes.size >= 3

        Log.d(TAG, "首页检测 - 有搜索框: $hasSearchInput, 底部标签数: ${bottomTabNodes.size}")

        return hasSearchInput || hasFourTabs
    }

    /**
     * 查找底部所有可点击的标签
     */
    private fun findBottomTabs(rootNode: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        findBottomClickableNodes(rootNode, result)
        return result
    }

    /**
     * 递归查找底部可点击的节点（底部标签）
     */
    private fun findBottomClickableNodes(node: AccessibilityNodeInfo?, result: MutableList<AccessibilityNodeInfo>) {
        if (node == null) return

        val rect = Rect()
        node.getBoundsInScreen(rect)
        val screenHeight = screenHeight()

        // 如果节点在屏幕下半部分（可能是底部标签区域）且可点击
        if (rect.top > screenHeight * 0.6 && node.isClickable) {
            result.add(node)
        }

        for (i in 0 until node.childCount) {
            findBottomClickableNodes(node.getChild(i), result)
        }
    }
    
    /**
     * 检测是否在通讯录页面
     */
    private fun isContactsPage(rootNode: AccessibilityNodeInfo): Boolean {
        // 检测通讯录页面特征
        val hasContactsTab = findNodeByText(rootNode, "通讯录") || findNodeByText(rootNode, "Contacts")
        val hasNewContact = findNodeByText(rootNode, "新的朋友") || findNodeByText(rootNode, "New Friend")
        val hasOfficialAccounts = findNodeByText(rootNode, "公众号") || findNodeByText(rootNode, "Official Accounts")
        
        return hasContactsTab || hasNewContact || hasOfficialAccounts
    }
    
    /**
     * 检测是否在"我的"页面
     */
    private fun isMePage(rootNode: AccessibilityNodeInfo): Boolean {
        // 检测"我的"页面特征
        val hasMeTab = findNodeByText(rootNode, "我") || findNodeByText(rootNode, "Me")
        val hasWallet = findNodeByText(rootNode, "钱包") || findNodeByText(rootNode, "Wallet")
        val hasFavorites = findNodeByText(rootNode, "收藏") || findNodeByText(rootNode, "Favorites")
        val hasSettings = findNodeByText(rootNode, "设置") || findNodeByText(rootNode, "Settings")
        
        return hasMeTab || hasWallet || hasFavorites || hasSettings
    }
    
    /**
     * 从"我的"页面导航到首页
     */
    private fun navigateFromMePage(rootNode: AccessibilityNodeInfo) {
        serviceScope.launch {
            try {
                // 尝试点击底部"微信"标签
                val wechatTab = findBottomTab(rootNode, "微信") ?: findBottomTab(rootNode, "Chats")
                if (wechatTab != null) {
                    Log.d(TAG, "点击底部微信标签")
                    wechatTab.click()
                    delay(800)
                    resetNavigationAttempts()
                } else {
                    // 如果没有底部标签，尝试返回
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    delay(500)
                    incrementNavigationAttempts()
                }
            } catch (e: Exception) {
                Log.e(TAG, "从‘我的’页面导航失败", e)
                incrementNavigationAttempts()
            }
        }
    }
    
    /**
     * 智能导航
     */
    private fun smartNavigation(rootNode: AccessibilityNodeInfo) {
        serviceScope.launch {
            try {
                if (navigationAttempts < MAX_NAVIGATION_ATTEMPTS) {
                    // 优先尝试点击底部标签栏
                    val wechatTab = findBottomTab(rootNode, "微信") ?: findBottomTab(rootNode, "Chats")
                    if (wechatTab != null) {
                        Log.d(TAG, "点击底部微信标签")
                        wechatTab.click()
                        delay(800)
                        resetNavigationAttempts()
                        return@launch
                    }
                    
                    // 尝试点击底部"通讯录"标签
                    val contactsTab = findBottomTab(rootNode, "通讯录") ?: findBottomTab(rootNode, "Contacts")
                    if (contactsTab != null) {
                        Log.d(TAG, "点击底部通讯录标签")
                        contactsTab.click()
                        delay(800)
                        resetNavigationAttempts()
                        return@launch
                    }
                    
                    // 尝试返回操作
                    Log.d(TAG, "尝试返回操作 (${navigationAttempts + 1}/$MAX_NAVIGATION_ATTEMPTS)")
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    delay(500)
                    incrementNavigationAttempts()
                } else {
                    Log.d(TAG, "导航尝试次数达到上限，尝试重启微信")
                    resetNavigationAttempts()
                    restartWechat()
                }
            } catch (e: Exception) {
                Log.e(TAG, "智能导航失败", e)
                incrementNavigationAttempts()
            }
        }
    }
    
    /**
     * 导航失败时重置状态
     */
    private fun restartWechat() {
        Log.d(TAG, "导航失败，重置到初始状态")
        resetNavigationAttempts()
        WeChatData.updateIndex(0)
    }
    
    /**
     * 查找底部标签
     */
    private fun findBottomTab(rootNode: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val nodes = rootNode.findAccessibilityNodeInfosByText(text)
        for (node in nodes) {
            if (node.isClickable) {
                // 检查是否在底部区域
                val rect = Rect()
                node.getBoundsInScreen(rect)
                // 简单判断：如果节点在屏幕下半部分，认为是底部标签
                if (rect.top > (screenHeight() / 2)) {
                    return node
                }
            }
        }
        return null
    }
    
    /**
     * 获取屏幕高度
     */
    private fun screenHeight(): Int {
        val displayMetrics = resources.displayMetrics
        return displayMetrics.heightPixels
    }
    
    /**
     * 增加导航尝试次数
     */
    private fun incrementNavigationAttempts() {
        navigationAttempts++
        if (navigationAttempts >= MAX_NAVIGATION_ATTEMPTS) {
            Log.d(TAG, "导航尝试次数达到上限，重启微信")
            restartWechat()
            resetNavigationAttempts()
        }
    }
    
    /**
     * 重置导航尝试次数
     */
    private fun resetNavigationAttempts() {
        navigationAttempts = 0
    }
    
    /**
     * 查找搜索图标
     */
    private fun findSearchIcon(rootNode: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // 方法1: 通过ViewId查找
        val searchById = rootNode.findAccessibilityNodeInfosByViewId(WeChatId.SEARCH.id)
        if (searchById.isNotEmpty()) {
            return searchById.first()
        }
        
        // 方法2: 通过文本查找
        val searchByText = rootNode.findAccessibilityNodeInfosByText("搜索")
        if (searchByText.isNotEmpty()) {
            return searchByText.first()
        }
        
        // 方法3: 通过英文文本查找
        val searchByEngText = rootNode.findAccessibilityNodeInfosByText("Search")
        if (searchByEngText.isNotEmpty()) {
            return searchByEngText.first()
        }
        
        return null
    }
    
    /**
     * 查找输入框
     */
    private fun findInputField(rootNode: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // 方法1: 通过ViewId查找
        val inputById = rootNode.findAccessibilityNodeInfosByViewId(WeChatId.INPUT.id)
        if (inputById.isNotEmpty()) {
            return inputById.first()
        }
        
        // 方法2: 查找可编辑节点
        val editableNodes = mutableListOf<AccessibilityNodeInfo>()
        findEditableNodes(rootNode, editableNodes)
        if (editableNodes.isNotEmpty()) {
            return editableNodes.first()
        }
        
        // 方法3: 通过文本查找
        val inputByText = rootNode.findAccessibilityNodeInfosByText("搜索")
        if (inputByText.isNotEmpty()) {
            return inputByText.first()
        }
        
        return null
    }
    
    /**
     * 查找联系人列表
     */
    private fun findContactList(rootNode: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // 方法1: 通过ViewId查找
        val listById = rootNode.findAccessibilityNodeInfosByViewId(WeChatId.LIST.id)
        if (listById.isNotEmpty()) {
            return listById.first()
        }
        
        // 方法2: 查找列表类型的节点
        return findListViewNode(rootNode)
    }
    
    /**
     * 查找更多按钮
     */
    private fun findMoreButton(rootNode: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // 方法1: 通过ViewId查找
        val moreById = rootNode.findAccessibilityNodeInfosByViewId(WeChatId.MORE.id)
        if (moreById.isNotEmpty()) {
            return moreById.first()
        }
        
        // 方法2: 通过文本查找
        val moreByText = rootNode.findAccessibilityNodeInfosByText("更多")
        if (moreByText.isNotEmpty()) {
            return moreByText.first()
        }
        
        return null
    }
    
    /**
     * 查找通话菜单
     */
    private fun findCallMenu(rootNode: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val callText = WeChatData.findText(false)
        val callNode = rootNode.findAccessibilityNodeInfosByText(callText)
        if (callNode.isNotEmpty()) {
            return callNode.first()
        }
        
        return null
    }
    
    /**
     * 查找确认按钮
     */
    private fun findConfirmButton(rootNode: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // 查找确认、确定等按钮
        val confirmTexts = listOf("确定", "确认", "OK", "Confirm")
        
        for (text in confirmTexts) {
            val nodes = rootNode.findAccessibilityNodeInfosByText(text)
            if (nodes.isNotEmpty()) {
                return nodes.first()
            }
        }
        
        return null
    }
    
    /**
     * 查找列表视图节点
     */
    private fun findListViewNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node == null) return null
        
        // 检查节点是否可能是列表
        if (node.childCount > 0 && node.isScrollable) {
            return node
        }
        
        // 递归检查子节点
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val result = findListViewNode(child)
            if (result != null) {
                return result
            }
        }
        
        return null
    }
    
    /**
     * 查找可编辑节点
     */
    private fun findEditableNodes(node: AccessibilityNodeInfo?, result: MutableList<AccessibilityNodeInfo>) {
        if (node == null) return
        
        if (node.isEditable) {
            result.add(node)
        }
        
        for (i in 0 until node.childCount) {
            findEditableNodes(node.getChild(i), result)
        }
    }
    
    /**
     * 根据文本查找节点
     */
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
    
    /**
     * 点击节点
     */
    private fun AccessibilityNodeInfo.click(): Boolean {
        if (isClickable) {
            return performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            return parent?.click() == true
        }
    }
    
    /**
     * 输入文本
     */
    private fun AccessibilityNodeInfo.input(text: String): Boolean {
        if (isEditable) {
            val arguments = Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            return performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        } else {
            return parent?.input(text) == true
        }
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "无障碍服务被中断")
        WeChatData.updateIndex(0)
        isProcessing = false
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
