# 密钥库（Keystore）生成与保管

> Google Play 要求所有 AAB 使用**同一个上传密钥**签名。密钥丢失 = 无法更新应用（除非申请密钥重置，且仅在新账号/首次可用时批准）。

---

## 1. 一次性生成（已完成 ✅）

仓库中已生成：

```
keystore/upload-keystore.jks      # 密钥库（已在 .gitignore 中忽略）
keystore/keystore.properties      # 密码配置（已在 .gitignore 中忽略）
```

`keystore.properties` 内容格式：

```properties
storeFile=upload-keystore.jks
storePassword=<24 位随机密码>
keyAlias=upload
keyPassword=<同上>
```

**⚠️ 立即把这两个文件备份到密码管理器 / 加密云盘。** 它们被 `.gitignore` 排除，不会进入版本库。

---

## 2. 从零重新生成

如果需要在另一台机器重建（**注意：这会更换签名，已发布的应用将无法更新**，仅用于首次发布前）：

```bash
cd /path/to/Sudoku
mkdir -p keystore

# 生成 4096 位 RSA、有效期 10000 天的上传密钥
keytool -genkeypair -v \
  -keystore keystore/upload-keystore.jks \
  -alias upload \
  -keyalg RSA -keysize 4096 -validity 10000 \
  -storepass "YOUR_STRONG_PASSWORD" \
  -keypass "YOUR_STRONG_PASSWORD" \
  -dname "CN=Finley Sudoku, OU=Mobile, O=Finley, L=Shanghai, ST=Shanghai, C=CN"

# 写入 Gradle 读取的配置
cat > keystore/keystore.properties <<'EOF'
storeFile=upload-keystore.jks
storePassword=YOUR_STRONG_PASSWORD
keyAlias=upload
keyPassword=YOUR_STRONG_PASSWORD
EOF

chmod 600 keystore/keystore.properties
```

密码要求：至少 16 位，含大小写字母 + 数字。不要用 `123456`、`android`、项目名。

---

## 3. Gradle 如何读取

`app/androidApp/build.gradle.kts`：

```kotlin
val keystorePropertiesFile = rootProject.file("keystore/keystore.properties")
val hasReleaseKeystore = keystorePropertiesFile.exists()

signingConfigs {
    if (hasReleaseKeystore) {
        create("release") {
            storeFile = rootProject.file("keystore/${keystoreProperties.getProperty("storeFile")}")
            storePassword = keystoreProperties.getProperty("storePassword")
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
        }
    }
}

buildTypes {
    getByName("release") {
        if (hasReleaseKeystore) signingConfig = signingConfigs.getByName("release")
    }
}
```

因此：

- 文件存在 → `bundleRelease` 产出**已签名** AAB
- 文件缺失 → 产出**未签名** AAB（Play Console 会拒绝）

---

## 4. CI 环境（GitHub Actions 示例）

不要把密钥库提交到仓库。用 Secrets 注入：

```yaml
- name: Restore keystore
  run: |
    mkdir -p keystore
    echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > keystore/upload-keystore.jks
    cat > keystore/keystore.properties <<EOF
    storeFile=upload-keystore.jks
    storePassword=${{ secrets.KEYSTORE_PASSWORD }}
    keyAlias=upload
    keyPassword=${{ secrets.KEY_PASSWORD }}
    EOF
```

本地准备 base64：

```bash
base64 -i keystore/upload-keystore.jks | pbcopy   # macOS
```

---

## 5. 校验密钥库内容

```bash
keytool -list -v -keystore keystore/upload-keystore.jks -alias upload
```

应输出：
- `Alias name: upload`
- `Entry type: PrivateKeyEntry`
- `Certificate fingerprints:` 中的 `SHA256:` 值

把 **SHA-256 指纹**记在安全的地方，将来核对签名一致性时用。

---

## 6. 校验 AAB 的签名者

```bash
$JAVA_HOME/bin/jarsigner -verify -verbose -certs \
  app/androidApp/build/outputs/bundle/release/androidApp-release.aab \
  | grep -E "CN=|SHA-256"
```

指纹应与第 5 步一致。

---

## 7. 忘记密码 / 丢失密钥怎么办

| 情况 | 处理 |
| --- | --- |
| 尚未发布过任何版本 | 直接重新生成，无影响 |
| 已发布，且开启了 Play 应用签名 | 在 Play Console → 设置 → 应用完整性 → **申请上传密钥重置**，Google 会保留应用签名密钥，用户无感 |
| 已发布，且未开启 Play 应用签名 | ⚠️ 无法更新，只能发布新应用（换包名） |

**结论：上架前务必开启 Play 应用签名。**

---

## 8. .gitignore 相关条目

仓库根 `.gitignore` 已包含：

```
*.keystore
*.jks
*.key
*.pem
keystore/keystore.properties
```

验证密钥库不会被提交：

```bash
git check-ignore -v keystore/upload-keystore.jks keystore/keystore.properties
```

两条都应输出匹配的忽略规则。
