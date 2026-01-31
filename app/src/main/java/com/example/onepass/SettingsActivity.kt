package com.example.onepass

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var backButton: ImageView
    private lateinit var userAvatar: ImageView
    private lateinit var userName: TextView
    private lateinit var userStatus: TextView
    private lateinit var radioLunar: RadioButton
    private lateinit var radioSolar: RadioButton
    private lateinit var dateStyleGroup: RadioGroup
    private lateinit var seekBarIconSize: SeekBar
    private lateinit var textIconSize: TextView
    private lateinit var btnSetDefaultLauncher: Button

    // new UI controls
    private lateinit var btnCommonApps: Button
    private lateinit var switchWeather: android.widget.Switch
    private lateinit var seekBarWeatherVol: SeekBar
    private lateinit var textWeatherVol: TextView
    private lateinit var commonAppsScrollView: android.widget.HorizontalScrollView
    private lateinit var commonAppsContainer: android.widget.LinearLayout
    private lateinit var textNoCommonApps: TextView
    private lateinit var btnContacts: Button

    private var isLoggedIn = false
    private val PREFS_NAME = "OnePassPrefs"
    private val COMMON_APPS_PREFS = "common_apps_prefs"
    private val KEY_DATE_STYLE = "date_style"
    private val VALUE_LUNAR = "lunar"
    private val VALUE_SOLAR = "solar"

    private val KEY_WEATHER_ENABLED = "weather_enabled"
    private val KEY_WEATHER_VOLUME = "weather_volume"
    private val KEY_COMMON_APPS = "common_apps"
    private val AVAILABLE_APPS = arrayOf("微信", "QQ", "微博", "浏览器", "邮件")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initViews()
        loadSettings()
        setupListeners()
        updateUserStatus()
    }

    override fun onResume() {
        super.onResume()
        val commonAppsSet = getSharedPreferences(COMMON_APPS_PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_COMMON_APPS, HashSet<String>())
        loadCommonApps(commonAppsSet)
    }

    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        userAvatar = findViewById(R.id.userAvatar)
        userName = findViewById(R.id.userName)
        userStatus = findViewById(R.id.userStatus)
        radioLunar = findViewById(R.id.radioLunar)
        radioSolar = findViewById(R.id.radioSolar)
        dateStyleGroup = findViewById(R.id.dateStyleGroup)
        seekBarIconSize = findViewById(R.id.seekBarIconSize)
        textIconSize = findViewById(R.id.textIconSize)
        btnSetDefaultLauncher = findViewById(R.id.btnSetDefaultLauncher)

        // new controls
        btnCommonApps = findViewById(R.id.btnCommonApps)
        switchWeather = findViewById(R.id.switchWeather)
        seekBarWeatherVol = findViewById(R.id.seekBarWeatherVol)
        textWeatherVol = findViewById(R.id.textWeatherVol)
        commonAppsScrollView = findViewById(R.id.commonAppsScrollView)
        commonAppsContainer = findViewById(R.id.commonAppsContainer)
        textNoCommonApps = findViewById(R.id.textNoCommonApps)
        btnContacts = findViewById(R.id.btnContacts)
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val dateStyle = prefs.getString(KEY_DATE_STYLE, VALUE_SOLAR)
        if (dateStyle == VALUE_LUNAR) {
            radioLunar.isChecked = true
        } else {
            radioSolar.isChecked = true
        }

        val weatherEnabled = prefs.getBoolean(KEY_WEATHER_ENABLED, false)
        switchWeather.isChecked = weatherEnabled
        val vol = prefs.getInt(KEY_WEATHER_VOLUME, 50)
        seekBarWeatherVol.progress = vol
        textWeatherVol.text = vol.toString() + "%"

        seekBarWeatherVol.isEnabled = weatherEnabled

        val commonAppsSet = prefs.getStringSet(KEY_COMMON_APPS, HashSet<String>())
        loadCommonApps(commonAppsSet)
    }

    private fun setupListeners() {
        backButton.setOnClickListener { finish() }

        userAvatar.setOnClickListener {
            if (isLoggedIn) {
                Toast.makeText(this, "已登录", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "登录功能开发中...", Toast.LENGTH_SHORT).show()
            }
        }

        dateStyleGroup.setOnCheckedChangeListener { _, checkedId ->
            val isLunar = checkedId == R.id.radioLunar
            val editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            editor.putString(KEY_DATE_STYLE, if (isLunar) VALUE_LUNAR else VALUE_SOLAR)
            editor.apply()
            Toast.makeText(this, if (isLunar) "已选择农历" else "已选择阳历", Toast.LENGTH_SHORT).show()
        }

        seekBarIconSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textIconSize.text = progress.toString() + "dp"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Toast.makeText(this@SettingsActivity, "图标大小已设置为 " + seekBarIconSize.progress + "dp", Toast.LENGTH_SHORT).show()
            }
        })

        btnSetDefaultLauncher.setOnClickListener { setAsDefaultLauncher() }

        btnCommonApps.setOnClickListener { startActivity(Intent(this, CommonAppsActivity::class.java)) }

        btnContacts.setOnClickListener { startActivity(Intent(this, ContactsActivity::class.java)) }

        switchWeather.setOnCheckedChangeListener { _, isChecked ->
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_WEATHER_ENABLED, isChecked).apply()
            seekBarWeatherVol.isEnabled = isChecked
            Toast.makeText(this, if (isChecked) "天气播报已开启" else "天气播报已关闭", Toast.LENGTH_SHORT).show()
        }

        seekBarWeatherVol.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textWeatherVol.text = progress.toString() + "%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putInt(KEY_WEATHER_VOLUME, seekBarWeatherVol.progress).apply()
                Toast.makeText(this@SettingsActivity, "天气音量设为 ${seekBarWeatherVol.progress}%", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUserStatus() {
        if (isLoggedIn) {
            userName.text = "用户名"
            userStatus.text = "已登录"
            userStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
        } else {
            userName.text = "未登录"
            userStatus.text = "点击登录"
            userStatus.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
        }
    }

    private fun setAsDefaultLauncher() {
        val pm = packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            val componentName = intent.component
            val mainIntent = Intent().apply {
                component = componentName
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                startActivity(mainIntent)
                Toast.makeText(this, "已设置为默认桌面", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "设置失败，请手动设置", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "无法设置为默认桌面", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCommonApps(commonAppsSet: Set<String>?) {
        commonAppsContainer.removeAllViews()
        
        if (commonAppsSet.isNullOrEmpty()) {
            commonAppsScrollView.visibility = android.view.View.GONE
            textNoCommonApps.visibility = android.view.View.VISIBLE
            return
        }
        
        commonAppsScrollView.visibility = android.view.View.VISIBLE
        textNoCommonApps.visibility = android.view.View.GONE
        
        // 从 SharedPreferences 加载应用排序信息
        val savedOrders = getSharedPreferences(COMMON_APPS_PREFS, Context.MODE_PRIVATE)
            .getString("app_orders", null)
        val appOrders = if (savedOrders != null) {
            try {
                parseAppOrders(savedOrders)
            } catch (e: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }
        
        // 按排序值对应用进行排序
        val sortedApps = commonAppsSet.sortedWith(Comparator {
                app1, app2 ->
            val order1 = appOrders[app1] ?: Int.MAX_VALUE
            val order2 = appOrders[app2] ?: Int.MAX_VALUE
            order1.compareTo(order2)
        })
        
        for (packageName in sortedApps) {
            try {
                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                val appName = packageInfo.applicationInfo?.loadLabel(packageManager)?.toString() ?: packageName
                val appIcon = packageInfo.applicationInfo?.loadIcon(packageManager)
                
                if (appIcon == null) {
                    continue
                }
                
                // 创建应用项布局
                val appItemLayout = android.widget.LinearLayout(this)
                appItemLayout.orientation = android.widget.LinearLayout.VERTICAL
                appItemLayout.gravity = android.view.Gravity.CENTER
                val layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(16, 0, 16, 0)
                appItemLayout.layoutParams = layoutParams
                
                // 创建应用图标
                val iconView = android.widget.ImageView(this)
                iconView.setImageDrawable(appIcon)
                val iconParams = android.widget.LinearLayout.LayoutParams(64, 64)
                iconParams.setMargins(0, 0, 0, 0)
                iconView.layoutParams = iconParams
                
                // 添加到应用项布局
                appItemLayout.addView(iconView)
                
                // 添加点击事件
                appItemLayout.setOnClickListener {
                    try {
                        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                        if (launchIntent != null) {
                            startActivity(launchIntent)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "启动应用失败", Toast.LENGTH_SHORT).show()
                    }
                }
                
                // 添加到容器
                commonAppsContainer.addView(appItemLayout)
            } catch (e: Exception) {
                continue
            }
        }
        
        // 为未使用的名额添加虚线框，总共最多6个
        val MAX_COMMON_APPS = 6
        val remainingSlots = MAX_COMMON_APPS - sortedApps.size
        if (remainingSlots > 0) {
            for (i in 0 until remainingSlots) {
                // 创建虚线框布局
                val emptySlotLayout = android.widget.LinearLayout(this)
                emptySlotLayout.orientation = android.widget.LinearLayout.VERTICAL
                emptySlotLayout.gravity = android.view.Gravity.CENTER
                val layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(16, 0, 16, 0)
                emptySlotLayout.layoutParams = layoutParams
                
                // 创建虚线框
                val borderView = android.view.View(this)
                val borderParams = android.widget.LinearLayout.LayoutParams(64, 64)
                borderParams.setMargins(0, 0, 0, 0)
                borderView.layoutParams = borderParams
                borderView.setBackgroundResource(R.drawable.dashed_border)
                
                emptySlotLayout.addView(borderView)
                commonAppsContainer.addView(emptySlotLayout)
            }
        }
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
                }
            }
        }
        return orders
    }
}
