package com.example.onepass.domain.model

object WeChatData {
    var value: String = ""
    fun updateValue(newValue: String) {
        value = newValue
    }

    var index: Int = 0
    fun updateIndex(newValue: Int) {
        index = newValue
    }

    var video: Boolean = true
    fun updateVideo(newValue: Boolean) {
        video = newValue
    }

    var isEnglish: Boolean = false
    fun updateLanguage(isEnglish: Boolean) {
        this.isEnglish = isEnglish
    }

    fun findText(options: Boolean): String {
        return if (video || !options) {
            if (isEnglish) "Video Call" else "视频通话"
        } else {
            if (isEnglish) "Voice Call" else "语音通话"
        }
    }
}