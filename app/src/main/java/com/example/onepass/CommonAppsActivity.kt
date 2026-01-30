package com.example.onepass

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CommonAppsActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "CommonAppsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "CommonAppsActivity onCreate 开始")
        
        setContentView(R.layout.activity_common_apps)
        
        Log.d(TAG, "CommonAppsActivity onCreate 完成，布局设置成功")
        
        findViewById<Button>(R.id.btnDone).setOnClickListener {
            Log.d(TAG, "完成按钮被点击")
            Toast.makeText(this, "常用应用已保存", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
