# 内购（Google Play Billing）配置与实现

> 商品：一次性买断的 **Pro 终身版**
> Product ID：`sudoku_pro_lifetime`
> 类型：**非消耗型（Non-consumable / One-time product）**
> 建议定价：**¥18 / $2.99**

---

## 1. 商品设计

| 项 | 值 |
| --- | --- |
| Product ID | `sudoku_pro_lifetime` |
| 名称（zh-CN） | 数独 Pro 终身版 |
| 名称（en-US） | Sudoku Pro (Lifetime) |
| 说明（zh-CN） | 一次购买，永久解锁第 11-50 关、每日挑战、限时挑战、无限提示、详细统计与全部主题。无订阅、无广告。 |
| 说明（en-US） | One-time purchase. Unlocks levels 11-50, Daily Challenge, Timed Challenge, unlimited hints, detailed statistics and all themes. No subscription, no ads. |
| 类型 | 应用内商品（一次性） |
| 定价 | ¥18（中国）/ $2.99（美国），其余地区用 Play 自动换算 |

### 免费 vs Pro 功能边界

代码中的唯一定义处：`app/shared/src/commonMain/kotlin/com/finley/android/sudoku/util/FreeTier.kt`

| 功能 | 免费 | Pro |
| --- | --- | --- |
| 关卡 1-10（简单、中等） | ✅ | ✅ |
| 关卡 11-50（困难、专家、大师） | ❌ | ✅ |
| 笔记 / 撤销 / 重做 | ✅ | ✅ |
| 提示次数 | 每关 3 次 | 无限 |
| 浅色 / 深色 / 跟随系统主题 | ✅ | ✅ |
| 每日挑战 | ❌ | ✅ |
| 限时挑战（10 分钟） | ❌ | ✅ |
| 详细统计页 | ❌ | ✅ |

---

## 2. Play Console 配置步骤

### 2.1 前提
- 应用已上传至少一个 AAB（哪怕内部测试轨道）
- 应用已启用「Play 结算库」权限（`com.android.vending.BILLING` 已在 Manifest 中）

### 2.2 创建商品

1. Play Console → 选择应用 → **创收 → 商品 → 应用内商品**
2. 点击 **创建商品**
3. 填写：
   - **商品 ID**：`sudoku_pro_lifetime`（**创建后不可修改**）
   - **名称**：数独 Pro 终身版
   - **说明**：见上表
4. **保存**
5. 进入商品 → **定价** → 设置默认价格 `USD 2.99`（或 `CNY 18`）
6. **激活**（状态从「草稿」变为「有效」）

> ⚠️ 商品必须处于**有效**状态，否则 `queryProductDetailsAsync` 返回空列表，Paywall 会显示"计费不可用"。

### 2.3 多语言商品信息

在商品编辑页 → **管理翻译**，为 8 种语言填写名称与说明：

| 语言 | 名称 | 说明（简版） |
| --- | --- | --- |
| en-US | Sudoku Pro (Lifetime) | One-time purchase. Unlocks all levels, challenges, unlimited hints and stats. |
| zh-CN | 数独 Pro 终身版 | 一次购买，永久解锁全部关卡、挑战模式、无限提示与详细统计。 |
| zh-TW | 數獨 Pro 終身版 | 一次購買，永久解鎖全部關卡、挑戰模式、無限提示與詳細統計。 |
| ja-JP | 数独 Pro（買い切り） | 一度の購入で全レベル・チャレンジ・ヒント無制限・統計を解放。 |
| ko-KR | 스도쿠 Pro (평생 이용권) | 한 번 구매로 모든 레벨, 챌린지, 무제한 힌트, 통계를 이용하세요. |
| es-ES | Sudoku Pro (de por vida) | Pago único. Desbloquea todos los niveles, retos, pistas ilimitadas y estadísticas. |
| fr-FR | Sudoku Pro (à vie) | Achat unique. Débloque tous les niveaux, défis, indices illimités et statistiques. |
| de-DE | Sudoku Pro (Lebenslang) | Einmaliger Kauf. Alle Level, Herausforderungen, unbegrenzte Tipps und Statistiken. |

### 2.4 许可测试（测试购买）

1. **设置 → 许可测试**
2. 添加测试用 Google 账号（必须是已登录设备的账号）
3. 选择响应：`RESPOND_NORMALLY`（推荐，能看到真实流程）
4. 用该账号从**内部测试轨道**安装应用
5. 购买时应出现「测试卡，始终批准」

---

## 3. 代码实现位置

| 文件 | 作用 |
| --- | --- |
| `app/shared/src/commonMain/kotlin/.../pro/ProController.kt` | 接口 + `ProProduct.ID` + `ProMessage` 枚举 + `expect rememberProController()` |
| `app/shared/src/androidMain/kotlin/.../pro/ProController.android.kt` | **真正的 Google Play Billing 实现**（Billing 9.1.0） |
| `app/shared/src/jvmMain/.../pro/ProController.jvm.kt` | 桌面端：本地解锁 |
| `app/shared/src/iosMain/.../pro/ProController.ios.kt` | iOS：本地解锁 |
| `app/shared/src/webMain/.../pro/ProController.web.kt` | Web：本地解锁 |
| `app/shared/src/commonMain/kotlin/.../ui/components/PaywallDialog.kt` | 付费墙 UI |
| `app/shared/src/commonMain/kotlin/.../ui/components/SettingsSheet.kt` | 设置页中的 Pro 区块 |
| `app/shared/src/commonMain/kotlin/.../util/Persistence.kt` | `isPro()` / `setPro()` 本地缓存 |

### 3.1 依赖

`gradle/libs.versions.toml`：

```toml
billing = "9.1.0"
...
billing = { module = "com.android.billingclient:billing", version.ref = "billing" }
```

`app/shared/build.gradle.kts`：

```kotlin
androidMain.dependencies {
    implementation(libs.billing)
}
```

### 3.2 关键 API（Billing 9.x 与 7.x 的差异）

```kotlin
// 9.x：回调第二个参数是 QueryProductDetailsResult
billingClient.queryProductDetailsAsync(params) { result, queryResult ->
    val details = queryResult.productDetailsList.firstOrNull()
    val price = details?.oneTimePurchaseOfferDetails?.formattedPrice
}

// 待处理购买（9.x 必须显式声明支持一次性商品）
BillingClient.newBuilder(context)
    .setListener(listener)
    .enablePendingPurchases(
        PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
    )
    .enableAutoServiceReconnection()
    .build()
```

### 3.3 权益判定逻辑

```
启动 → 读本地 Persistence.isPro()（立即生效，保证离线可用）
     ↓
连接 Play → queryPurchasesAsync(INAPP)
     ├─ 查到已购 & PURCHASED  → 授权 Pro，并 acknowledge（若未确认）
     ├─ 查到但无该商品        → 撤销 Pro（退款/取消场景）
     └─ 查询失败（离线）      → 保留本地缓存（不误伤付费用户）
```

购买流程：

```
purchase()
  → launchBillingFlow(activity, ProductDetailsParams)
  → PurchasesUpdatedListener
      ├─ OK + purchases      → handlePurchase → acknowledge → grantPro()
      ├─ OK + 空列表          → PURCHASE_CANCELLED
      ├─ USER_CANCELED       → PURCHASE_CANCELLED
      ├─ ITEM_ALREADY_OWNED  → queryPurchases(announce = true)
      └─ 其他                 → PURCHASE_FAILED
```

恢复购买：`restore()` → `queryPurchasesAsync` → 有则 `RESTORE_SUCCESS`，无则 `RESTORE_NOTHING`。

---

## 4. 常见问题排查

| 现象 | 原因 | 解决 |
| --- | --- | --- |
| Paywall 显示"计费不可用" | 商品未激活 / 未上传过 AAB / 非 Play 安装包 | 激活商品；用内部测试轨道安装；不要用侧载 APK |
| 价格一直显示"加载中" | `queryProductDetailsAsync` 失败 | 检查商品 ID 拼写；确认账号在许可测试名单 |
| 购买成功但 Pro 没解锁 | 未调用 `acknowledgePurchase`（3 天后自动退款） | 代码已处理；检查 `handlePurchase` 是否被调用 |
| 重启后 Pro 消失 | 本地缓存被清 / 另一台设备退款 | 检查 `Persistence.setPro(true)` 是否执行 |
| `ITEM_ALREADY_OWNED` | 该账号已购买过 | 走恢复购买流程（代码已处理） |
| Debug 包无法购买 | applicationId 带 `.debug` 后缀 | 必须用 release 签名包测试内购 |
| 桌面 / Web 端点了就解锁 | 这是预期行为 | 非 Android 平台使用 `LocalProController`，仅用于开发体验 |

### 调试日志

`ProController.android.kt` 目前不上报日志。需要时临时加：

```kotlin
android.util.Log.d("Billing", "response=${result.responseCode} msg=${result.debugMessage}")
```

---

## 5. 定价与退款

- **改价**：Play Console → 商品 → 定价。已购买用户不受影响。
- **退款**：由 Google Play 处理，退款后 `queryPurchasesAsync` 不再返回该商品 → 应用自动撤销 Pro。
- **促销**：一次性商品支持"开发者指定价格"折扣，可在 Console 中配置限时优惠。

---

## 6. 后续可扩展

| 需求 | 做法 |
| --- | --- |
| 加订阅 | 新建订阅商品，`ProController` 增加 `SubscriptionOffer` 分支 |
| 加"去广告"独立商品 | 新建 `sudoku_remove_ads`（本应用无广告，暂不需要） |
| 加皮肤/主题包 | 新建消耗型或非消耗型商品，扩展 `ProController` 为多商品管理 |
| iOS 内购 | 在 `iosMain` 用 StoreKit 2 实现 `ProController`（接口已就绪） |

---

## 7. 检查清单

- [ ] Play Console 中 `sudoku_pro_lifetime` 状态为**有效**
- [ ] 默认价格已设置
- [ ] 8 种语言商品信息已填写
- [ ] 许可测试账号已添加
- [ ] 内部测试轨道已发布且可安装
- [ ] 测试购买成功 → Pro 解锁 → 重启应用仍为 Pro
- [ ] 清除数据后「恢复购买」能恢复 Pro
