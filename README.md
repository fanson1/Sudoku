# 数独 Sudoku

一款**完全离线**的单机数独游戏，基于 **Kotlin Multiplatform (KMP)** 与 **Compose Multiplatform** 构建。

> 🎮 **商店名称**：数独 Sudoku  
> 📦 **包名**：`com.finley.android.sudoku`  
> 💰 **变现**：免费下载 + 一次性买断 Pro（¥18 / $2.99）  
> 🌐 **语言**：8 种语言 + 跟随系统，应用内即时切换

---

## ✨ 核心特性

| 维度 | 详情 |
| --- | --- |
| **离线优先** | 无网络权限，无账号，无追踪，飞行模式下完整可玩 |
| **50 关卡** | 5 个难度（简单 → 大师），每级 10 关；前 10 关永久免费 |
| **智能辅助** | 笔记自动清理、实时冲突高亮、唯一候选脉动、无限撤销/重做 |
| **挑战模式** | 每日挑战（确定性种子 + 连续天数）、10 分钟限时挑战 |
| **详细统计** | 对局数、胜率、最高分、各难度最佳、每日连续 |
| **Pro 买断** | 一次付费，永久解锁：关卡 11-50、挑战模式、无限提示、统计页 |
| **主题** | 浅色 / 深色 / 跟随系统 |
| **多语言** | English、简体中文、繁體中文、日本語、한국어、Español、Français、Deutsch |

---

## 🏗 架构概览

```
├── core/                    # 纯 KMP 引擎：生成、求解、难度分级、规则、模型
├── app/
│   ├── shared/              # Compose UI + ViewModel（完全离线）
│   │   ├── i18n/            # StringKey枚举 + Strings类 + 8语言包
│   │   ├── pro/             # ProController接口 + Android Billing实现 + 非Android本地解锁
│   │   ├── ui/              # 关卡选择、游戏、统计、设置、付费墙
│   │   ├── game/            # GameState、GameViewModel、GameIntent/Effect
│   │   └── util/            # Persistence、FreeTier、DailyUtil
│   ├── androidApp/          # Android 配置：签名、ProGuard、Manifest(BILLING only)
│   ├── desktopApp/          # JVM 桌面端
│   └── webApp/              # Wasm/Web 端
└── server/                  # ⚠️ 源码保留，构建已排除（仅供参考）
```

### 关键技术选型
- **语言切换**：自研 `StringKey` + `Strings` 映射（非 Compose resources），运行时热切换
- **内购**：Google Play Billing 9.1.0（`queryProductDetailsAsync`、`PendingPurchasesParams`）
- **持久化**：`multiplatform-settings`（SP / NSUserDefaults / localStorage / java.util.prefs）
- **构建**：Gradle 9.6 / AGP 8.x / Kotlin 2.4 / JDK 21 (Corretto)
- **签名**：`keystore/keystore.properties` 驱动 release 签名（gitignored）

---

## 🚀 快速开始

### 前置要求
- JDK 21（推荐 Amazon Corretto 21，已在工具链配置）
- Android Studio Ladybug+ / IntelliJ IDEA 2024.2+

### 开发构建（Debug）
```bash
# 共享模块类型检查（最快）
gradle :app:shared:compileDevKotlinJvm

# Android Debug APK（applicationId 会加 .debug 后缀，不可测试内购）
gradle :app:androidApp:assembleDebug

# 桌面端运行
gradle :app:desktopApp:run

# Web 开发服务器
gradle :app:webApp:wasmJsBrowserDevelopmentRun
```

### 发布构建（Release / Play Console 上传）
```bash
# 1. 确认 keystore 就位
cat keystore/keystore.properties
ls -l keystore/upload-keystore.jks

# 2. 构建签名 AAB
gradle :app:androidApp:bundleRelease

# 3. 校验签名
$JAVA_HOME/bin/jarsigner -verify -verbose -certs \
  app/androidApp/build/outputs/bundle/release/androidApp-release.aab

# 4. (可选) 本地安装测试
java -jar bundletool.jar build-apks \
  --bundle=app/androidApp/build/outputs/bundle/release/androidApp-release.aab \
  --output=sudoku.apks --mode=universal
adb install -r sudoku.apks/universal.apk
```

> ⚠️ **内购测试必须使用 release 签名包**（内部测试轨道 + 许可测试账号）。  
> Debug 包的 `applicationIdSuffix = ".debug"` 会导致 Play Billing 拒绝。

---

## 📱 应用内购说明

| 项目 | 值 |
| --- | --- |
| Product ID | `sudoku_pro_lifetime` |
| 类型 | 非消耗型（一次性，终身） |
| 价格 | ¥18（中国）/ $2.99（美国），其余地区自动换算 |
| 解锁内容 | 关卡 11-50、每日挑战、限时挑战、无限提示、详细统计 |

详见：[docs/IAP_SETUP.md](docs/IAP_SETUP.md)

---

## 🌍 语言支持

应用内设置 → 语言，即时生效（无需重启）：

| 代码 | 显示名 | 本地名 |
| --- | --- | --- |
| `system` | Follow system | 跟随系统 |
| `en` | English | English |
| `zh-Hans` | 简体中文 | 简体中文 |
| `zh-Hant` | 繁體中文 | 繁體中文 |
| `ja` | 日本語 | 日本語 |
| `ko` | 한국어 | 한국어 |
| `es` | Español | Español |
| `fr` | Français | Français |
| `de` | Deutsch | Deutsch |

实现细节：`app/shared/src/commonMain/kotlin/com/finley/android/sudoku/i18n/`

---

## 📦 Play Store 上架物料

所有上架所需文档与素材均在仓库内：

| 文件 | 说明 |
| --- | --- |
| `docs/GOOGLE_PLAY_PUBLISHING.md` | 端到端上架指南 |
| `docs/IAP_SETUP.md` | 内购商品配置与代码路径 |
| `docs/KEYSTORE_SETUP.md` | 密钥库生成、CI、灾备恢复 |
| `docs/PLAY_CONSOLE_DATA_SAFETY.md` | 数据安全表单逐项答案（不收集任何数据） |
| `docs/PLAY_STORE_LISTING.md` | 8 语言商店文案（名称/简短/完整/版本说明） |
| `docs/PRIVACY_POLICY.md` | 隐私政策（中英双语） |
| `docs/SCREENSHOT_GUIDE.md` | 截图/图标/特色图片规格与生成脚本 |
| `docs/RELEASE_CHECKLIST.md` | 发布前逐项核对清单 |
| `docs/CHANGELOG.md` | 版本历史 |
| `tools/make_store_assets.py` | 一键生成图标、特色图、6 张概念截图 |
| `play-store-assets/` | 生成产物目录（已含示例） |

---

## 🔐 隐私与数据安全

- **零数据收集**：应用无 `INTERNET` 权限，无任何分析/崩溃/广告 SDK
- **本地存储**：进度、设置、购买状态仅存于设备（SharedPreferences 等）
- **内购交易**：由 Google Play 结算库处理，应用不接触支付信息
- **备份禁用**：`allowBackup=false`、`fullBackupContent=false`，云备份/设备迁移均排除
- **隐私政策**：见 [docs/PRIVACY_POLICY.md](docs/PRIVACY_POLICY.md)

---

## 📄 许可证

MIT License — 详见 [LICENSE](LICENSE)（如无则默认 MIT）。

---

## 📚 相关文档

- [AGENTS.md](AGENTS.md) — 构建/开发/发布完整手册（给 AI/人类维护者）
- [docs/](docs/) — 所有上架物料与操作指南

---

*最后更新：2026-11-01*