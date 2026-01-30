package com.example.onepass.data.source.mock

import com.google.gson.Gson
import com.example.onepass.data.model.AppInfo
import com.example.onepass.data.source.StaticAppsDataSource

class MockStaticAppsDataSource : StaticAppsDataSource {
    override suspend fun fetchAvailableApps(userId: String): List<AppInfo> {
        // JSON 结构中只有 packageName 和 name 字段，与 AppInfo 匹配
        data class JsonApp(val packageName: String, val name: String)
        val json = """
        [
          {"packageName":"com.tencent.mm","name":"微信"},
          {"packageName":"com.google.android.youtube","name":"YouTube"},
          {"packageName":"com.instagram.android","name":"Instagram"},
          {"packageName":"com.spotify.music","name":"Spotify"},
          {"packageName":"com.facebook.katana","name":"Facebook"},
          {"packageName":"com.netflix.mediaclient","name":"Netflix"},
          {"packageName":"com.google.android.calendar","name":"日历"},
          {"packageName":"com.google.android.maps","name":"Google 地图"},
          {"packageName":"com.baidu.searchbox","name":"百度"},
          {"packageName":"com.android.settings","name":"设置"}
        ]
        """.trimIndent()
        val items = Gson().fromJson(json, Array<JsonApp>::class.java)
        return items.map { AppInfo(it.packageName, it.name, null, true, 0, 0L, 0L) }
    }
}
