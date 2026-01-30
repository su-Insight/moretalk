package com.example.onepass.api

import com.example.onepass.model.AmapWeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("v3/weather/weatherInfo")
    fun getWeather(
        @Query("key") key: String,
        @Query("city") city: String,
        @Query("extensions") extensions: String = "base"
    ): Call<AmapWeatherResponse>
}
