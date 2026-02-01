# 更新日志 (Changelog)

本文档记录OnePass项目的所有重要更改。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

---

## [Unreleased]

### 计划中
- 联系人分组功能
- 云同步功能
- 备份与恢复
- 主题自定义
- 手势操作
- 桌面小部件
- 性能优化
- 多语言支持

---

## [1.0.0] - 2024-02-02

### 新增 (Added)
- ✨ 完整的桌面启动器功能
- ✨ 联系人管理系统（增删改查）
- ✨ 实时天气查询与播报
- ✨ 农历/阳历日期显示
- ✨ 常用应用管理
- ✨ 联系人智能搜索
- ✨ 深色模式完整支持
- ✨ 微信跳转协议集成（扫一扫、付款码）
- ✨ 联系人快速操作（微信视频、微信语音、拨打电话）
- ✨ 应用图标大小调整
- ✨ 天气播报音量控制
- ✨ 联系人头像管理
- ✨ 联系人导入/导出功能
- ✨ 优化的搜索对话框UI
- ✨ 优化的搜索结果卡片UI

### 优化 (Changed)
- 🎨 重新设计联系人搜索界面
- 🎨 优化搜索结果项布局
- 🎨 改进对话框UI设计
- 🎨 统一应用配色方案
- 🎨 完善深色模式适配
- ⚡ 提升应用启动速度
- ⚡ 优化图片加载性能
- ⚡ 改进RecyclerView滚动性能

### 修复 (Fixed)
- 🐛 修复深色模式颜色资源缺失问题
- 🐛 修复搜索对话框类型不匹配错误
- 🐛 修复联系人头像显示问题
- 🐛 修复天气播报权限问题

### 技术细节
- 📦 添加ThemeManager主题管理类
- 📦 实现RecyclerView.Adapter优化
- 📦 添加完整的深色模式资源
- 📦 优化布局文件结构
- 📦 改进错误处理机制

---

## 版本说明

### 版本号格式
- **主版本号**: 不兼容的API修改
- **次版本号**: 向下兼容的功能性新增
- **修订号**: 向下兼容的问题修正

### 更新类型
- **新增 (Added)**: 新功能
- **变更 (Changed)**: 功能变更
- **弃用 (Deprecated)**: 即将移除的功能
- **移除 (Removed)**: 已移除的功能
- **修复 (Fixed)**: Bug修复
- **安全 (Security)**: 安全相关修复

---

## [0.1.0] - 2024-01-XX

### 新增 (Added)
- ✨ 项目初始化
- ✨ 基础UI框架
- ✨ AndroidX依赖集成
- ✨ Material Design主题

---

[Unreleased]: https://github.com/yourusername/OnePass/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/yourusername/OnePass/releases/tag/v1.0.0
[0.1.0]: https://github.com/yourusername/OnePass/releases/tag/v0.1.0