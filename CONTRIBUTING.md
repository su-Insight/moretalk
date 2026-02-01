# 贡献指南

感谢你对 OnePass 项目的关注！我们欢迎任何形式的贡献。

## 📋 目录

- [行为准则](#行为准则)
- [如何贡献](#如何贡献)
- [开发流程](#开发流程)
- [代码规范](#代码规范)
- [提交规范](#提交规范)
- [问题反馈](#问题反馈)

---

## 🤝 行为准则

- 尊重所有贡献者
- 欢迎不同观点和经验
- 优雅地接受建设性批评
- 关注对社区最有利的事情
- 对其他社区成员表示同理心

---

## 🚀 如何贡献

### 报告Bug

在提交Bug报告前，请确保：

1. **搜索现有Issue**
   - 检查是否已有相同问题的报告
   - 如果有，请在现有Issue中补充信息

2. **使用Bug报告模板**
   - 清晰描述问题
   - 提供复现步骤
   - 附上截图或录屏
   - 说明设备信息（型号、系统版本）
   - 提供日志信息

3. **示例Bug报告**

```markdown
**问题描述**
简要描述遇到的问题

**复现步骤**
1. 进入设置页面
2. 点击深色模式开关
3. 应用崩溃

**预期行为**
应用应该正常切换到深色模式

**实际行为**
应用崩溃并关闭

**设备信息**
- 设备: Xiaomi 12
- Android版本: 13
- 应用版本: 1.0.0

**日志**
```
崩溃日志...
```
```

### 提出新功能

1. **检查现有功能**
   - 确保功能尚未实现
   - 检查是否有相关讨论

2. **使用功能请求模板**
   - 清晰描述功能
   - 说明使用场景
   - 提供可能的实现方案

3. **示例功能请求**

```markdown
**功能描述**
添加联系人分组功能

**使用场景**
用户有大量联系人时，希望按类别分组管理

**建议方案**
1. 在联系人页面添加分组标签
2. 支持创建、编辑、删除分组
3. 支持将联系人分配到不同分组
```

### 提交代码

1. **Fork项目**
```bash
git fork https://github.com/yourusername/OnePass.git
```

2. **克隆你的Fork**
```bash
git clone https://github.com/yourusername/OnePass.git
cd OnePass
```

3. **创建新分支**
```bash
git checkout -b feature/your-feature-name
```

4. **进行更改**
   - 编写代码
   - 添加测试
   - 更新文档

5. **提交更改**
```bash
git add .
git commit -m "feat: add new feature"
```

6. **推送到你的Fork**
```bash
git push origin feature/your-feature-name
```

7. **创建Pull Request**
   - 前往你的Fork页面
   - 点击"New Pull Request"
   - 填写PR模板
   - 等待审核

---

## 🔄 开发流程

### 分支策略

- `main` - 主分支，稳定版本
- `develop` - 开发分支
- `feature/*` - 功能分支
- `bugfix/*` - 修复分支
- `hotfix/*` - 紧急修复分支

### 开发步骤

1. **从main创建功能分支**
```bash
git checkout main
git pull origin main
git checkout -b feature/your-feature
```

2. **开发和测试**
   - 编写代码
   - 运行单元测试
   - 进行集成测试

3. **提交代码**
```bash
git add .
git commit -m "type: description"
```

4. **推送并创建PR**
```bash
git push origin feature/your-feature
```

### Pull Request要求

- [ ] 代码通过编译
- [ ] 所有测试通过
- [ ] 添加了必要的测试
- [ ] 更新了相关文档
- [ ] 遵循代码规范
- [ ] PR描述清晰完整

---

## 📝 代码规范

### Kotlin代码规范

#### 命名规范

```kotlin
// 类名：大驼峰
class MainActivity
class ContactAdapter

// 函数名：小驼峰
fun initViews()
fun searchContacts()

// 变量名：小驼峰
val userName: String
var contactList: List<Contact>

// 常量：全大写下划线分隔
const val MAX_CONTACTS = 100
companion object {
    private const val TAG = "MainActivity"
}
```

#### 代码格式

```kotlin
// 使用4空格缩进
if (condition) {
    doSomething()
}

// 左大括号不换行
class MyClass {
    fun myFunction() {
        // ...
    }
}

// 单行if/when
val result = if (condition) true else false
```

#### 注释规范

```kotlin
/**
 * 联系人数据类
 * 
 * @property id 联系人ID
 * @property name 姓名
 * @property phoneNumber 手机号
 */
data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String
)

// 单行注释
// 初始化视图
private fun initViews() {
    // ...
}
```

### XML布局规范

```xml
<!-- 使用有意义的ID -->
android:id="@+id/textContactName"

<!-- 使用资源引用 -->
android:text="@string/contact_name"
android:textColor="@color/text_primary"

<!-- 添加注释说明布局用途 -->
<!-- 联系人列表项 -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
```

### Git提交规范

#### 提交信息格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### 类型(type)

- `feat`: 新功能
- `fix`: Bug修复
- `docs`: 文档更新
- `style`: 代码格式调整（不影响功能）
- `refactor`: 重构
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建/工具相关

#### 范围(scope)

- `main`: 主页面
- `contact`: 联系人模块
- `settings`: 设置模块
- `weather`: 天气模块
- `ui`: UI组件
- `utils`: 工具类

#### 示例

```bash
# 新功能
git commit -m "feat(contact): add search functionality"

# Bug修复
git commit -m "fix(weather): resolve crash when location is null"

# 文档更新
git commit -m "docs(readme): update installation guide"

# 重构
git commit -m "refactor(ui): extract common layout components"

# 性能优化
git commit -m "perf(image): optimize image loading"
```

---

## 🐛 问题反馈

### 报告问题前

1. **搜索现有Issue**
   - 使用关键词搜索
   - 检查是否已解决

2. **收集信息**
   - 设备型号和系统版本
   - 应用版本
   - 复现步骤
   - 错误日志
   - 截图或录屏

3. **创建Issue**
   - 使用合适的模板
   - 提供完整信息
   - 标记相关标签

### Issue标签

- `bug`: Bug报告
- `enhancement`: 功能增强
- `documentation`: 文档相关
- `good first issue`: 适合新手
- `help wanted`: 需要帮助
- `priority: high`: 高优先级
- `priority: medium`: 中优先级
- `priority: low`: 低优先级

---

## 📧 联系方式

- **邮箱**: support@onepass.com
- **GitHub**: [https://github.com/yourusername/OnePass](https://github.com/yourusername/OnePass)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/OnePass/discussions)

---

## 📚 相关资源

- [项目README](README.md)
- [许可证](LICENSE)
- [代码规范](https://kotlinlang.org/docs/coding-conventions.html)
- [Android开发指南](https://developer.android.com/guide)

---

<div align="center">

感谢你的贡献！🎉

</div>