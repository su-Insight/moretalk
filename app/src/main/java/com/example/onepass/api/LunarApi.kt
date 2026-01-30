package com.example.onepass.api

import retrofit2.http.GET
import retrofit2.http.Query

interface LunarApi {
    @GET("v3/lunar/calendar")
    suspend fun getLunarDate(
        @Query("date") date: String
    ): LunarResponse
}

data class LunarResponse(
    val code: Int,
    val data: LunarData
)

data class LunarData(
    val solarYear: Int,
    val solarMonth: Int,
    val solarDay: Int,
    val lunarYear: Int,
    val lunarMonth: Int,
    val lunarDay: Int,
    val lunarMonthName: String,
    val lunarDayName: String,
    val isLeap: Boolean
)
