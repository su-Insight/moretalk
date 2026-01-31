package com.example.onepass

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.LocationManager as AndroidLocationManager
import android.provider.Settings
import android.graphics.drawable.Drawable
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import com.example.onepass.api.WeatherApi
import com.example.onepass.location.LocationManager
import com.example.onepass.model.AmapWeatherResponse
import com.example.onepass.model.LiveWeather

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import kotlin.math.max
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.nlf.calendar.Solar
import com.nlf.calendar.Lunar


class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "WeatherAPI"
    }
    private lateinit var dateTypeText: TextView
    private lateinit var dateText: TextView
    private lateinit var weekText: TextView
    private lateinit var weatherText: TextView
    private lateinit var temperatureText: TextView
    private lateinit var weatherDetailText: TextView
    private lateinit var locationText: TextView
    private lateinit var settingsIcon: ImageView
    private lateinit var weatherCard: CardView

    private lateinit var locationManager: LocationManager
    private lateinit var textToSpeech: TextToSpeech
    private var isTextToSpeechInitialized = false
    private var currentCity = AppConfig.CITY
    private var isRefreshing = false
    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            refreshWeather()
            handler.postDelayed(this, 300000)
        }
    }

    // 默认配置
    private val PREFS_NAME = "OnePassPrefs"
    private val KEY_DATE_STYLE = "date_style"
    private val VALUE_LUNAR = "lunar"
    private val VALUE_SOLAR = "solar"
    private val KEY_ICON_SIZE = "icon_size"
    private var lastWeatherInfo = ""
    private var lunarDateText = ""
    
    // 常用应用相关
    private val COMMON_APPS_PREFS = "common_apps_prefs"
    private val KEY_COMMON_APPS = "common_apps"
    private val KEY_APP_ORDERS = "app_orders"
    private lateinit var commonAppsCard: CardView
    private lateinit var commonAppTitle: TextView
    private lateinit var recyclerViewCommonApps: RecyclerView
    private val commonApps = mutableListOf<CommonApp>()
    private lateinit var commonAppsAdapter: CommonAppAdapter
    
    // 联系人相关
    private val CONTACTS_PREFS = "contacts_prefs"
    private val KEY_CONTACTS = "contacts"
    private lateinit var contactsCard: CardView
    private lateinit var contactTitle: TextView
    private lateinit var recyclerViewContacts: RecyclerView
    private lateinit var textNoContacts: TextView
    private val contacts = mutableListOf<Contact>()
    private lateinit var contactsAdapter: HomeContactAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        val camera = permissions[Manifest.permission.CAMERA] ?: false
        val readStorage = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
        val writeStorage = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false
        val callPhone = permissions[Manifest.permission.CALL_PHONE] ?: false
        
        if (fineLocation || coarseLocation) {
            getLocationAndFetchWeather()
        } else {
            fetchWeather(currentCity)
        }
        
        Log.d(TAG, "权限请求结果 - 位置: ${fineLocation || coarseLocation}, 相机: $camera, 存储: ${readStorage || writeStorage}, 电话: $callPhone")
        
        // 权限请求完成后，延迟初始化TextToSpeech
        handler.postDelayed({
            if (!isTextToSpeechInitialized) {
                Log.d(TAG, "开始延迟初始化TextToSpeech")
                initTextToSpeech()
            } else {
                Log.d(TAG, "TextToSpeech已经初始化，跳过")
            }
        }, 1000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate 开始")
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        locationManager = LocationManager(this)
        Log.d(TAG, "locationManager 初始化完成")
        
        // 不在onCreate中初始化TextToSpeech，而是在onResume中初始化
        Log.d(TAG, "准备在onResume中初始化TextToSpeech")
        
        initViews()
        updateDate()
        checkLocationPermissionAndFetchWeather()
        
        handler.postDelayed(refreshRunnable, 30 * 60 * 1000)
        Log.d(TAG, "onCreate 完成")
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(refreshRunnable)
        if (isTextToSpeechInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart 开始")
        
        // 在onStart中初始化TextToSpeech，而不是onResume
        if (!isTextToSpeechInitialized) {
            Log.d(TAG, "onStart中初始化TextToSpeech")
            initTextToSpeech()
        } else {
            Log.d(TAG, "TextToSpeech已经初始化")
        }
        
        Log.d(TAG, "onStart 完成")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume 开始")
        
        updateDate()
        loadCommonApps()
        loadContacts()
        
        Log.d(TAG, "onResume 完成")
    }

    private fun initTextToSpeech() {
        android.util.Log.i("TTS_DEBUG", "开始初始化TextToSpeech")
        try {
            textToSpeech = TextToSpeech(this) { status ->
                android.util.Log.i("TTS_DEBUG", "TextToSpeech初始化状态: $status")
                if (status == TextToSpeech.SUCCESS) {
                    android.util.Log.i("TTS_DEBUG", "TextToSpeech初始化成功，开始设置语言")
                    val result = textToSpeech.setLanguage(Locale.CHINA)
                    android.util.Log.i("TTS_DEBUG", "设置中文语言结果: $result")
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        android.util.Log.e("TTS_DEBUG", "不支持中文语音，结果: $result")
                        showTTSErrorDialog()
                    } else {
                        isTextToSpeechInitialized = true
                        android.util.Log.i("TTS_DEBUG", "语音播报初始化成功")
                        
                        try {
                            // 设置语音参数
                            textToSpeech.setSpeechRate(1.0f)
                            textToSpeech.setPitch(1.0f)
                            android.util.Log.i("TTS_DEBUG", "语音参数设置成功")
                            
                            // 设置播报监听器
                            textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                                override fun onStart(utteranceId: String?) {
                                    android.util.Log.i("TTS_DEBUG", "语音播报开始: $utteranceId")
                                }
                                
                                override fun onDone(utteranceId: String?) {
                                    android.util.Log.i("TTS_DEBUG", "语音播报完成: $utteranceId")
                                }
                                
                                override fun onError(utteranceId: String?) {
                                    android.util.Log.e("TTS_DEBUG", "语音播报错误: $utteranceId")
                                }
                                
                                override fun onStop(utteranceId: String?, interrupted: Boolean) {
                                    android.util.Log.i("TTS_DEBUG", "语音播报停止: $utteranceId, 中断: $interrupted")
                                }
                            })
                            android.util.Log.i("TTS_DEBUG", "播报监听器设置成功")
                        } catch (e: Exception) {
                            android.util.Log.e("TTS_DEBUG", "设置语音参数时出错: ${e.message}", e)
                        }
                    }
                } else {
                    android.util.Log.e("TTS_DEBUG", "语音播报初始化失败: $status")
                    android.util.Log.e("TTS_DEBUG", "尝试延迟重试...")
                    // 延迟重试
                    handler.postDelayed({
                        android.util.Log.i("TTS_DEBUG", "开始重试初始化TextToSpeech")
                        initTextToSpeechRetry()
                    }, 2000)
                    android.util.Log.i("TTS_DEBUG", "重试任务已安排")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("TTS_DEBUG", "创建TextToSpeech时出错: ${e.message}", e)
            android.util.Log.e("TTS_DEBUG", "尝试延迟重试...")
            // 延迟重试
            handler.postDelayed({
                android.util.Log.i("TTS_DEBUG", "开始重试初始化TextToSpeech")
                initTextToSpeechRetry()
            }, 2000)
            android.util.Log.i("TTS_DEBUG", "重试任务已安排")
        }
    }

    private fun initTextToSpeechRetry() {
        if (isTextToSpeechInitialized) {
            android.util.Log.i("TTS_DEBUG", "TextToSpeech已经初始化，跳过重试")
            return
        }
        
        android.util.Log.i("TTS_DEBUG", "重试初始化TextToSpeech")
        try {
            textToSpeech = TextToSpeech(this) { status ->
                android.util.Log.i("TTS_DEBUG", "重试 - TextToSpeech初始化状态: $status")
                if (status == TextToSpeech.SUCCESS) {
                    android.util.Log.i("TTS_DEBUG", "重试 - TextToSpeech初始化成功，开始设置语言")
                    val result = textToSpeech.setLanguage(Locale.CHINA)
                    android.util.Log.i("TTS_DEBUG", "重试 - 设置中文语言结果: $result")
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        android.util.Log.e("TTS_DEBUG", "重试 - 不支持中文语音，结果: $result")
                        // 暂时不显示错误对话框
                        // showTTSErrorDialog()
                    } else {
                        isTextToSpeechInitialized = true
                        android.util.Log.i("TTS_DEBUG", "重试 - 语音播报初始化成功")
                        
                        try {
                            textToSpeech.setSpeechRate(1.0f)
                            textToSpeech.setPitch(1.0f)
                            android.util.Log.i("TTS_DEBUG", "重试 - 语音参数设置成功")
                            
                            textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                                override fun onStart(utteranceId: String?) {
                                    android.util.Log.i("TTS_DEBUG", "语音播报开始: $utteranceId")
                                }
                                
                                override fun onDone(utteranceId: String?) {
                                    android.util.Log.i("TTS_DEBUG", "语音播报完成: $utteranceId")
                                }
                                
                                override fun onError(utteranceId: String?) {
                                    android.util.Log.e("TTS_DEBUG", "语音播报错误: $utteranceId")
                                }
                                
                                override fun onStop(utteranceId: String?, interrupted: Boolean) {
                                    android.util.Log.i("TTS_DEBUG", "语音播报停止: $utteranceId, 中断: $interrupted")
                                }
                            })
                            android.util.Log.i("TTS_DEBUG", "重试 - 播报监听器设置成功")
                        } catch (e: Exception) {
                            android.util.Log.e("TTS_DEBUG", "重试 - 设置语音参数时出错: ${e.message}", e)
                        }
                    }
                } else {
                    android.util.Log.e("TTS_DEBUG", "重试 - 语音播报初始化失败: $status")
                    // 暂时不显示错误对话框
                    // showTTSErrorDialog()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("TTS_DEBUG", "重试 - 创建TextToSpeech时出错: ${e.message}", e)
            // 暂时不显示错误对话框
            // showTTSErrorDialog()
        }
    }
    
    private fun showTTSErrorDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("语音播报不可用")
        builder.setMessage("您的设备没有安装语音播报引擎或不支持中文语音播报。")
        
        builder.setPositiveButton("安装语音引擎") { dialog, which ->
            // 跳转到Google Play商店搜索TTS应用
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = android.net.Uri.parse("market://search?q=Text+to+Speech&c=apps")
                startActivity(intent)
            } catch (e: Exception) {
                // 如果没有Google Play，跳转到系统设置
                val settingsIntent = Intent("com.android.settings.TTS_SETTINGS")
                startActivity(settingsIntent)
            }
            dialog.dismiss()
        }
        
        builder.setNegativeButton("稍后") { dialog, which ->
            dialog.dismiss()
        }
        
        builder.setCancelable(false)
        builder.show()
    }

    private fun initViews() {
        val weatherComponent = findViewById<View>(R.id.weatherCalendarComponent)
        dateTypeText = weatherComponent.findViewById(R.id.dateTypeText)
        dateText = weatherComponent.findViewById(R.id.dateText)
        weekText = weatherComponent.findViewById(R.id.weekText)
        weatherText = weatherComponent.findViewById(R.id.weatherText)
        temperatureText = weatherComponent.findViewById(R.id.temperatureText)
        weatherDetailText = weatherComponent.findViewById(R.id.weatherDetailText)
        locationText = weatherComponent.findViewById(R.id.locationText)
        settingsIcon = weatherComponent.findViewById(R.id.settingsIcon)
        weatherCard = weatherComponent as CardView

        // 初始化常用应用视图
        commonAppsCard = findViewById(R.id.commonAppsCard)
        commonAppTitle = commonAppsCard.findViewById(R.id.commonAppTitle)
        recyclerViewCommonApps = findViewById(R.id.recyclerViewCommonApps)
        
        // 设置常用应用RecyclerView
        commonAppsAdapter = CommonAppAdapter(commonApps) {
            packageName ->
            launchApp(packageName)
        }
        recyclerViewCommonApps.adapter = commonAppsAdapter
        
        // 设置默认的LayoutManager
        val defaultLayoutManager = GridLayoutManager(this, 3)
        recyclerViewCommonApps.layoutManager = defaultLayoutManager
        
        // 添加ItemDecoration来增加图标之间的间距
        recyclerViewCommonApps.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.right = 16 // 右侧间距
                outRect.bottom = 16 // 底部间距
            }
        })

        // 初始化联系人视图
        contactsCard = findViewById(R.id.contactsCard)
        contactTitle = contactsCard.findViewById(R.id.contactTitle)
        recyclerViewContacts = findViewById(R.id.recyclerViewContacts)
        textNoContacts = findViewById(R.id.textNoContacts)
        
        // 设置联系人RecyclerView为网格布局，每行2个
        val gridLayoutManager = GridLayoutManager(this, 2)
        recyclerViewContacts.layoutManager = gridLayoutManager
        contactsAdapter = HomeContactAdapter(contacts, object : HomeContactAdapter.OnContactClickListener {
            override fun onContactClick(contact: Contact) {
                showContactActionDialog(contact)
            }
        })
        recyclerViewContacts.adapter = contactsAdapter

        settingsIcon.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        weatherCard.setOnClickListener {
            refreshWeatherAndSpeak()
        }
    }

    private fun refreshWeatherAndSpeak() {
        if (isRefreshing) {
            Log.d(TAG, "正在刷新中，忽略点击")
            return
        }
        
        Log.d(TAG, "用户点击刷新天气（带播报）")
        
        // 检查定位服务是否开启
        if (!isLocationServiceEnabled()) {
            showLocationServiceDialog()
            return
        }
        
        // 检查位置权限
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        val locationGranted = fineLocationPermission == PackageManager.PERMISSION_GRANTED ||
            coarseLocationPermission == PackageManager.PERMISSION_GRANTED
        
        if (!locationGranted) {
            requestPermissions()
            return
        }
        
        isRefreshing = true
        startRefreshAnimation()
        
        if (currentCity == AppConfig.CITY) {
            getLocationAndFetchWeather(true)
        } else {
            fetchWeather(currentCity, true)
        }
    }
    
    private fun isLocationServiceEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager
        return locationManager.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(AndroidLocationManager.NETWORK_PROVIDER)
    }
    
    private fun showLocationServiceDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("定位服务未开启")
        builder.setMessage("刷新天气需要开启定位服务，请前往设置开启。")
        
        builder.setPositiveButton("前往设置") { dialog, which ->
            val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
            dialog.dismiss()
        }
        
        builder.setNegativeButton("取消") { dialog, which ->
            dialog.dismiss()
        }
        
        builder.setCancelable(false)
        builder.show()
    }

    private fun refreshWeather() {
        if (isRefreshing) {
            Log.d(TAG, "正在刷新中，忽略自动刷新")
            return
        }
        
        Log.d(TAG, "自动刷新天气")
        isRefreshing = true
        startRefreshAnimation()
        
        if (currentCity == AppConfig.CITY) {
            getLocationAndFetchWeather(false)
        } else {
            fetchWeather(currentCity, false)
        }
    }

    private fun startRefreshAnimation() {
        val animator = ObjectAnimator.ofFloat(weatherCard, "translationX", 0f, -10f, 10f, -10f, 10f, 0f)
        animator.duration = 500
        animator.start()
        
        weatherCard.tag = animator
    }

    private fun stopRefreshAnimation() {
        weatherCard.translationX = 0f
        isRefreshing = false
    }

    private fun checkLocationPermissionAndFetchWeather() {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val cameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )
        val readStoragePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val writeStoragePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val callPhonePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        )
        
        val locationGranted = fineLocationPermission == PackageManager.PERMISSION_GRANTED ||
            coarseLocationPermission == PackageManager.PERMISSION_GRANTED
        
        val otherPermissionsGranted = cameraPermission == PackageManager.PERMISSION_GRANTED &&
            readStoragePermission == PackageManager.PERMISSION_GRANTED &&
            writeStoragePermission == PackageManager.PERMISSION_GRANTED &&
            callPhonePermission == PackageManager.PERMISSION_GRANTED
        
        if (locationGranted && otherPermissionsGranted) {
            getLocationAndFetchWeather()
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CALL_PHONE
            )
        )
    }

    private fun getLocationAndFetchWeather(shouldSpeak: Boolean = false) {
        Log.d(TAG, "开始获取用户位置")
        locationManager.getCurrentLocation { city ->
            if (city != null) {
                Log.d(TAG, "定位成功，获取到城市: $city")
                currentCity = city
                fetchWeather(currentCity, shouldSpeak)
            } else {
                Log.w(TAG, "定位失败，使用默认城市: ${AppConfig.CITY}")
                fetchWeather(AppConfig.CITY, shouldSpeak)
            }
        }
    }

    private fun updateDate() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val dateStyle = prefs.getString(KEY_DATE_STYLE, VALUE_SOLAR)
        
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        val weekDays = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
        val weekDay = weekDays[dayOfWeek - 1]
        
        fetchDateFromNetwork(year, month, day, weekDay)
    }

    private fun fetchDateFromNetwork(year: Int, month: Int, day: Int, weekDay: String) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val dateStyle = prefs.getString(KEY_DATE_STYLE, VALUE_SOLAR)
        
        if (dateStyle == VALUE_LUNAR) {
            val solar = Solar.fromYmd(year, month, day)
            val lunar = solar.lunar
            val lunarDate = "${lunar.getYearInChinese()}年${lunar.getMonthInChinese()}月${lunar.getDayInChinese()}"
            dateTypeText.text = "农历"
            dateText.text = lunarDate
            weekText.text = weekDay
            lunarDateText = "农历${lunarDate}"
        } else {
            dateTypeText.text = "阳历"
            dateText.text = "${year}年${month}月${day}日"
            weekText.text = weekDay
            lunarDateText = "阳历${year}年${month}月${day}日"
        }
    }

    private fun fetchWeather(city: String, shouldSpeak: Boolean = false) {
        Log.d(TAG, "开始请求天气数据")
        Log.d(TAG, "请求城市: $city")
        Log.d(TAG, "API Key: ${AppConfig.API_KEY}")
        Log.d(TAG, "是否播报: $shouldSpeak")
        
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl("https://restapi.amap.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherApi = retrofit.create(WeatherApi::class.java)
        val call = weatherApi.getWeather(AppConfig.API_KEY, city)

        call.enqueue(object : Callback<AmapWeatherResponse> {
            override fun onResponse(call: Call<AmapWeatherResponse>, response: Response<AmapWeatherResponse>) {
                Log.d(TAG, "收到API响应")
                Log.d(TAG, "响应状态码: ${response.code()}")
                Log.d(TAG, "响应是否成功: ${response.isSuccessful}")
                
                if (response.isSuccessful && response.body() != null) {
                    val weatherResponse = response.body()!!
                    Log.d(TAG, "API状态: ${weatherResponse.status}")
                    Log.d(TAG, "API信息: ${weatherResponse.info}")
                    
                    if (weatherResponse.status == "1" && !weatherResponse.lives.isNullOrEmpty()) {
                        val weather = weatherResponse.lives[0]
                        Log.d(TAG, "天气数据获取成功")
                        Log.d(TAG, "省份: ${weather.province}")
                        Log.d(TAG, "城市: ${weather.city}")
                        Log.d(TAG, "天气状况: ${weather.weather}")
                        Log.d(TAG, "温度: ${weather.temperature}°C")
                        Log.d(TAG, "湿度: ${weather.humidity}%")
                        Log.d(TAG, "风向: ${weather.winddirection}")
                        Log.d(TAG, "风力: ${weather.windpower}")
                        
                        updateWeatherUI(weather)
                        if (shouldSpeak) {
                            speakWeather(weather)
                        }
                    } else {
                        Log.e(TAG, "天气数据获取失败")
                        Log.e(TAG, "API状态: ${weatherResponse.status}")
                        Log.e(TAG, "API信息: ${weatherResponse.info}")
                        showError()
                    }
                } else {
                    Log.e(TAG, "天气数据获取失败")
                    Log.e(TAG, "错误响应码: ${response.code()}")
                    Log.e(TAG, "错误消息: ${response.message()}")
                    showError()
                }
                stopRefreshAnimation()
            }

            override fun onFailure(call: Call<AmapWeatherResponse>, t: Throwable) {
                Log.e(TAG, "网络请求失败")
                Log.e(TAG, "错误信息: ${t.message}")
                Log.e(TAG, "错误堆栈: ", t)
                showError()
                stopRefreshAnimation()
            }
        })
    }

    private fun updateWeatherUI(weather: LiveWeather) {
        val weatherEmoji = getWeatherEmoji(weather.weather)
        weatherText.text = "$weatherEmoji ${weather.weather}"
        temperatureText.text = "${weather.temperature}°C"
        weatherDetailText.text = "湿度: ${weather.humidity}% | 风力: ${weather.windpower} | 风向: ${weather.winddirection}"
        
        locationText.text = weather.city
    }

    private fun getWeatherEmoji(weather: String): String {
        return when {
            weather.contains("晴") -> "☀️"
            weather.contains("多云") -> "⛅"
            weather.contains("阴") -> "☁️"
            weather.contains("暴雨") -> "⛈️"
            weather.contains("雷阵雨") -> "⛈️"
            weather.contains("大雨") -> "�️"
            weather.contains("中雨") -> "�️"
            weather.contains("小雨") -> "🌦️"
            weather.contains("雨") -> "🌧️"
            weather.contains("大雪") -> "❄️"
            weather.contains("中雪") -> "🌨️"
            weather.contains("小雪") -> "🌨️"
            weather.contains("雪") -> "❄️"
            weather.contains("雷") -> "⛈️"
            weather.contains("雾") -> "🌫️"
            weather.contains("霾") -> "😷"
            weather.contains("风") -> "🌬️"
            weather.contains("冰雹") -> "🌨️"
            else -> "🌤️"
        }
    }

    private fun speakWeather(weather: LiveWeather) {
        if (!isTextToSpeechInitialized) {
            return
        }
        
        // 检查设置中是否开启了天气播报
        val prefs = getSharedPreferences("OnePassPrefs", Context.MODE_PRIVATE)
        val weatherEnabled = prefs.getBoolean("weather_enabled", false)
        if (!weatherEnabled) {
            return
        }
        
        val speechText = "今天是$lunarDateText，${weather.city}的天气是${weather.weather}，气温${weather.temperature}摄氏度，湿度${weather.humidity}%，风力${weather.windpower}级，风向${weather.winddirection}"

        // 按照设置的声音比例播报（相对音量）
        val weatherVolume = prefs.getInt("weather_volume", 50)
        val volumeScale = weatherVolume / 100.0f
        
        // 使用Bundle设置音量参数
        val params = android.os.Bundle()
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volumeScale)

        textToSpeech.speak(speechText, TextToSpeech.QUEUE_FLUSH, params, null)
    }

    private fun showError() {
        weatherText.text = "获取失败"
        temperatureText.text = "--"
        weatherDetailText.text = "请检查网络连接"
    }

    private fun loadContacts() {
        Log.d(TAG, "开始加载联系人")
        
        // 从SharedPreferences加载联系人数据
        val prefs = getSharedPreferences(CONTACTS_PREFS, Context.MODE_PRIVATE)
        val contactsJson = prefs.getString(KEY_CONTACTS, null)
        
        if (contactsJson != null) {
            try {
                val gson = com.google.gson.Gson()
                val contactArray = gson.fromJson(contactsJson, Array<Contact>::class.java)
                contacts.clear()
                contacts.addAll(contactArray)
                Log.d(TAG, "成功加载 ${contacts.size} 个联系人")
            } catch (e: Exception) {
                Log.e(TAG, "解析联系人数据失败: ${e.message}", e)
                contacts.clear()
            }
        } else {
            Log.d(TAG, "没有找到联系人数据")
            contacts.clear()
        }
        
        // 根据宽度动态计算列数
        recyclerViewContacts.post {
            val recyclerViewWidth = recyclerViewContacts.width
            val originalImageSize = 400 // 与HomeContactAdapter中的原始大小保持一致
            val scaledImageSize = GlobalScaleManager.getScaledValue(this, originalImageSize)
            val contactItemWidth = scaledImageSize + 32 // 联系人项宽度 = 头像宽度 + 左右边距
            val spanCount = maxOf(1, recyclerViewWidth / contactItemWidth)
            val gridLayoutManager = GridLayoutManager(this, spanCount)
            recyclerViewContacts.layoutManager = gridLayoutManager
            contactsAdapter.notifyDataSetChanged()
        }
        
        // 更新UI
        if (contacts.isEmpty()) {
            contactsCard.visibility = View.GONE
        } else {
            contactsCard.visibility = View.VISIBLE
            contactsAdapter.notifyDataSetChanged()
        }
    }
    
    private fun showContactActionDialog(contact: Contact) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_contact_actions, null)
        
        val contactName = dialogView.findViewById<TextView>(R.id.contactName)
        val rowWechatVideo = dialogView.findViewById<LinearLayout>(R.id.rowWechatVideo)
        val rowWechatVoice = dialogView.findViewById<LinearLayout>(R.id.rowWechatVoice)
        val rowPhoneCall = dialogView.findViewById<LinearLayout>(R.id.rowPhoneCall)
        
        val btnWechatVideo = dialogView.findViewById<android.widget.Button>(R.id.btnWechatVideo)
        val btnWechatVoice = dialogView.findViewById<android.widget.Button>(R.id.btnWechatVoice)
        val btnPhoneCall = dialogView.findViewById<android.widget.Button>(R.id.btnPhoneCall)
        
        val btnPlayWechatVideo = dialogView.findViewById<android.widget.Button>(R.id.btnPlayWechatVideo)
        val btnPlayWechatVoice = dialogView.findViewById<android.widget.Button>(R.id.btnPlayWechatVoice)
        val btnPlayPhoneCall = dialogView.findViewById<android.widget.Button>(R.id.btnPlayPhoneCall)
        
        contactName.text = contact.name
        
        val builder = android.app.AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setCancelable(true)
        val dialog = builder.create()
        
        // 根据联系人功能显示对应按钮
        if (contact.hasWechatVideo) {
            rowWechatVideo.visibility = View.VISIBLE
            btnWechatVideo.setOnClickListener {
                openWechatVideo(contact)
                dialog.dismiss()
            }
            btnPlayWechatVideo.setOnClickListener {
                speakText("给${contact.name}拨打微信视频")
            }
        }
        
        if (contact.hasWechatVoice) {
            rowWechatVoice.visibility = View.VISIBLE
            btnWechatVoice.setOnClickListener {
                openWechatVoice(contact)
                dialog.dismiss()
            }
            btnPlayWechatVoice.setOnClickListener {
                speakText("给${contact.name}拨打微信语音")
            }
        }
        
        if (contact.hasPhoneCall) {
            rowPhoneCall.visibility = View.VISIBLE
            btnPhoneCall.setOnClickListener {
                makePhoneCall(contact)
                dialog.dismiss()
            }
            btnPlayPhoneCall.setOnClickListener {
                speakText("给${contact.name}拨打电话")
            }
        }
        
        dialog.show()
    }
    
    private fun speakText(text: String) {
        Log.d(TAG, "播报语音: $text")
        Log.d(TAG, "语音播报是否初始化: $isTextToSpeechInitialized")
        
        if (isTextToSpeechInitialized) {
            try {
                // 尝试不同的播报方式
                val result = textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                Log.d(TAG, "播报结果: $result")
                
                // 添加延迟，确保播报完成
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    Log.d(TAG, "播报延迟检查")
                }, 100)
            } catch (e: Exception) {
                Log.e(TAG, "播报失败", e)
                Toast.makeText(this, "语音播报失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "语音播报未初始化", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun openWechatVideo(contact: Contact) {
        Log.d(TAG, "发起微信视频通话: ${contact.wechatNote}")
        
        if (contact.wechatNote.isEmpty()) {
            Toast.makeText(this, "微信备注为空，无法打开微信", Toast.LENGTH_SHORT).show()
            return
        }
        
        val isEnabled = isAccessibilityServiceEnabled()
        Log.d(TAG, "无障碍服务是否启用: $isEnabled")
        
        if (!isEnabled) {
            showAccessibilityServiceDialog()
            return
        }
        
        try {
            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
            Log.d(TAG, "微信启动Intent: $intent")
            
            if (intent != null) {
                WeChatData.updateValue(contact.wechatNote)
                WeChatData.updateVideo(true)
                WeChatData.updateIndex(1)
                Log.d(TAG, "设置微信数据 - 昵称: ${contact.wechatNote}, 视频: true, 索引: 1")
                
                startActivity(intent)
                Toast.makeText(this, "正在发起微信视频通话", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "未安装微信", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "打开微信失败", e)
            Toast.makeText(this, "打开微信失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun openWechatVoice(contact: Contact) {
        Log.d(TAG, "发起微信语音通话: ${contact.wechatNote}")
        
        if (contact.wechatNote.isEmpty()) {
            Toast.makeText(this, "微信备注为空，无法打开微信", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!isAccessibilityServiceEnabled()) {
            showAccessibilityServiceDialog()
            return
        }
        
        try {
            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
            if (intent != null) {
                WeChatData.updateValue(contact.wechatNote)
                WeChatData.updateVideo(false)
                WeChatData.updateIndex(1)
                startActivity(intent)
                
                Toast.makeText(this, "正在发起微信语音通话", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "未安装微信", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "打开微信失败", e)
            Toast.makeText(this, "打开微信失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        val serviceName = "com.example.onepass/com.example.onepass.WechatAccessibilityService"
        val enabledServices = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        Log.d(TAG, "已启用的无障碍服务: $enabledServices")
        Log.d(TAG, "查找的服务名: $serviceName")
        val result = enabledServices?.contains(serviceName) == true
        Log.d(TAG, "检查结果: $result")
        return result
    }
    
    private fun showAccessibilityServiceDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("需要开启无障碍服务")
        builder.setMessage("为了自动拨打微信视频/语音通话，需要开启无障碍服务。是否现在前往设置？")
        builder.setPositiveButton("前往设置") { _, _ ->
            val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        builder.setNegativeButton("取消", null)
        builder.show()
    }
    
    private fun makePhoneCall(contact: Contact) {
        if (contact.phoneNumber.isEmpty()) {
            Toast.makeText(this, "手机号为空", Toast.LENGTH_SHORT).show()
            return
        }
        
        Log.d(TAG, "拨打电话: ${contact.phoneNumber}")
        try {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = android.net.Uri.parse("tel:${contact.phoneNumber}")
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "拨打电话失败", e)
            Toast.makeText(this, "拨打电话失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun launchApp(packageName: String) {
        Log.d(TAG, "启动应用: $packageName")
        
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            } else {
                Log.e(TAG, "无法启动应用: $packageName")
                Toast.makeText(this, "无法启动应用", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "启动应用失败: $packageName", e)
            Toast.makeText(this, "启动应用失败", Toast.LENGTH_SHORT).show()
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
                    // 忽略解析错误
                }
            }
        }
        return orders
    }

    private fun loadCommonApps() {
        Log.d(TAG, "开始加载常用应用")
        
        // 从 SharedPreferences 加载图标大小设置
        val iconSizeValue = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_ICON_SIZE, 80)
        // 将 0-100 的值映射到 100-240dp 的范围（增大图标大小），然后在缩小30%的基础上再缩小10%
        val baseIconSize = 100 + (iconSizeValue * 140 / 100) // 增加100%
        val originalIconSize = (baseIconSize * 0.7 * 0.9).toInt() // 缩小30%后再缩小10%
        // 使用GlobalScaleManager进行缩放
        val iconSize = GlobalScaleManager.getScaledValue(this, originalIconSize)
        val originalTextSize = (originalIconSize / 10).toFloat()
        val textSize = GlobalScaleManager.getScaledValue(this, originalTextSize)
        
        // 控制标题大小
        val originalTitleSize = 23f // 原始大小23sp
        val scaledTitleSize = GlobalScaleManager.getScaledValue(this, originalTitleSize)
        commonAppTitle.textSize = scaledTitleSize
        contactTitle.textSize = scaledTitleSize
        
        // 从 SharedPreferences 加载已保存的应用列表
        val savedApps = getSharedPreferences(COMMON_APPS_PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_COMMON_APPS, HashSet<String>()) ?: HashSet()
        
        // 从 SharedPreferences 加载应用排序信息
        val savedOrders = getSharedPreferences(COMMON_APPS_PREFS, Context.MODE_PRIVATE)
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
        
        // 清空常用应用列表
        commonApps.clear()
        
        if (savedApps.isEmpty()) {
            Log.d(TAG, "没有保存的常用应用，隐藏常用应用卡片")
            commonAppsCard.visibility = View.GONE
            val layoutParams = contactsCard.layoutParams as LinearLayout.LayoutParams
            layoutParams.topMargin = (16 * resources.displayMetrics.density).toInt()
            contactsCard.layoutParams = layoutParams
            commonAppsAdapter.notifyDataSetChanged()
            return
        }
        
        // 显示常用应用卡片
        commonAppsCard.visibility = View.VISIBLE
        val layoutParams = contactsCard.layoutParams as LinearLayout.LayoutParams
        layoutParams.topMargin = 0
        contactsCard.layoutParams = layoutParams
        
        // 按排序值对应用进行排序
        val sortedApps = savedApps.sortedWith(Comparator {
                app1, app2 ->
            val order1 = appOrders[app1] ?: Int.MAX_VALUE
            val order2 = appOrders[app2] ?: Int.MAX_VALUE
            order1.compareTo(order2)
        })
        
        Log.d(TAG, "常用应用排序完成: ${sortedApps.size} 个应用")
        
        // 加载应用信息
        for (packageName in sortedApps) {
            try {
                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                val appName = packageInfo.applicationInfo?.loadLabel(packageManager)?.toString() ?: packageName
                val appIcon = packageInfo.applicationInfo?.loadIcon(packageManager)
                
                if (appIcon != null) {
                    commonApps.add(CommonApp(packageName, appName, appIcon))
                    Log.d(TAG, "加载应用: $appName ($packageName)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载应用失败: $packageName", e)
            }
        }
        
        // 通知适配器数据变化
        commonAppsAdapter.notifyDataSetChanged()
        
        // 根据宽度动态计算列数，考虑额外的间距
        recyclerViewCommonApps.post {
            val recyclerViewWidth = recyclerViewCommonApps.width
            val originalImageSize = 128 // 原始大小128dp
            val scaledImageSize = GlobalScaleManager.getScaledValue(this, originalImageSize)
            val appItemWidth = scaledImageSize + 32 + 16 // 应用项宽度 = 图标宽度 + 左右边距 + ItemDecoration右侧间距
            val spanCount = maxOf(1, minOf(4, recyclerViewWidth / appItemWidth)) // 限制最大列数为4
            val gridLayoutManager = GridLayoutManager(this, spanCount)
            recyclerViewCommonApps.layoutManager = gridLayoutManager
            commonAppsAdapter.notifyDataSetChanged()
        }
        
        Log.d(TAG, "常用应用加载完成")
    }
}

data class CommonApp(val packageName: String, val appName: String, val appIcon: Drawable)

class CommonAppAdapter(private val apps: List<CommonApp>, private val listener: (String) -> Unit) : RecyclerView.Adapter<CommonAppAdapter.ViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_common_app, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.bind(app, listener)
    }
    
    override fun getItemCount(): Int = apps.size
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.appIcon)
        private val nameView: TextView = itemView.findViewById(R.id.appName)
        
        fun bind(app: CommonApp, listener: (String) -> Unit) {
            iconView.setImageDrawable(app.appIcon)
            nameView.text = app.appName
            
            // 从 SharedPreferences 加载图标大小设置
            val iconSizeValue = itemView.context.getSharedPreferences("OnePassPrefs", Context.MODE_PRIVATE).getInt("icon_size", 80)
            // 将 0-100 的值映射到 100-240dp 的范围（增大图标大小），然后在缩小30%的基础上再缩小10%
            val baseIconSize = 100 + (iconSizeValue * 140 / 100) // 增加100%
            val originalIconSize = (baseIconSize * 0.7 * 0.9).toInt() // 缩小30%后再缩小10%
            // 使用GlobalScaleManager进行缩放
            val scaledIconSize = GlobalScaleManager.getScaledValue(itemView.context, originalIconSize)
            val iconParams = iconView.layoutParams
            iconParams.width = scaledIconSize
            iconParams.height = scaledIconSize
            iconView.layoutParams = iconParams
            
            val originalTextSize = (originalIconSize / 10).toFloat()
            val scaledTextSize = GlobalScaleManager.getScaledValue(itemView.context, originalTextSize)
            nameView.textSize = scaledTextSize
            
            itemView.setOnClickListener {
                listener(app.packageName)
            }
        }
    }
}