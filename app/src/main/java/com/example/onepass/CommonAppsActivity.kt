package com.example.onepass

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

data class AppInfo(val label: String, val packageName: String, val icon: Drawable, var selected: Boolean)

class CommonAppsActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "CommonAppsActivity"
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
            Toast.makeText(this, "常用应用已保存", Toast.LENGTH_SHORT).show()
            finish()
        }
        
        Log.d(TAG, "CommonAppsActivity onCreate 完成")
    }

    private fun loadApps(packageManager: PackageManager) {
        Log.d(TAG, "loadApps 开始加载应用列表")
        
        apps.clear()
        
        try {
            val applicationInfos = packageManager.getInstalledApplications(0)
            Log.d(TAG, "扫描到应用总数: ${applicationInfos.size}")
            
            for (applicationInfo in applicationInfos) {
                val packageName = applicationInfo.packageName
                
                if (!shouldIncludeApp(packageName, applicationInfo, packageManager)) {
                    continue
                }
                
                Log.d(TAG, "处理应用: $packageName")
                
                try {
                    val appName = applicationInfo.loadLabel(packageManager).toString()
                    val icon = applicationInfo.loadIcon(packageManager)
                    
                    val appInfo = AppInfo(appName, packageName, icon, false)
                    apps.add(appInfo)
                    
                    Log.d(TAG, "  -> 添加到列表: $appName")
                } catch (e: Exception) {
                    Log.e(TAG, "  -> 处理应用失败: ${e.message}", e)
                    continue
                }
            }
            
            Log.d(TAG, "应用扫描完成，共添加 ${apps.size} 个应用")
            apps.sortBy { it.label.lowercase() }
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

    inner class AppAdapter(private val items: List<AppInfo>) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {
        private var displayItems = items.toList()
        fun filter(query: String) {
            displayItems = if (query.isEmpty()) items else items.filter { it.label.lowercase().contains(query) }
            notifyDataSetChanged()
            Log.d(TAG, "搜索过滤: 查询='$query', 结果数=${displayItems.size}")
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
            private val check: android.widget.CheckBox = itemView.findViewById(R.id.appSelected)
            fun bind(app: AppInfo) {
                icon.setImageDrawable(app.icon)
                label.text = app.label
                check.isChecked = app.selected
                itemView.setOnClickListener {
                    app.selected = !app.selected
                    check.isChecked = app.selected
                    Log.d(TAG, "应用点击: ${app.label}, 选中状态: ${app.selected}")
                }
                check.setOnCheckedChangeListener { _, isChecked -> 
                    app.selected = isChecked
                    Log.d(TAG, "应用切换: ${app.label}, 选中状态: $isChecked")
                }
            }
        }
    }
}
