# 截图与商店图片生成指南

> Google Play 对图片有硬性要求，不符合会被拒。本文件列出所需素材、尺寸与生成方式。

---

## 1. 必备素材清单

| 素材 | 尺寸 | 格式 | 数量 | 状态 |
| --- | --- | --- | --- | --- |
| 应用图标 | 512 × 512 | 32-bit PNG（含 alpha） | 1 | 见 `play-store-assets/icon-512.png` |
| 特色图片（Feature graphic） | 1024 × 500 | PNG / JPEG（无 alpha） | 1 | 见 `play-store-assets/feature-graphic-*.png` |
| 手机截图 | 最短边 ≥ 320，最长边 ≤ 3840，比例 ≤ 2:1 | PNG / JPEG | 2-8 张 | 见 `play-store-assets/phone-*.png` |
| 7 英寸平板截图 | 同上 | PNG / JPEG | 可选 | 暂缺（平板非主打） |
| 10 英寸平板截图 | 同上 | PNG / JPEG | 可选 | 暂缺 |
| 宣传视频 | YouTube 链接 | — | 可选 | 不需要 |

> 本应用只支持手机竖屏布局，**手机截图必填**；平板截图可选，缺省时平板用户会看到手机截图。

---

## 2. 推荐截图顺序（6 张）

| # | 画面 | 传达的信息 |
| --- | --- | --- |
| 1 | 关卡选择首页（含进度条 + Pro 横幅） | 50 关、进度可视化 |
| 2 | 游戏进行中（棋盘 + 数字键盘 + 连击） | 核心玩法、界面质感 |
| 3 | 笔记模式（候选数铺满） | 专业数独功能 |
| 4 | 通关弹窗（撒花 + 得分） | 成就感 |
| 5 | 设置面板（语言 chips + 主题） | 8 语言、主题 |
| 6 | Pro 付费墙 | 变现说明 |

---

## 3. 生成方式

### 3.1 应用图标（512×512）

若已有 1024×1024 源图：

```bash
sips -z 512 512 source-icon-1024.png --out play-store-assets/icon-512.png
```

本仓库的图标由脚本生成（品牌色 `#4F46E5` 圆角方块 + 白色 "9"）：

```bash
python3 tools/make_store_assets.py
```

脚本产出：
- `play-store-assets/icon-512.png`
- `play-store-assets/feature-graphic-en.png`
- `play-store-assets/feature-graphic-zh.png`
- `play-store-assets/phone-1..6.png`（1080×2400 概念稿）

> 概念稿用于占位与排版参考。**正式上架建议用真机截图**（见 3.3），因为 Play 审核会检查截图与应用实际界面是否一致。

### 3.2 特色图片（1024×500）

要求：
- 不能有 alpha 通道
- 不要放「立即下载」「第一名」等诱导文案
- 不要包含设备边框以外的其他应用界面

设计要点：
- 左侧：应用名 + 一句卖点（"100% offline · No ads"）
- 右侧：棋盘或图标特写
- 背景：品牌渐变 `#4F46E5 → #0E7490`

### 3.3 真机截图（推荐）

```bash
# 1. 在模拟器/真机上安装 release 包
adb install -r universal.apk

# 2. 逐个界面截图
adb exec-out screencap -p > raw-1.png

# 3. 裁剪状态栏（可选）并缩放到 1080×2400
sips -z 2400 1080 raw-1.png --out play-store-assets/phone-1.png
```

要求：
- 至少 2 张，最多 8 张
- 竖屏（本应用不支持横屏）
- 不要出现调试信息、Toast、通知栏图标（建议开启飞行模式并隐藏状态栏）
- 不要出现其他应用

### 3.4 批量加边框/文案（可选）

如需在截图外加营销文案条：

```python
from PIL import Image, ImageDraw, ImageFont

W, H = 1080, 2400
CANVAS = (1080, 2100)   # 截图区域
BAR = H - CANVAS[1]     # 底部文案条高度

shot = Image.open("raw-1.png").resize(CANVAS)
canvas = Image.new("RGB", (W, H), "#0F172A")
canvas.paste(shot, (0, 0))
d = ImageDraw.Draw(canvas)
font = ImageFont.truetype("/System/Library/Fonts/PingFang.ttc", 56)
d.text((60, CANVAS[1] + 40), "50 关 · 5 个难度", fill="#FFFFFF", font=font)
canvas.save("play-store-assets/phone-1.png")
```

---

## 4. 各语言截图

Play Console 允许为每种语言单独上传截图。若人力有限：

- **必做**：en-US、zh-CN（覆盖绝大多数用户）
- **可选**：其余 6 种语言复用 en-US 截图

若要做本地化截图，只需在应用内切换语言后重新截图（设置 → 语言）。

---

## 5. 审核常见拒绝原因

| 问题 | 说明 |
| --- | --- |
| 截图含设备外框且带品牌 logo | 用无品牌通用外框，或干脆不加外框 |
| 截图含「Download now」等 CTA | 禁止 |
| 截图与实际界面不符 | 必须用真实界面 |
| 图标带 alpha 但边缘有白边 | 导出时检查透明区域 |
| 特色图片含 alpha | 导出为 RGB |
| 截图过小（< 320px） | 用 1080×2400 |
| 出现其他应用图标 | 去掉 |

---

## 6. 素材目录结构

```
play-store-assets/
├── icon-512.png
├── feature-graphic-en.png
├── feature-graphic-zh.png
├── phone-1.png          # 关卡选择
├── phone-2.png          # 游戏中
├── phone-3.png          # 笔记模式
├── phone-4.png          # 通关
├── phone-5.png          # 设置
└── phone-6.png          # Pro 付费墙
```

---

## 7. 生成脚本

见仓库根目录 `tools/make_store_assets.py`。依赖：

```bash
python3 -m pip install pillow
python3 tools/make_store_assets.py
```

脚本会：
1. 用品牌渐变绘制 512×512 图标（圆角 + 白色 9）
2. 绘制 1024×500 特色图片（英文 / 中文两版）
3. 绘制 6 张 1080×2400 概念截图（棋盘网格、键盘、付费墙等）

---

## 8. 上传前自检

- [ ] 图标 512×512，PNG，含 alpha，无白边
- [ ] 特色图片 1024×500，**无 alpha**
- [ ] 手机截图 ≥ 2 张，1080×2400，竖屏
- [ ] 截图中无调试信息、无通知栏、无其他应用
- [ ] 截图内容与应用实际功能一致
- [ ] 中文截图使用简体中文界面
