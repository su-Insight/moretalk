package com.example.onepass

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CommonAppsActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "CommonAppsActivity"
    }
    
    private lateinit var searchEdit: SearchView
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "CommonAppsActivity onCreate 开始")
        
        setContentView(R.layout.activity_common_apps)
        
        Log.d(TAG, "布局设置成功，开始初始化视图")
        
        val packageManager = packageManager
        searchEdit = findViewById(R.id.editSearch)
        recyclerView = findViewById(R.id.recyclerViewApps)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        testAppScanning(packageManager)
        
        recyclerView.adapter = SimpleAdapter()
        
        searchEdit.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
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

    private fun testAppScanning(packageManager: PackageManager) {
        Log.d(TAG, "testAppScanning 开始测试应用扫描")
        
        try {
            val applicationInfos = packageManager.getInstalledApplications(0)
            Log.d(TAG, "扫描到应用总数: ${applicationInfos.size}")
            
            for (i in applicationInfos.indices) {
                if (i < 10) {
                    val packageName = applicationInfos[i].packageName
                    Log.d(TAG, "应用 $i: $packageName")
                    
                    try {
                        val appName = applicationInfos[i].loadLabel(packageManager).toString()
                        Log.d(TAG, "  -> 应用名称: $appName")
                    } catch (e: Exception) {
                        Log.e(TAG, "  -> 获取应用名称失败: ${e.message}", e)
                    }
                }
            }
            
            Log.d(TAG, "应用扫描测试完成")
        } catch (e: Exception) {
            Log.e(TAG, "应用扫描测试出错: ${e.message}", e)
        }
    }

    inner class SimpleAdapter : RecyclerView.Adapter<SimpleAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = 0

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        }

        inner class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        }
    }
}
