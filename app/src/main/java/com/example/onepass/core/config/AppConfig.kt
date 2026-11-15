package com.example.onepass.core.config

object AppConfig {
    // Existing project key remains as a fallback until the complete key list is supplied.
    val DEFAULT_WEATHER_API_KEYS = listOf("16cb2331e187b30517a1ac580e0bc6fc")
    const val WEATHER_API_KEY_PREFS = "OnePassPrefs"
    const val CUSTOM_WEATHER_API_KEY = "custom_weather_api_key"
    const val CITY = "北京"
    const val LUNAR_API_BASE_URL = "https://timor.tech/api/"
}
