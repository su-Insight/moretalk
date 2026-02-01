
<div align="center">

<img width="204" height="192" alt="image" src="https://github.com/user-attachments/assets/ab5fa028-83b1-47e7-8550-1936a822ce7a" />

**一款功能强大的Android桌面启动器与联系人管理应用**

[![Android API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=flat)](LICENSE)

</div>

---
# MoreTalk「墨谈」- 给长辈的极简通讯桌面

**MoreTalk（墨谈）** 是一款专门为老年用户研发的 Android 桌面启动器。它通过“减法”设计，移除了智能手机中繁琐的干扰项，将核心功能锁定在**亲情沟通**与**生活辅助**上。

借助 Android 无障碍辅助技术（Accessibility Service），MoreTalk 将复杂的微信视频/语音通话流程缩短为**一键直达**，解决长辈“找不到人”、“不会拨号”的痛点。

---

## ✨ 核心特性

* **🏠 极简大图标桌面**：锁定布局，超大应用图标，从源头防止误触和图标丢失。
* **👥 亲情一键达**：在联系人卡片上直接显示“视频通话”按钮，一触即发。
* **💬 自动化智慧拨号**：底层无障碍服务自动模拟点击微信菜单，长辈无需任何中间操作。
* **🌤️ 自动语音天气**：全后台自动定位刷新，支持实时语音播报，长辈不用看图也能知冷暖。
* **📅 大字农历日历**：首页醒目标注农历与阳历日期，贴合长辈生活习惯。


---

## 🛠️ 技术栈

* **开发语言**：Kotlin
* **系统要求**：Android 7.0 (API 24) 及以上
* **核心框架**：
    * `AccessibilityService`: 实现微信自动化点击。
    * `Coroutine + Flow`: 处理天气异步获取。
    * `Retrofit + GSON`: 驱动天气与农历数据接口。
    * `FusedLocationProvider`: 极简地理位置获取。

---

## 🚀 部署与安装说明

如果你是开发者或希望帮家人部署此应用，请参考以下步骤：
1.本地自行部署

### 1. 环境准备
* 安装 **Android Studio** (推荐 Jellyfish 或更高版本)。
* 准备一台 Android 7.0+ 的实体手机（无障碍服务在模拟器上可能表现不稳定）。

### 2. 获取代码与构建
```bash
# 克隆仓库
git clone [https://github.com/yourusername/MoreTalk.git](https://github.com/yourusername/MoreTalk.git)

# 进入目录
cd MoreTalk
```

* 在 Android Studio 中选择 **Open** 导入项目。
* 等待 Gradle 同步完成。
* 点击 **Run 'app'** 将应用安装至手机。

2.下载构建产物（）链接

### 3. 关键配置（家属必看）

为了保证应用能顺畅控制微信，请务必完成以下手动设置：

1.  **设为默认桌面**：在系统设置中将 MoreTalk 设置为“默认屏幕桌面”。(功能尚在开发)
2.  **开启无障碍权限**：进入 `系统设置 -> 无障碍 -> 已安装的服务`，找到 **MoreTalk 智能辅助** 并开启。
3.  **录入联系人**：在应用内添加联系人时，微信备注名必须与微信 App 里的备注完全一致（辅助规则为首个）。
4.  **开启定位**：确保应用拥有“始终允许”的定位权限，以便自动更新天气。

---

## 📁 项目结构

* `ui/activity/MainActivity.kt`：主页大图标布局与状态管理。
* `service/WechatAccessibilityService.kt`：无障碍自动化逻辑（核心模块）。
* `data/WeatherManager.kt`：全自动天气获取逻辑。
* `ui/adapter/ContactAdapter.kt`：大字版联系人列表适配。

---

## 🎨 UI & Logo 设计理念

* **极简主义**：全界面无阴影、无渐变，采用纯色块区分功能区。
* **高辨识度**：Logo 采用绿色气泡与白色心形组合，代表“微信沟通”与“关爱”。

![MoreTalk Final Logo](http://googleusercontent.com/image_generation_content/4<image-tokens-bardstorage://RESPONSE_DATA:124232465e58426d000649cb75143f5a092a5e789b346b22:97580075864430386b62699af3687e5f0:0:>)

---

### 💡 部署小提示：AndroidManifest.xml 关键配置

为了确保长辈在使用过程中能始终保持在 MoreTalk 的极简桌面，建议在 `AndroidManifest.xml` 中为 `MainActivity` 添加 `android:launchMode="singleInstance"` 配置。这样，无论长辈按“返回键”还是“主页键”，都能直接回到 MoreTalk 桌面，避免误入其他系统界面。(功能尚在开发)

**示例配置：**

```xml
<activity
    android:name=".ui.activity.MainActivity"
    android:launchMode="singleInstance"
    android:label="@string/app_name"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.HOME" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
```

---

## 🤝 贡献与反馈

<div align="center">

**如果这个项目对你有帮助，请给个 ⭐️ Star 支持一下！**
**如果您有更好的简化建议，欢迎提交 Pull Request。**

**MoreTalk - 科技不应成为长辈的门槛。**

</div>


