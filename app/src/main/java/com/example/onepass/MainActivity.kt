package com.example.onepass

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.onepass.api.WeatherApi
import com.example.onepass.location.LocationManager
import com.example.onepass.model.AmapWeatherResponse
import com.example.onepass.model.LiveWeather

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
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
    private lateinit var dateText: TextView
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
    private var lastWeatherInfo = ""
    private var lunarDateText = ""

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocation || coarseLocation) {
            getLocationAndFetchWeather()
        } else {
            fetchWeather(currentCity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        locationManager = LocationManager(this)
        
        initTextToSpeech()
        initViews()
        updateDate()
        checkLocationPermissionAndFetchWeather()
        
        handler.postDelayed(refreshRunnable, 30 * 60 * 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(refreshRunnable)
        if (isTextToSpeechInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    override fun onResume() {
        super.onResume()
        updateDate()
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.CHINA
                isTextToSpeechInitialized = true
            }
        }
    }

    private fun initViews() {
        val weatherComponent = findViewById<View>(R.id.weatherCalendarComponent)
        dateText = weatherComponent.findViewById(R.id.dateText)
        weatherText = weatherComponent.findViewById(R.id.weatherText)
        temperatureText = weatherComponent.findViewById(R.id.temperatureText)
        weatherDetailText = weatherComponent.findViewById(R.id.weatherDetailText)
        locationText = weatherComponent.findViewById(R.id.locationText)
        settingsIcon = weatherComponent.findViewById(R.id.settingsIcon)
        weatherCard = weatherComponent as CardView

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
        isRefreshing = true
        startRefreshAnimation()
        
        if (currentCity == AppConfig.CITY) {
            getLocationAndFetchWeather(true)
        } else {
            fetchWeather(currentCity, true)
        }
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
        val animator = ObjectAnimator.ofFloat(weatherCard, "rotationY", 0f, 360f)
        animator.duration = 1000
        animator.start()
        
        weatherCard.tag = animator
    }

    private fun stopRefreshAnimation() {
        weatherCard.rotationY = 0f
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

        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED ||
            coarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            getLocationAndFetchWeather()
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
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
            dateText.text = "农历${lunarDate} $weekDay"
            lunarDateText = "农历${lunarDate}"
        } else {
            dateText.text = "公历${year}年${month}月${day}日 $weekDay"
            lunarDateText = "公历${year}年${month}月${day}日"
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
        weatherText.text = weather.weather
        temperatureText.text = "${weather.temperature}°C"
        weatherDetailText.text = "湿度: ${weather.humidity}% | 风力: ${weather.windpower} | 风向: ${weather.winddirection}"
        
        locationText.text = weather.city
    }

    private fun speakWeather(weather: LiveWeather) {
        if (!isTextToSpeechInitialized) {
            return
        }
        
        val speechText = "今天是$lunarDateText，${weather.city}的天气是${weather.weather}，气温${weather.temperature}摄氏度，湿度${weather.humidity}%，风力${weather.windpower}级，风向${weather.winddirection}"

        textToSpeech.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun showError() {
        weatherText.text = "获取失败"
        temperatureText.text = "--"
        weatherDetailText.text = "请检查网络连接"
    }
}