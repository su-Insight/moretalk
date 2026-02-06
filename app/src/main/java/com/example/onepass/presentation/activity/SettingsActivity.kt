package com.example.onepass.presentation.activity

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
import com.example.onepass.R
import com.example.onepass.core.config.GlobalScaleManager

class SettingsActivity : AppCompatActivity() {

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
    
    // 标签文本控件
    private lateinit var textDateStyle: TextView
    private lateinit var textCommonAppsTitle: TextView
    private lateinit var textContactsTitle: TextView
    private lateinit var textIconSizeTitle: TextView
    private lateinit var textWeatherTitle: TextView

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
    }

    override fun onResume() {
        super.onResume()
        val commonAppsSet = getSharedPreferences(COMMON_APPS_PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_COMMON_APPS, HashSet<String>())
        loadCommonApps(commonAppsSet)
        
        // 应用最新的缩放设置
        val scalePercentage = GlobalScaleManager.getScalePercentage(this)
        applyScaleEffects(scalePercentage)
    }

    private fun initViews() {
        radioLunar = findViewById(R.id.radioLunar)
        radioSolar = findViewById(R.id.radioSolar)
        dateStyleGroup = findViewById(R.id.dateStyleGroup)
        seekBarIconSize = findViewById(R.id.seekBarIconSize)
        textIconSize = findViewById(R.id.textIconSize)
        btnSetDefaultLauncher = findViewById(R.id.btnSetDefaultLauncher)
        
        // 设置缩放比例进度条的范围
        seekBarIconSize.min = 60
        seekBarIconSize.max = 100

        // new controls
        btnCommonApps = findViewById(R.id.btnCommonApps)
        switchWeather = findViewById(R.id.switchWeather)
        seekBarWeatherVol = findViewById(R.id.seekBarWeatherVol)
        textWeatherVol = findViewById(R.id.textWeatherVol)
        commonAppsScrollView = findViewById(R.id.commonAppsScrollView)
        commonAppsContainer = findViewById(R.id.commonAppsContainer)
        textNoCommonApps = findViewById(R.id.textNoCommonApps)
        btnContacts = findViewById(R.id.btnContacts)
        
        // 标签文本控件
        textDateStyle = findViewById(R.id.textDateStyle)
        radioLunar = findViewById(R.id.radioLunar)
        radioSolar = findViewById(R.id.radioSolar)
        textCommonAppsTitle = findViewById(R.id.textCommonAppsTitle)
        textContactsTitle = findViewById(R.id.textContactsTitle)
        textIconSizeTitle = findViewById(R.id.textIconSizeTitle)
        textWeatherTitle = findViewById(R.id.textWeatherTitle)
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val dateStyle = prefs.getString(KEY_DATE_STYLE, VALUE_SOLAR)
        if (dateStyle == VALUE_LUNAR) {
            radioLunar.isChecked = true
        } else {
            radioSolar.isChecked = true
        }

        val weatherEnabled = prefs.getBoolean(KEY_WEATHER_ENABLED, true)
        switchWeather.isChecked = weatherEnabled
        val vol = prefs.getInt(KEY_WEATHER_VOLUME, 50)
        seekBarWeatherVol.progress = vol
        textWeatherVol.text = vol.toString() + "%"

        seekBarWeatherVol.isEnabled = weatherEnabled

        val scalePercentage = GlobalScaleManager.getScalePercentage(this)
        seekBarIconSize.progress = scalePercentage
        textIconSize.text = scalePercentage.toString() + "%"
        
        // 应用缩放效果
        applyScaleEffects(scalePercentage)

        val commonAppsSet = prefs.getStringSet(KEY_COMMON_APPS, HashSet<String>())
        loadCommonApps(commonAppsSet)
    }

    private fun setupListeners() {
        dateStyleGroup.setOnCheckedChangeListener { _, checkedId ->
            val isLunar = checkedId == R.id.radioLunar
            val editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            editor.putString(KEY_DATE_STYLE, if (isLunar) VALUE_LUNAR else VALUE_SOLAR)
            editor.apply()
            Toast.makeText(this, if (isLunar) "已选择农历" else "已选择阳历", Toast.LENGTH_SHORT).show()
        }

        seekBarIconSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val clampedProgress = progress.coerceIn(60, 100)
                textIconSize.text = clampedProgress.toString() + "%"
                // 实时应用缩放效果
                applyScaleEffects(clampedProgress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val clampedProgress = seekBarIconSize.progress.coerceIn(60, 100)
                GlobalScaleManager.setScalePercentage(this@SettingsActivity, clampedProgress)
                Toast.makeText(this@SettingsActivity, "缩放比例已设置为 " + clampedProgress + "%", Toast.LENGTH_SHORT).show()
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
                val originalAppIconSize = 120 // 原始大小120dp
                val scaledAppIconSize = GlobalScaleManager.getScaledValue(this, originalAppIconSize)
                val iconParams = android.widget.LinearLayout.LayoutParams(scaledAppIconSize, scaledAppIconSize)
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
    
    private fun applyScaleEffects(scalePercentage: Int) {
        // 缩放字体大小
        val originalTitleSize = 27f // 标题原始大小27sp
        val originalOptionSize = 24f // 选项原始大小24sp
        val originalButtonSize = 20f // 按钮原始大小20sp

        val scaledTitleSize = GlobalScaleManager.getScaledValue(this, originalTitleSize)
        val scaledOptionSize = GlobalScaleManager.getScaledValue(this, originalOptionSize)
        val scaledButtonSize = GlobalScaleManager.getScaledValue(this, originalButtonSize)
        
        // 选项文本
        radioLunar.textSize = scaledOptionSize
        radioSolar.textSize = scaledOptionSize
        textIconSize.textSize = scaledOptionSize
        textWeatherVol.textSize = scaledOptionSize
        textNoCommonApps.textSize = scaledOptionSize
        
        // 按钮文本
        btnSetDefaultLauncher.textSize = scaledOptionSize
        btnCommonApps.textSize = scaledButtonSize
        btnContacts.textSize = scaledButtonSize
        
        // 缩放常用应用图标
        val originalAppIconSize = 120 // 原始大小120dp
        val scaledAppIconSize = GlobalScaleManager.getScaledValue(this, originalAppIconSize)
        
        for (i in 0 until commonAppsContainer.childCount) {
            val childView = commonAppsContainer.getChildAt(i)
            if (childView is android.widget.LinearLayout && childView.childCount > 0) {
                val iconView = childView.getChildAt(0)
                if (iconView is android.widget.ImageView) {
                    val iconParams = iconView.layoutParams
                    iconParams.width = scaledAppIconSize
                    iconParams.height = scaledAppIconSize
                    iconView.layoutParams = iconParams
                }
            }
        }
    }
}
