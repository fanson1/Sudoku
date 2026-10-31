# Google Play 上架完整指南

> 适用于 `数独 Sudoku`（`com.finley.android.sudoku`）v1.0.0
> 应用定位：**完全离线**的单机数独 + 一次性买断的 Pro 内购。

---

## 0. 一分钟概览

| 项目 | 值 |
| --- | --- |
| 应用名称（商店） | 数独 Sudoku |
| 包名 / applicationId | `com.finley.android.sudoku` |
| 版本 | versionCode `1` / versionName `1.0.0` |
| 默认语言 | 英语（en），另有 8 种语言 |
| 支持语言 | en, zh-CN, zh-TW, ja, ko, es, fr, de |
| 内容分级 | 所有人 / 3+ |
| 广告 | 无 |
| 内购 | 1 个非消耗型商品 `sudoku_pro_lifetime` |
| 网络权限 | **无**（AndroidManifest 中不声明 `INTERNET`） |
| 数据收集 | 无（全部数据留在设备本地） |
| 上架产物 | `app/androidApp/build/outputs/bundle/release/androidApp-release.aab` |

---

## 1. 上传前必须完成的 6 件事

1. **生成上传密钥库** → 见 [KEYSTORE_SETUP.md](KEYSTORE_SETUP.md)
2. **构建签名 AAB** → `gradle :app:androidApp:bundleRelease`
3. **创建 Play Console 应用**（包名一旦确定不可更改）
4. **配置内购商品** `sudoku_pro_lifetime` → 见 [IAP_SETUP.md](IAP_SETUP.md)
5. **填写商店资料**（文案 / 截图 / 图标 / 特色图片）→ 见 [PLAY_STORE_LISTING.md](PLAY_STORE_LISTING.md)、[SCREENSHOT_GUIDE.md](SCREENSHOT_GUIDE.md)
6. **填写数据安全表单 + 隐私政策** → 见 [PLAY_CONSOLE_DATA_SAFETY.md](PLAY_CONSOLE_DATA_SAFETY.md)、[PRIVACY_POLICY.md](PRIVACY_POLICY.md)

---

## 2. 构建签名 AAB

### 2.1 确认签名配置就位

```bash
cd /path/to/Sudoku
cat keystore/keystore.properties     # 应包含 storeFile/storePassword/keyAlias/keyPassword
ls -l keystore/upload-keystore.jks
```

`app/androidApp/build.gradle.kts` 会自动读取该文件；文件缺失时 release 构建会退化为**未签名**，Play Console 会拒绝上传。

### 2.2 构建

```bash
# 注意：本仓库的 ./gradlew 缺少 wrapper jar，请直接使用 gradle
gradle :app:androidApp:bundleRelease
```

产物：

```
app/androidApp/build/outputs/bundle/release/androidApp-release.aab
```

### 2.3 验证产物

```bash
# 用 JDK 自带工具校验签名
$JAVA_HOME/bin/jarsigner -verify -verbose -certs \
  app/androidApp/build/outputs/bundle/release/androidApp-release.aab | head
```

如需本地安装测试：

```bash
# 用 bundletool 从 AAB 生成通用 APK
java -jar bundletool.jar build-apks \
  --bundle=app/androidApp/build/outputs/bundle/release/androidApp-release.aab \
  --output=sudoku.apks --mode=universal
unzip -o sudoku.apks universal.apk
adb install -r universal.apk
```

> 调试包 `assembleDebug` 的 applicationId 是 `com.finley.android.sudoku.debug`，
> 它**不能**用于内购测试（Play Billing 只认 release 包名 + 已上传的签名）。

---

## 3. 创建应用

1. 打开 <https://play.google.com/console>
2. **创建应用**
   - 应用名称：`数独 Sudoku`
   - 默认语言：`English (United States)`（建议；后续在「商品详情」中补充中文）
   - 应用或游戏：**游戏**
   - 免费或付费：**免费**（Pro 通过内购解锁，不能选"付费"）
3. 接受开发者计划政策与出口合规声明。

> ⚠️ 包名创建后**永久不可修改**。上传第一个 AAB 时包名必须等于 `com.finley.android.sudoku`。

---

## 4. 设置商店信息（Store settings）

**设置 → 商店设置**：

| 项 | 建议值 |
| --- | --- |
| 应用类别 | 游戏 → 益智 |
| 标签 | 数独、逻辑、益智、离线 |
| 联系方式邮箱 | 你的开发者邮箱 |
| 网站（可选） | 隐私政策托管地址 |
| 外部营销 | 关闭（本应用无营销） |

---

## 5. 完成"应用内容"问卷

**政策 → 应用内容**，逐项填写：

### 5.1 隐私权政策
- 必填 URL → 托管 [PRIVACY_POLICY.md](PRIVACY_POLICY.md) 的内容（GitHub Pages / Notion / 个人站均可）
- 内容必须包含：收集哪些数据、如何使用、如何删除、联系方式

### 5.2 广告
- **本应用不含广告** → 选「否」

### 5.3 应用访问权限
- 所有功能无需登录即可使用 → 选「所有功能均不受限制」

### 5.4 内容分级
填写问卷（答案要点）：
- 类别：游戏
- 暴力 / 性 / 恐怖 / 赌博 / 毒品：全部「否」
- 用户互动：**无**（无聊天、无 UGC 分享、无排行榜）
- 是否分享位置：**否**
- 是否允许用户购买数字商品：**是**（有内购）
- 预期分级结果：**3+ / 所有人**

### 5.5 目标受众
- 年龄段：`13 岁以上`（或按需选择 6-12 与 13+）
- 若包含 13 岁以下：**不要**选择"专为儿童设计"（本应用无广告、无数据收集，但避免 Family 政策额外审核）

### 5.6 新闻应用
- 否

### 5.7 数据安全
见 [PLAY_CONSOLE_DATA_SAFETY.md](PLAY_CONSOLE_DATA_SAFETY.md)。核心答案：
- **不收集、不共享任何用户数据**
- 数据传输加密：**不适用**（无传输）
- 数据可删除：**不适用**（无服务端数据）

### 5.8 政府应用 / 金融功能
- 否

### 5.9 健康应用
- 否

---

## 6. 上传 AAB 到内部测试轨道

1. **测试 → 内部测试 → 创建新版本**
2. 上传 `androidApp-release.aab`
3. 填写"此版本中的新功能"（见 [PLAY_STORE_LISTING.md](PLAY_STORE_LISTING.md) 的 Release notes 段）
4. 添加测试人员邮箱列表 → 保存 → 发布

> 首次上传后，Play 会用你的上传密钥重新签名。之后**必须**始终用同一个 `upload-keystore.jks`，否则无法更新。

### 6.1 建议开启 Play 应用签名

**设置 → 应用完整性 → Play 应用签名**：
- 让 Google 管理应用签名密钥（推荐）
- 上传密钥（upload key）只用于上传，泄露可申请重置

---

## 7. 内购测试

1. **设置 → 许可测试**，把测试账号加入许可测试人员
2. 用该账号在内部测试轨道安装应用
3. 打开应用 → 设置 → Pro → 购买
4. 应看到 `测试卡，始终批准` 的付款面板
5. 购买完成后：`Pro 已解锁` 提示 + 关卡 11-50 解锁

详细排错见 [IAP_SETUP.md](IAP_SETUP.md)。

---

## 8. 正式发布

1. **测试 → 封闭测试**（可选，建议先跑 20 人 14 天以满足新账号要求）
2. **生产 → 创建新版本** → 上传同一 AAB
3. 选择发布国家/地区（建议全部）
4. 填写定价：**免费**（内购价格在"创收 → 商品"里单独设置）
5. 检查"发布概览"中所有红色警告清零
6. **开始发布**（首次发布通常审核 1-7 天）

---

## 9. 审核被拒的常见原因（本项目已规避）

| 风险 | 本项目状态 |
| --- | --- |
| 缺少隐私政策 | ✅ 提供 `docs/PRIVACY_POLICY.md` |
| 数据安全表单与实际不符 | ✅ 无网络、无收集，表单全选"不收集" |
| 使用 `INTERNET` 却声明离线 | ✅ Manifest 无 `INTERNET` 权限 |
| 内购商品未在 Console 创建 | ⚠️ 上架前必须创建 `sudoku_pro_lifetime` |
| 目标 API 过低 | ✅ `targetSdk` 使用最新（见 `gradle/libs.versions.toml`） |
| 应用名称含误导性关键词 | ✅ `数独 Sudoku` 直接描述功能 |
| 截图含其他应用/设备边框水印 | ✅ 按 [SCREENSHOT_GUIDE.md](SCREENSHOT_GUIDE.md) 生成 |

---

## 10. 发布后维护

- **更新版本**：改 `app/androidApp/build.gradle.kts` 里的 `versionCode`（+1）与 `versionName`
- **改文案**：Play Console → 商店发布 → 主要商品详情
- **改价格**：创收 → 商品 → `sudoku_pro_lifetime` → 定价（已有用户不受影响）
- **崩溃排查**：Play Console → 品质 → Android vitals

---

## 11. 相关文档

- [KEYSTORE_SETUP.md](KEYSTORE_SETUP.md) — 密钥库生成与保管
- [IAP_SETUP.md](IAP_SETUP.md) — 内购商品配置与代码路径
- [PLAY_STORE_LISTING.md](PLAY_STORE_LISTING.md) — 商店文案（8 语言）
- [PLAY_CONSOLE_DATA_SAFETY.md](PLAY_CONSOLE_DATA_SAFETY.md) — 数据安全表单逐项答案
- [PRIVACY_POLICY.md](PRIVACY_POLICY.md) — 隐私政策正文
- [SCREENSHOT_GUIDE.md](SCREENSHOT_GUIDE.md) — 截图与图片规格
- [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) — 发布前逐项打勾
- [CHANGELOG.md](CHANGELOG.md) — 版本记录
