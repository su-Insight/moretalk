# OnePass

<div align="center">

![OnePass Logo](app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp)

**一款功能强大的Android桌面启动器与联系人管理应用**

[![Android API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=flat)](LICENSE)

</div>

---

## 📱 项目简介

OnePass 是一款集成了桌面启动器、联系人管理、天气播报、常用应用管理等多功能于一体的Android应用。它旨在为用户提供一个简洁、高效、美观的移动体验。

### 核心特性

- 🏠 **桌面启动器** - 自定义桌面，快速访问常用应用
- 👥 **联系人管理** - 完整的联系人增删改查功能
- 🌤️ **天气播报** - 实时天气查询与语音播报
- 🌙 **深色模式** - 完整的深色主题支持
- 🔍 **智能搜索** - 快速搜索联系人
- 💬 **微信集成** - 支持微信跳转协议
- 📅 **农历支持** - 农历/阳历日期显示
- ♿ **无障碍服务** - 辅助功能支持

---

## ✨ 功能特性

### 1. 桌面启动器
- 自定义桌面布局
- 常用应用快捷方式
- 应用网格管理
- 应用搜索功能

### 2. 联系人管理
- 添加/编辑/删除联系人
- 联系人头像管理
- 联系人信息展示
- 智能搜索功能
- 联系人快速操作（微信视频、微信语音、拨打电话）

### 3. 天气服务
- 实时天气查询
- 位置自动获取
- 天气语音播报
- 天气详情展示

### 4. 设置中心
- 深色模式切换
- 日期样式选择（农历/阳历）
- 图标大小调整
- 天气播报设置
- 常用应用管理

### 5. 主题系统
- 浅色/深色主题
- 自动主题切换
- 统一的颜色方案
- 完整的深色模式适配

---

## 🛠️ 技术栈

### 核心技术
- **语言**: Kotlin
- **最低SDK**: API 24 (Android 7.0)
- **目标SDK**: API 36 (Android 14)

### 主要依赖库
```kotlin
// AndroidX
androidx.core.ktx
androidx.appcompat
androidx.material
androidx.constraintlayout
androidx.recyclerview

// 网络请求
retrofit:2.9.0
converter-gson:2.9.0

// JSON处理
gson:2.10.1
kotlinx-serialization-json:1.6.3

// 位置服务
play-services-location:21.1.0

// 农历计算
lunar-1.7.7.jar
```

### 架构模式
- MVVM架构
- Repository模式
- 适配器模式
- 单例模式

---

## 📦 安装说明

### 前置要求
- Android 7.0 (API 24) 或更高版本
- 50MB 可用存储空间
- 位置权限（用于天气功能）
- 无障碍服务权限（用于微信集成）

### 安装步骤

1. **克隆仓库**
```bash
git clone https://github.com/yourusername/OnePass.git
cd OnePass
```

2. **使用Android Studio打开**
   - 打开Android Studio
   - 选择 "Open an Existing Project"
   - 选择克隆的项目目录

3. **同步Gradle**
   - 等待Gradle同步完成
   - 如有依赖下载，请耐心等待

4. **构建项目**
```bash
# 使用Gradle构建
./gradlew assembleDebug

# 或在Android Studio中点击 Run 按钮
```

5. **安装到设备**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 🎯 使用说明

### 首次使用

1. **启动应用**
   - 首次启动会请求必要权限
   - 授予位置权限以使用天气功能

2. **设置桌面**
   - 长按桌面空白处可添加应用
   - 进入设置管理常用应用

3. **添加联系人**
   - 点击"添加联系人"按钮
   - 填写联系人信息
   - 选择头像图片
   - 保存联系人

4. **使用天气功能**
   - 点击天气卡片刷新天气
   - 开启天气播报自动播报
   - 调整播报音量

### 主要功能操作

#### 联系人搜索
1. 进入联系人页面
2. 点击搜索按钮
3. 输入关键词（姓名、微信备注、手机号）
4. 查看搜索结果
5. 点击结果进入详情页

#### 微信功能
1. 在首页找到"微信功能测试"区域
2. 点击"微信扫一扫"打开微信扫一扫
3. 点击"微信付款码"打开微信付款码

#### 深色模式
1. 进入设置页面
2. 找到"深色模式"开关
3. 切换开关启用/禁用深色模式

---

## 📁 项目结构

```
OnePass/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/onepass/
│   │   │   │   ├── api/                    # API接口
│   │   │   │   │   ├── LunarApi.kt
│   │   │   │   │   └── WeatherApi.kt
│   │   │   │   ├── data/                   # 数据层
│   │   │   │   │   ├── model/
│   │   │   │   │   ├── repository/
│   │   │   │   │   └── source/
│   │   │   │   ├── location/                # 位置服务
│   │   │   │   │   └── LocationManager.kt
│   │   │   │   ├── model/                  # 数据模型
│   │   │   │   │   ├── Contact.kt
│   │   │   │   │   ├── WeatherResponse.kt
│   │   │   │   │   └── ...
│   │   │   │   ├── ui/                     # UI组件
│   │   │   │   │   └── activity/
│   │   │   │   ├── utils/                  # 工具类
│   │   │   │   │   ├── ImageUtils.kt
│   │   │   │   │   ├── PerformanceUtils.kt
│   │   │   │   │   └── Logger.kt
│   │   │   │   ├── MainActivity.kt          # 主页面
│   │   │   │   ├── ContactsActivity.kt      # 联系人页面
│   │   │   │   ├── AddContactActivity.kt    # 添加联系人
│   │   │   │   ├── SettingsActivity.kt      # 设置页面
│   │   │   │   └── ...
│   │   │   ├── res/                       # 资源文件
│   │   │   │   ├── layout/                # 布局文件
│   │   │   │   ├── drawable/              # 图片资源
│   │   │   │   ├── values/                # 值资源
│   │   │   │   └── values-night/          # 深色模式资源
│   │   │   └── AndroidManifest.xml        # 应用清单
│   │   ├── androidTest/                   # 集成测试
│   │   └── test/                         # 单元测试
│   ├── build.gradle.kts                    # 应用级构建配置
│   └── proguard-rules.pro                # 混淆规则
├── gradle/                               # Gradle配置
├── build.gradle.kts                       # 项目级构建配置
├── settings.gradle.kts                     # Gradle设置
└── README.md                             # 项目文档
```

---

## 🔧 核心模块说明

### MainActivity
- **功能**: 主页面，展示天气、常用应用、联系人
- **特性**: 
  - 天气日历组件
  - 常用应用网格
  - 联系人列表
  - 微信功能测试入口

### ContactsActivity
- **功能**: 联系人管理页面
- **特性**:
  - 联系人列表展示
  - 搜索功能
  - 添加/导入/导出联系人
  - 联系人详情查看

### AddContactActivity
- **功能**: 添加/编辑联系人
- **特性**:
  - 联系人信息编辑
  - 头像选择
  - 功能开关（微信视频、语音、电话）
  - 数据保存与删除

### SettingsActivity
- **功能**: 应用设置中心
- **特性**:
  - 深色模式切换
  - 日期样式选择
  - 图标大小调整
  - 天气播报设置
  - 常用应用管理

### ThemeManager
- **功能**: 主题管理
- **特性**:
  - 深色模式切换
  - 主题持久化
  - 自动主题应用

---

## 🎨 UI设计

### 设计原则
- **简洁**: 界面简洁，操作直观
- **一致**: 统一的设计语言和交互模式
- **响应式**: 适配不同屏幕尺寸
- **可访问**: 支持无障碍服务

### 主题配色

#### 浅色模式
```xml
<color name="background">#F8FAFC</color>
<color name="background_card">#FFFFFF</color>
<color name="text_primary">#1E293B</color>
<color name="text_secondary">#475569</color>
```

#### 深色模式
```xml
<color name="background">#0F172A</color>
<color name="background_card">#1E293B</color>
<color name="text_primary">#F1F5F9</color>
<color name="text_secondary">#CBD5E1</color>
```

---

## 📸 功能截图

> 注：此处应添加应用截图

### 主页面
- 天气日历组件
- 常用应用网格
- 联系人列表

### 联系人页面
- 联系人卡片展示
- 搜索功能
- 快速操作按钮

### 设置页面
- 深色模式开关
- 日期样式选择
- 图标大小调整

---

## 🚀 开发计划

### 已完成 ✅
- [x] 基础桌面启动器功能
- [x] 联系人管理系统
- [x] 天气查询与播报
- [x] 深色模式支持
- [x] 联系人搜索功能
- [x] 微信跳转协议集成

### 进行中 🚧
- [ ] 联系人分组功能
- [ ] 云同步功能
- [ ] 备份与恢复

### 计划中 📋
- [ ] 主题自定义
- [ ] 手势操作
- [ ] 桌面小部件
- [ ] 性能优化
- [ ] 多语言支持

---

## 🤝 贡献指南

我们欢迎任何形式的贡献！

### 如何贡献

1. **Fork项目**
```bash
git fork https://github.com/yourusername/OnePass.git
```

2. **创建特性分支**
```bash
git checkout -b feature/AmazingFeature
```

3. **提交更改**
```bash
git commit -m 'Add some AmazingFeature'
```

4. **推送到分支**
```bash
git push origin feature/AmazingFeature
```

5. **提交Pull Request**

### 代码规范
- 遵循Kotlin代码规范
- 添加必要的注释
- 确保代码通过编译
- 运行测试确保功能正常

### 提交信息格式
```
<type>: <subject>

<body>

<footer>
```

类型:
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具相关

---

## 📝 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

```
MIT License

Copyright (c) 2024 OnePass

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 📞 联系方式

- **作者**: OnePass Team
- **邮箱**: support@onepass.com
- **GitHub**: [https://github.com/yourusername/OnePass](https://github.com/yourusername/OnePass)
- **问题反馈**: [GitHub Issues](https://github.com/yourusername/OnePass/issues)

---

## 🙏 致谢

感谢以下开源项目和服务：

- [Android Open Source Project](https://source.android.com/)
- [Kotlin](https://kotlinlang.org/)
- [Retrofit](https://square.github.io/retrofit/)
- [Material Design](https://material.io/)

---

## 📊 项目统计

![GitHub stars](https://img.shields.io/github/stars/yourusername/OnePass?style=social)
![GitHub forks](https://img.shields.io/github/forks/yourusername/OnePass?style=social)
![GitHub issues](https://img.shields.io/github/issues/yourusername/OnePass)
![GitHub license](https://img.shields.io/github/license/yourusername/OnePass)

---

<div align="center">

**如果这个项目对你有帮助，请给个 ⭐️ Star 支持一下！**

Made with ❤️ by OnePass Team

</div>