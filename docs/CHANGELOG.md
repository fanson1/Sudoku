# 更新日志

所有重要变更均记录于此。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

---

## [1.0.0] - 2026-11-01

### 新增
- **完全离线架构**：移除所有网络权限、Ktor 客户端、登录/注册/排行榜/个人中心等联网功能
- **一次性买断 Pro**：Google Play Billing 9.1.0 实现，商品 ID `sudoku_pro_lifetime`，价格 ¥18 / $2.99
- **分级功能**：
  - 免费：关卡 1-10、笔记、撤销/重做、每关 3 次提示、浅色/深色/跟随系统主题
  - Pro：关卡 11-50、每日挑战、限时挑战（10 分钟）、无限提示、详细统计页、全部主题
- **应用内多语言切换**：支持 8 种语言 + "跟随系统"
  - English、简体中文、繁體中文、日本語、한국어、Español、Français、Deutsch
- **自研 i18n 方案**：`StringKey` 枚举 + `Strings` 类，运行时热切换，无需 Compose 资源
- **关卡系统**：50 关，5 个难度等级（简单、中等、困难、专家、大师）
- **每日挑战**：确定性种子生成，连续打卡天数统计
- **限时挑战**：10 分钟倒计时模式
- **智能辅助**：
  - 笔记（候选数）+ 自动擦除
  - 实时冲突高亮
  - 唯一候选格脉动提示
  - 无限撤销/重做
- **统计系统**：对局数、通关数、胜率、最高分、各难度最佳成绩、每日连续天数
- **Material 3 UI**：自适应棋盘、流畅动画、主题感知配色
- **本地持久化**：`multiplatform-settings` 适配 Android / iOS / Desktop / Web
- **发布就绪**：签名配置、ProGuard、数据安全表单、隐私政策、商店文案

### 变更
- `:server` 模块从构建中排除（源码保留在 `/server`，仅供参考）
- `settings.gradle.kts` 移除 `:server` include
- `AndroidManifest.xml` 精简为仅 `BILLING` 权限
- `theme/ThemeMode` 从带标签的 enum 改为纯 enum（语言包提供标签）

### 修复
- 移除所有硬编码中文字符串，统一走 i18n
- `GameViewModel` Pro 感知：`unlimitedHints`、`hintsRemaining` 逻辑

### 技术栈
- Kotlin 2.4.0 / Compose Multiplatform 1.7.x
- Gradle 9.x / AGP 8.x
- Google Play Billing 9.1.0
- JDK 21 (Amazon Corretto)

---

## [0.x] - 早期开发版本

### 0.9.0
- KMP 架构建立：core / shared / androidApp / desktopApp / webApp / server
- 数独生成/求解引擎（core）
- Compose UI 与 ViewModel（shared）
- Ktor 客户端 + 服端认证（已移除）

### 0.1.0
- 项目初始化