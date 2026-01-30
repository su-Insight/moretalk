package com.example.onepass

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
            
            for (i in applicationInfos.indices) {
                if (i < 50) {
                    val packageName = applicationInfos[i].packageName
                    Log.d(TAG, "应用 $i: $packageName")
                    
                    try {
                        val appName = applicationInfos[i].loadLabel(packageManager).toString()
                        val icon = applicationInfos[i].loadIcon(packageManager)
                        
                        val appInfo = AppInfo(appName, packageName, icon, false)
                        apps.add(appInfo)
                        
                        Log.d(TAG, "  -> 添加到列表: $appName")
                    } catch (e: Exception) {
                        Log.e(TAG, "  -> 处理应用失败: ${e.message}", e)
                        continue
                    }
                }
            }
            
            Log.d(TAG, "应用扫描完成，共添加 ${apps.size} 个应用")
            apps.sortBy { it.label.lowercase() }
            Log.d(TAG, "应用列表已排序")
        } catch (e: Exception) {
            Log.e(TAG, "加载应用列表时出错: ${e.message}", e)
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
