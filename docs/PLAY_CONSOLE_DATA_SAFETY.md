# Google Play 数据安全表单 — 逐项答案

> 应用：数独 Sudoku（`com.finley.android.sudoku`）
> 结论：**本应用不收集、不共享任何用户数据。**

---

## 1. 概览答案

| 问题 | 答案 |
| --- | --- |
| 您的应用是否会收集或共享任何必需的**用户数据类型**？ | **否** |
| 您的应用是否使用加密方式传输数据？ | **不适用**（无数据传输） |
| 您的应用是否允许用户请求删除其数据？ | **不适用**（无服务端数据） |
| 您的应用是否面向儿童？ | **否**（如选择 13+ 目标受众） |
| 您是否已对照《Google Play 用户数据政策》审查应用？ | **是** |

因为第一个问题选「否」，表单会直接结束，无需填写任何数据类型。

---

## 2. 为什么"不收集"

Google 对"收集"的定义：**从设备向外传输数据**。

本应用的数据流：

| 数据 | 存放位置 | 是否离开设备 |
| --- | --- | --- |
| 关卡进度（`unlocked_level`） | 本地 Settings（SharedPreferences / NSUserDefaults / localStorage / java.util.prefs） | ❌ |
| Pro 购买状态（`pro_unlocked`） | 同上 | ❌ |
| 每日挑战完成日期、连续天数 | 同上 | ❌ |
| 统计数据（对局数、胜场、最高分） | 同上 | ❌ |
| 主题、语言偏好 | 同上 | ❌ |
| 游戏设置（自动擦除笔记、显示冲突） | 同上 | ❌ |

内购交易由 **Google Play 结算库**在系统层面处理，属于 Google 的服务，不计入"应用收集数据"。应用中**没有**任何分析 SDK、崩溃上报 SDK、广告 SDK。

---

## 3. 权限清单（Manifest 中实际声明）

`app/androidApp/src/main/AndroidManifest.xml`：

```xml
<uses-permission android:name="com.android.vending.BILLING" />
```

| 权限 | 用途 | 是否涉及用户数据 |
| --- | --- | --- |
| `com.android.vending.BILLING` | 内购 | 否（交易由 Play 处理） |

**未声明**：
- ❌ `android.permission.INTERNET`
- ❌ `ACCESS_NETWORK_STATE`
- ❌ `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE`
- ❌ `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`
- ❌ `READ_CONTACTS` / `READ_PHONE_STATE`
- ❌ `POST_NOTIFICATIONS`
- ❌ `AD_ID`

可用以下命令复核：

```bash
# 查看合并后的 Manifest 权限
gradle :app:androidApp:processReleaseManifest
cat app/androidApp/build/intermediates/merged_manifest/release/AndroidManifest.xml | grep uses-permission
```

或直接检查已构建的 AAB：

```bash
unzip -p app/androidApp/build/outputs/bundle/release/androidApp-release.aab \
  base/manifest/AndroidManifest.xml | strings | grep -i permission
```

---

## 4. 备份与数据导出设置

Manifest：

```xml
<application
    android:allowBackup="false"
    android:fullBackupContent="false"
    android:dataExtractionRules="@xml/data_extraction_rules"
    ... >
```

`res/xml/data_extraction_rules.xml` 排除所有域（root/file/database/sharedpref/external）的云备份与设备迁移。

**含义**：用户换机不会通过 Google 备份迁移进度；这是刻意设计，确保"数据只在当前设备"，与数据安全表单的"不收集"完全一致。

> 如果后续希望支持换机迁移，需要重新评估数据安全表单（备份到 Google 云端仍不算"应用收集"，但需在隐私政策中说明）。

---

## 5. 隐私政策 URL

Play Console 要求提供可公开访问的隐私政策链接。可直接托管 [PRIVACY_POLICY.md](PRIVACY_POLICY.md) 的渲染版本：

| 方案 | 步骤 |
| --- | --- |
| GitHub Pages（推荐） | 把 `PRIVACY_POLICY.md` 转成 `privacy.html` 放进 `docs/`，仓库 Settings → Pages → 从 `main`/`docs` 发布 |
| Notion | 新建页面粘贴内容 → 分享 → 复制公开链接 |
| 自建站 | 上传 HTML，确保无登录墙、无 `noindex` |

提交前自检：
- [ ] 链接能在**无痕窗口**打开
- [ ] 内容包含：收集的数据类型（无）、用途（无）、第三方（Google Play 结算）、联系方式、生效日期
- [ ] 与应用实际行为一致（这是审核重点）

---

## 6. 内容分级问卷中的相关答案

| 问题 | 答案 |
| --- | --- |
| 应用是否收集或分享用户位置？ | 否 |
| 应用是否允许用户相互交流？ | 否 |
| 应用是否分享用户创建的内容？ | 否 |
| 应用是否包含数字商品购买？ | **是**（`sudoku_pro_lifetime`） |
| 应用是否包含广告？ | 否 |

---

## 7. 如果将来加入联网功能

届时需要更新：

1. Manifest 增加 `INTERNET`
2. 数据安全表单改为「是」，勾选实际收集的类型（如邮箱、用户 ID）
3. 隐私政策补充：收集目的、保留期限、删除方式、第三方处理者
4. 若涉及账号，需提供**应用内删除账号入口**（Play 政策强制要求）
5. 若面向儿童，需符合 Families 政策

---

## 8. 提交前最终核对

- [ ] 数据安全表单第一问选「否」
- [ ] 隐私政策 URL 可公开访问
- [ ] 隐私政策内容与本文件一致
- [ ] 广告问题选「否」
- [ ] 应用访问权限选「所有功能均不受限制」
- [ ] 内容分级问卷全部如实填写
- [ ] AAB 中确实没有 `INTERNET` 权限
