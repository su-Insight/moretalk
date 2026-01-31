package com.example.onepass

enum class WeChatActivity(val id: String) {
    INDEX("com.tencent.mm.ui.LauncherUI"),
    CHAT("com.tencent.mm.ui.chatting.ChattingUI"),
    SEARCH("com.tencent.mm.plugin.fts.ui.FTSMainUI"),
    DIALOG("com.tencent.mm.ui.widget.dialog"),
    DIALOG_OLD("yj4.o3")
}