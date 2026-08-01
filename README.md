# Sudoku KMP - 全平台数独游戏

这是一个基于 Kotlin Multiplatform (KMP) 开发的全平台数独游戏，支持 Android、Desktop (JVM)、Web (Wasm) 以及 Server (Ktor)。该项目展示了如何在不同平台间共享核心业务逻辑、UI 组件以及实现联网模式。

## 🚀 项目特性

- **多平台支持**：共享核心逻辑与 UI。
  - **Android**: 原生应用体验。
  - **Desktop**: 跨平台桌面支持。
  - **Web (Wasm)**: 高性能 Web 体验。
- **数独引擎**：
  - 自研生成算法，支持不同难度级别（EASY, MEDIUM, HARD, EXPERT, MASTER）。
  - 自动解题与合法性校验。
  - 实时检查模式与笔记（Notes）功能。
- **联网模式**：
  - 用户注册与登录（支持 BCrypt 密码加密）。
  - 全球排行榜，实时同步玩家进度与分数。
  - 基于 JWT 的安全身份验证。
- **现代技术栈**：
  - **Compose Multiplatform**: 声明式 UI 框架。
  - **Ktor**: 用于服务端开发与客户端网络通信。
  - **Exposed**: Kotlin 官方 ORM，支持持久化存储。
  - **Kotlinx Serialization**: 高效的数据序列化。

## 🛠 项目架构

项目采用模块化设计，最大程度实现代码复用：

- `:core`: 纯 Kotlin 模块，包含数独算法、数据模型及网络协议。
- `:app:shared`: 包含 Compose UI 组件、状态管理（ViewModel）及网络服务，供各客户端调用。
- `:app:androidApp`: Android 特定配置与启动项。
- `:app:desktopApp`: 桌面端特定配置。
- `:app:webApp`: Web (Wasm) 特定配置。
- `:server`: 基于 Ktor 的服务端，负责用户管理、排行榜及持久化存储。

## 🏁 快速开始

### 前置要求
- JDK 17 或更高版本
- Android Studio Koala 或更高版本
- 安装 Kotlin Multiplatform 插件

### 运行服务端
1. 进入 `server` 模块。
2. 运行 `com.finley.android.sudoku.ApplicationKt` 中的 `main` 函数，或通过命令行运行：
   ```bash
   ./gradlew :server:run
   ```
3. 服务端默认在 `http://localhost:8080` 启动。

### 运行 Android 应用
1. 在 Android Studio 中直接点击运行 `app:androidApp` 配置。
2. 或使用命令行安装并运行：
   ```bash
   ./gradlew :app:androidApp:installDebug
   ```
   - *注意：Android 模拟器访问本地服务请使用 `http://10.0.2.2:8080`。*

### 运行桌面应用 (Desktop)
1. 执行以下 Gradle 任务启动应用：
   ```bash
   ./gradlew :app:desktopApp:run
   ```

### 运行 Web 应用 (Wasm)
1. 执行以下 Gradle 任务启动本地开发服务器并预览：
   ```bash
   ./gradlew :app:webApp:wasmJsBrowserDevelopmentRun
   ```

## 🛡 安全与存储
- **数据库**：默认使用 H2 文件数据库（位于 `server/build/db`），易于开发调试。
- **密码学**：使用 BCrypt 对用户密码进行加盐哈希。
- **认证**：通过 JWT 实现受保护接口的访问控制。

## 📄 许可证
本项目采用 MIT 许可证。
