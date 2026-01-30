package com.example.onepass

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class AppInfo(val label: String, val packageName: String, val icon: Drawable, var selected: Boolean, val order: Int = 0)

class CommonAppsActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "CommonAppsActivity"
        private const val PREFS = "common_apps_prefs"
        private const val KEY_COMMON_APPS = "common_apps"
        private const val KEY_APP_ORDERS = "app_orders"
        private const val MAX_COMMON_APPS = 6
    }
    
    private lateinit var searchEdit: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppAdapter
    private val apps = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "CommonAppsActivity onCreate 开始")
        
        setContentView(R.layout.activity_common_apps)
        
        Log.d(TAG, "布局设置成功，开始初始化视图")
        
        val packageManager = packageManager
        searchEdit = findViewById(R.id.editSearch)
        recyclerView = findViewById(R.id.recyclerViewApps)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        loadApps(packageManager)
        adapter = AppAdapter(apps)
        recyclerView.adapter = adapter
        
        searchEdit.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                val q = (newText ?: "").trim().lowercase()
                adapter.filter(q)
                return true
            }
        })
        
        findViewById<Button>(R.id.btnDone).setOnClickListener {
            Log.d(TAG, "完成按钮被点击")
            
            // 保存选中的应用
            val selectedApps = apps.filter { it.selected }
            val selectedPackageNames = selectedApps.map { it.packageName }.toSet()
            
            // 为选中的应用分配排序值
            val appOrders = mutableMapOf<String, Int>()
            selectedApps.forEachIndexed { index, app ->
                appOrders[app.packageName] = index
            }
            
            getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putStringSet(KEY_COMMON_APPS, selectedPackageNames)
                .putString(KEY_APP_ORDERS, formatAppOrders(appOrders))
                .apply()
            
            Log.d(TAG, "保存了 ${selectedApps.size} 个常用应用")
            Toast.makeText(this, "常用应用已保存", Toast.LENGTH_SHORT).show()
            finish()
        }
        
        Log.d(TAG, "CommonAppsActivity onCreate 完成")
    }

    private fun loadApps(packageManager: PackageManager) {
        Log.d(TAG, "loadApps 开始加载应用列表")
        
        apps.clear()
        
        // 从 SharedPreferences 加载已保存的应用列表
        val savedApps = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_COMMON_APPS, HashSet<String>()) ?: HashSet()
        
        // 从 SharedPreferences 加载应用排序信息
        val savedOrders = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_APP_ORDERS, null)
        val appOrders = if (savedOrders != null) {
            try {
                parseAppOrders(savedOrders)
            } catch (e: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }
        
        try {
            val applicationInfos = packageManager.getInstalledApplications(0)
            Log.d(TAG, "扫描到应用总数: ${applicationInfos.size}")
            
            var currentOrder = 0
            for (applicationInfo in applicationInfos) {
                val packageName = applicationInfo.packageName
                
                if (!shouldIncludeApp(packageName, applicationInfo, packageManager)) {
                    continue
                }
                
                Log.d(TAG, "处理应用: $packageName")
                
                try {
                    val appName = applicationInfo.loadLabel(packageManager).toString()
                    val icon = applicationInfo.loadIcon(packageManager)
                    
                    // 检查是否为默认图标
                    if (hasDefaultIcon(packageName, packageManager)) {
                        Log.d(TAG, "  -> 跳过默认图标应用: $appName")
                        continue
                    }
                    
                    // 检查应用名是否为 XX.XX.XX 格式
                    if (isPackageNameFormatApp(appName)) {
                        Log.d(TAG, "  -> 跳过包名格式应用: $appName")
                        continue
                    }
                    
                    // 检查是否已在保存的列表中
                    val isSelected = savedApps.contains(packageName)
                    // 获取应用的排序值
                    val order = appOrders[packageName] ?: currentOrder++
                    val appInfo = AppInfo(appName, packageName, icon, isSelected, order)
                    apps.add(appInfo)
                    
                    Log.d(TAG, "  -> 添加到列表: $appName, 选中状态: $isSelected, 排序: $order")
                } catch (e: Exception) {
                    Log.e(TAG, "  -> 处理应用失败: ${e.message}", e)
                    continue
                }
            }
            
            Log.d(TAG, "应用扫描完成，共添加 ${apps.size} 个应用")
            // 先按选中状态排序，再按排序值排序，最后按名称排序
            apps.sortWith(Comparator {
                    app1, app2 ->
                if (app1.selected && !app2.selected) {
                    -1
                } else if (!app1.selected && app2.selected) {
                    1
                } else if (app1.order != app2.order) {
                    app1.order.compareTo(app2.order)
                } else {
                    app1.label.compareTo(app2.label, ignoreCase = true)
                }
            })
            Log.d(TAG, "应用列表已排序")
        } catch (e: Exception) {
            Log.e(TAG, "加载应用列表时出错: ${e.message}", e)
        }
    }

    private fun shouldIncludeApp(packageName: String, applicationInfo: ApplicationInfo, packageManager: PackageManager): Boolean {
        val isSystemApp = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        
        if (isExcludedSystemApp(packageName)) {
            return false
        }
        
        if (!isSystemApp) {
            return true
        }
        
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            return true
        }
        
        return isImportantSystemApp(packageName) || isUsefulSystemApp(packageName) || isUserVisibleApp(packageName, applicationInfo, packageManager)
    }

    private fun isExcludedSystemApp(packageName: String): Boolean {
        val excludedApps = listOf(
            "android",
            "com.android.shell",
            "com.android.sharedstoragebackup",
            "com.android.printspooler",
            "com.android.externalstorage",
            "com.android.providers.partnerbookmarks",
            "com.android.proxyhandler",
            "com.android.fallback",
            "com.android.managedprovisioning",
            "com.android.defcontainer",
            "com.android.backupconfirm",
            "com.android.keychain",
            "com.android.pacprocessor",
            "com.android.statementservice",
            "com.android.server.telecom",
            "com.android.cts",
            "com.android.development",
            "com.android.smoketest",
            "com.android.test",
            "com.android.emulator",
            "com.android.systemui.tests",
            "com.android.companiondevicemanager",
            "com.android.bips",
            "com.android.bluetoothmidiservice",
            "com.android.bookmarkprovider",
            "com.android.calllogbackup",
            "com.android.captiveportallogin",
            "com.android.cellbroadcastreceiver",
            "com.android.certinstaller",
            "com.android.companiondevicemanager",
            "com.android.dreams.basic",
            "com.android.dreams.phototable",
            "com.android.emergency",
            "com.android.htmlviewer",
            "com.android.inputdevices",
            "com.android.location.fused",
            "com.android.managedprovisioning",
            "com.android.nfc",
            "com.android.onetimeinitializer",
            "com.android.providers.userdictionary",
            "com.android.vpndialogs",
            "com.android.wallpaperbackup",
            "com.android.webview"
        )
        return excludedApps.any { excluded -> packageName == excluded || packageName.startsWith("$excluded.") }
    }

    private fun isImportantSystemApp(packageName: String): Boolean {
        val importantSystemApps = listOf(
            "com.android.phone",
            "com.android.dialer",
            "com.android.contacts",
            "com.android.messaging",
            "com.android.mms",
            "com.android.camera",
            "com.android.camera2",
            "com.google.android.GoogleCamera",
            "com.android.gallery",
            "com.android.music",
            "com.android.calculator2",
            "com.android.calendar",
            "com.android.deskclock",
            "com.android.settings",
            "com.android.documentsui",
            "com.android.browser",
            "com.google.android.chrome",
            "com.android.soundrecorder",
            "com.google.android.apps.maps",
            "com.autonavi.minimap",
            "com.baidu.BaiduMap",
            "com.android.email",
            "com.google.android.gm",
            "com.google.android.apps.weather",
            "com.miui.weather",
            "com.huawei.weather",
            "com.google.android.keep",
            "com.miui.notes",
            "com.huawei.notepad",
            "com.google.android.apps.fitness",
            "com.huawei.health",
            "com.xiaomi.wearable",
            "com.android.vending",
            "com.google.android.gms",
            "com.google.android.gsf",
            "com.android.systemui"
        )
        return importantSystemApps.contains(packageName)
    }

    private fun isUsefulSystemApp(packageName: String): Boolean {
        val usefulSystemApps = listOf(
            "com.android.settings",
            "com.android.systemui",
            "com.android.providers.settings",
            "com.android.packageinstaller",
            "com.android.providers.media",
            "com.android.providers.downloads",
            "com.android.providers.contacts",
            "com.android.providers.telephony",
            "com.android.inputmethod.latin",
            "com.sohu.inputmethod.sogou",
            "com.baidu.input",
            "com.android.vending",
            "com.huawei.appmarket",
            "com.xiaomi.market",
            "com.bbk.appstore",
            "com.oppo.market",
            "com.android.browser",
            "com.chrome.beta",
            "com.chrome.dev",
            "com.chrome.canary",
            "com.google.android.chrome",
            "com.google.android.apps.docs",
            "com.google.android.apps.sheets",
            "com.google.android.apps.slides",
            "com.google.android.gms",
            "com.google.android.gsf",
            "com.android.wallpapercropper",
            "com.android.wallpaper.livepicker"
        )
        return usefulSystemApps.contains(packageName)
    }

    private fun isUserVisibleApp(packageName: String, applicationInfo: ApplicationInfo, packageManager: PackageManager): Boolean {
        return try {
            val appName = applicationInfo.loadLabel(packageManager).toString()
            val icon = applicationInfo.loadIcon(packageManager)
            appName.isNotBlank() && appName != packageName && icon != null && 
            !packageName.contains("test") && !packageName.contains("stub") && 
            !packageName.endsWith(".provider") && !packageName.endsWith(".service")
        } catch (e: Exception) {
            false
        }
    }

    private fun hasDefaultIcon(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            // 检查应用的icon资源是否为0，如果是0则表示使用默认图标
            packageInfo.applicationInfo?.icon == 0
        } catch (e: Exception) {
            // 如果获取包信息失败，默认认为不是默认图标
            false
        }
    }

    private fun isPackageNameFormatApp(appName: String): Boolean {
        // 检查应用名是否符合 XX.XX.XX 格式（包含至少两个点）
        return appName.count { it == '.' } >= 2
    }

    private fun parseAppOrders(ordersString: String): Map<String, Int> {
        val orders = mutableMapOf<String, Int>()
        val pairs = ordersString.split(",")
        for (pair in pairs) {
            val parts = pair.split(":")
            if (parts.size == 2) {
                try {
                    orders[parts[0]] = parts[1].toInt()
                } catch (e: Exception) {
                    // 忽略解析错误
                }
            }
        }
        return orders
    }

    private fun formatAppOrders(orders: Map<String, Int>): String {
        val pairs = mutableListOf<String>()
        for ((packageName, order) in orders) {
            pairs.add("$packageName:$order")
        }
        return pairs.joinToString(",")
    }

    inner class AppAdapter(private val items: List<AppInfo>) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {
        private var displayItems = items.toList()
        fun filter(query: String) {
            displayItems = if (query.isEmpty()) items else items.filter { it.label.lowercase().contains(query) }
            notifyDataSetChanged()
            Log.d(TAG, "搜索过滤: 查询='$query', 结果数=${displayItems.size}")
        }
        fun updateDisplayItems() {
            displayItems = items.toList()
            notifyDataSetChanged()
            Log.d(TAG, "更新显示列表, 结果数=${displayItems.size}")
        }
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
            return ViewHolder(view)
        }
        override fun getItemCount(): Int = displayItems.size
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = displayItems[position]
            holder.bind(app)
        }
        inner class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
            private val icon: android.widget.ImageView = itemView.findViewById(R.id.appIcon)
            private val label: android.widget.TextView = itemView.findViewById(R.id.appLabel)
            private val check: android.widget.Switch = itemView.findViewById(R.id.appSelected)
            fun bind(app: AppInfo) {
                icon.setImageDrawable(app.icon)
                label.text = app.label
                check.isChecked = app.selected
                
                // 设置已启用应用的背景色
                if (app.selected) {
                    itemView.setBackgroundColor(itemView.resources.getColor(android.R.color.holo_blue_light, null))
                } else {
                    itemView.setBackgroundColor(itemView.resources.getColor(android.R.color.transparent, null))
                }
                
                itemView.setOnClickListener {
                    if (app.selected) {
                        // 取消选择
                        app.selected = false
                        check.isChecked = false
                        itemView.setBackgroundColor(itemView.resources.getColor(android.R.color.transparent, null))
                        Log.d(TAG, "应用点击: ${app.label}, 选中状态: false")
                    } else {
                        // 检查是否超过最大选择数量
                        val selectedCount = apps.count { it.selected }
                        if (selectedCount >= MAX_COMMON_APPS) {
                            Toast.makeText(itemView.context, "最多只能选择6个应用", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "应用点击: ${app.label}, 已达到最大选择数量")
                            return@setOnClickListener
                        }
                        // 选择应用
                        app.selected = true
                        check.isChecked = true
                        itemView.setBackgroundColor(itemView.resources.getColor(android.R.color.holo_blue_light, null))
                        Log.d(TAG, "应用点击: ${app.label}, 选中状态: true")
                    }
                }
                check.setOnCheckedChangeListener { _, isChecked -> 
                    if (isChecked) {
                        // 检查是否超过最大选择数量
                        val selectedCount = apps.count { it.selected }
                        if (selectedCount >= MAX_COMMON_APPS) {
                            check.isChecked = false
                            Toast.makeText(itemView.context, "最多只能选择6个应用", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "应用切换: ${app.label}, 已达到最大选择数量")
                            return@setOnCheckedChangeListener
                        }
                        itemView.setBackgroundColor(itemView.resources.getColor(android.R.color.holo_blue_light, null))
                    } else {
                        itemView.setBackgroundColor(itemView.resources.getColor(android.R.color.transparent, null))
                    }
                    app.selected = isChecked
                    Log.d(TAG, "应用切换: ${app.label}, 选中状态: $isChecked")
                }
            }
        }
    }
}
