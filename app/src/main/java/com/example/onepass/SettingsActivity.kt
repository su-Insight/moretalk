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

    private var isLoggedIn = false
    private val PREFS_NAME = "OnePassPrefs"
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
        val display = if (!commonAppsSet.isNullOrEmpty()) commonAppsSet!!.joinToString(", ") else "未选择"
        btnCommonApps.text = "常用应用: ${display}"
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
            Toast.makeText(this, if (isLunar) "已选择农历" else "已选择公历", Toast.LENGTH_SHORT).show()
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
}
