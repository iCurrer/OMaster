# OMaster - 大师模式调色参数库

<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="120" alt="OMaster Logo"/>
</p>

<p align="center">
  <b>专为 OPPO / 一加 / Realme 手机打造的摄影调色参数管理工具</b>
</p>

<p align="center">
  <a href="https://github.com/iCurrer/OMaster/releases">
    <img src="https://img.shields.io/badge/版本-v1.1.0-FF6B35.svg?style=flat-square" alt="Version"/>
  </a>
  <a href="https://creativecommons.org/licenses/by-nc-sa/4.0/deed.zh">
    <img src="https://img.shields.io/badge/协议-CC%20BY--NC--SA%204.0-orange.svg?style=flat-square" alt="License"/>
  </a>
  <img src="https://img.shields.io/badge/平台-Android%2014+-brightgreen.svg?style=flat-square" alt="Platform"/>
  <img src="https://img.shields.io/badge/技术-Jetpack%20Compose-4285F4.svg?style=flat-square" alt="Tech"/>
</p>

<p align="center">
  <a href="https://github.com/iCurrer/OMaster/releases">
    <b>⬇️ 立即下载最新版本</b>
  </a>
</p>

---

## 📸 还在为拍照参数而烦恼吗？

每次想拍出满意的照片，却要在互联网的海量信息里像**大海捞针**一样搜索参数，既浪费时间又不一定能找到适合自己的那一款 😫

现在，**OMaster** 为你打造了一个界面简洁清爽的平台，所有数据都一目了然，让你轻松告别参数焦虑 ✨

---

## ✨ 核心功能

### 🎨 丰富的预设库
- 收录多款专业摄影调色预设
- 涵盖胶片、复古、清新、黑白等多种风格
- 支持 Pro 模式和 Auto 模式参数

### ⭐ 收藏管理
- 一键收藏喜欢的预设
- 快速访问常用参数
- 本地存储，无需网络

### 🛠️ 自定义预设
- 创建属于自己的调色参数
- 导入自定义封面图片
- 灵活调节各项参数

### 🔲 悬浮窗模式
- 拍照时可悬浮显示参数
- 半透明设计不遮挡取景
- 可自由拖动位置

### 📱 简洁优雅的界面
- 纯黑背景 + 哈苏橙配色
- 流畅的动画过渡
- 瀑布流卡片布局

---

## 🎬 功能预览

| 首页浏览 | 预设详情 | 悬浮窗 |
|---------|---------|--------|
| 瀑布流展示所有预设 | 查看完整参数和样片 | 拍照时随时参考 |
| 支持分类筛选 | 图片轮播展示效果 | 可收起为悬浮球 |

---

## 📥 下载安装

### 方式一：GitHub Releases
前往 [Releases 页面](https://github.com/iCurrer/OMaster/releases) 下载最新版本的 APK

### 方式二：扫码下载
（待添加二维码）

### 系统要求
- Android 14 (API 34) 及以上
- 支持 OPPO / 一加 / Realme 手机的大师模式

---

## 🛠️ 技术栈

| 技术 | 用途 |
|------|------|
| **Kotlin** | 主要开发语言 |
| **Jetpack Compose** | 现代化 UI 框架 |
| **Material Design 3** | 设计语言 |
| **Coil** | 图片加载 |
| **Gson** | JSON 解析 |
| **Kotlin Serialization** | 类型安全导航 |

---

## 📋 参数说明

OMaster 支持的大师模式参数包括：

| 参数类别 | 具体参数 |
|---------|---------|
| **基础参数** | 滤镜、柔光、影调、饱和度、冷暖、青品、锐度、暗角 |
| **专业参数** | ISO、快门速度、曝光补偿、色温、色调 |

---

## 📝 更新日志

### v1.1.0 (2026-02-12)

#### ✨ 新增功能
- **悬浮窗预设切换** - 悬浮窗标题栏新增左右切换按钮，可在拍照时快速切换不同预设参数
- **悬浮窗无闪动切换** - 优化切换逻辑，切换预设时窗口位置保持不变，内容平滑更新
- **删除二次确认** - 自定义预设删除时增加确认对话框，防止误删操作
- **版本号统一管理** - 新建 `VersionInfo` 工具类，从 `BuildConfig` 自动读取版本号，避免多处修改不一致

#### 📸 新增预设
- **手机徕卡** - 人文滤镜配方，红色蓝色突出，色彩浓郁德味十足，作者：@盒子叔
- **梦幻富士** - 模仿富士NC负片经典胶片色彩，适合绿色植物、红色元素、日常扫街，作者：@盒子叔

#### 🎨 UI 优化
- 悬浮窗引导对话框增加 10 秒倒计时功能
- 优化对话框样式，使用品牌色渐变和圆角设计
- 关于页面新增 @盒子叔 素材提供者链接

#### 🐛 Bug 修复
- 修复悬浮窗引导对话框点击"以后再说"仍跳转系统权限设置页面的问题
- 修复版本号不一致导致更新检查提示错误的问题
- 修复 `UpdateChecker` 与 `VersionInfo` 类名冲突导致的编译错误

---

### v1.0.2 (2026-02-11)

#### 🔥 重要更新
- **移除友盟统计 SDK**，解决安装时敏感权限提示问题
- **集成 Firebase Analytics**，无需敏感权限即可统计使用数据
- 优化悬浮窗权限申请流程

#### 🐛 Bug 修复
- 修复 OPPO 安装管理器提示敏感权限导致权限被锁定的问题
- 修复 BuildConfig 编译错误

---

### v1.0.1 (2026-02-11)

#### ✨ 新增预设
- **人文** - 适合人文街拍，作者：@蘭州白鴿
- **清新** - 青橙色调，适合自然风光，作者：@蘭州白鴿
- **氛围雪夜** - 冷暖碰撞，王家卫电影感，作者：@派瑞特凯
- **美味流芳** - 美食专用，奶油光泽感，作者：@ONESTEP™

#### 🎨 功能优化
- Pro 模式预设新增 ISO 和快门速度参数
- 悬浮窗精简显示，只展示基础参数
- 关于页面新增素材提供者链接

#### 🐛 Bug 修复
- 修复专业参数重复显示问题
- 优化预设详情页参数展示逻辑

---

## ❓ 常见问题

### 悬浮窗无法开启怎么办？

部分 ColorOS / OxygenOS 系统（OPPO / 一加 / Realme）可能会将本应用识别为"未知来源应用"，从而限制悬浮窗权限授权。

**解决方法：解除未知来源应用的授权限制**

#### 方式一：系统设置
1. 打开**设置** → **应用** → **应用管理**
2. 找到 **OMaster**，点击**权限管理**
3. 点击右上角 **⋮** 图标，选择**"解除所有授权限制"**
4. 返回应用重新开启悬浮窗权限

#### 方式二：桌面快捷操作
1. 长按桌面上的 **OMaster** 应用图标
2. 点击**"应用详情"**
3. 选择**"权限管理"**
4. 点击右上角 **⋮** 图标，选择**"解除所有授权限制"**

> ⚠️ 注意：解除限制后，请确保只授予"悬浮窗"权限，其他敏感权限可根据需要选择是否授予。

---

## 🔒 隐私说明

- 所有数据本地存储，无需联网
- 悬浮窗权限仅用于显示参数窗口
- 统计功能需用户同意后开启
- 详细隐私政策请查看应用内说明

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

### 提交新预设
如果你想贡献新的调色预设，可以：
1. Fork 本仓库
2. 在 `app/src/main/assets/presets.json` 中添加预设数据
3. 在 `app/src/main/assets/images/` 中添加样片
4. 提交 Pull Request

### 预设数据格式
```json
{
  "presets": [
    {
      "name": "预设名称",
      "coverPath": "images/cover.jpg",
      "galleryImages": ["images/sample1.jpg"],
      "author": "@作者",
      "mode": "pro",
      "iso": "100",
      "shutterSpeed": "1/125",
      "exposureCompensation": "0",
      "colorTemperature": 5500,
      "colorHue": 0,
      "whiteBalance": null,
      "colorTone": null,
      "filter": "滤镜类型",
      "softLight": "柔光强度",
      "tone": 0,
      "saturation": 0,
      "warmCool": 0,
      "cyanMagenta": 0,
      "sharpness": 50,
      "vignette": "关"
    }
  ]
}
```

---

## 📄 开源协议

本项目采用 [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/deed.zh)（知识共享署名-非商业性使用-相同方式共享 4.0 国际许可协议）开源

### 协议要点

- **署名** (BY) - 您必须给出适当的署名，提供指向本许可协议的链接，并标明是否作出了修改
- **非商业性使用** (NC) - 您不得将本作品用于商业目的
- **相同方式共享** (SA) - 如果您再混合、转换或基于本作品进行创作，您必须基于与原先相同的许可协议分发您的贡献作品

### 详细说明

1. **免费使用**：任何人都可以免费使用、修改本软件的源代码
2. **禁止商用**：禁止将本软件或基于本软件开发的衍生作品用于商业销售或盈利目的
3. **保留版权**：使用或修改时必须保留原始版权声明和作者信息
4. **协议继承**：修改后的作品必须使用 CC BY-NC-SA 4.0 协议开源

完整的协议内容请参阅 [LICENSE](LICENSE) 文件或访问 [Creative Commons 官网](https://creativecommons.org/licenses/by-nc-sa/4.0/legalcode.zh-hans)

---

## 🙏 致谢

- 素材提供：
  - [@OPPO影像](https://xhslink.com/m/8c2gJYGlCTR)
  - [@蘭州白鴿](https://xhslink.com/m/4h5lx4Lg37n)
  - [@派瑞特凯](https://xhslink.com/m/AkrgUI0kgg1)
  - [@ONESTEP™](https://xhslink.com/m/4LZ8zRdNCSv)
  - [@盒子叔](https://xhslink.com/m/4mje9mimNXJ)
- 设计灵感：哈苏相机品牌色系
- 开发框架：Jetpack Compose

---

## 📞 联系我们

如有问题或建议，欢迎通过以下方式联系：

- 提交 [GitHub Issue](https://github.com/iCurrer/OMaster/issues)
- 发送邮件至：iboy66lee@qq.com

---

<p align="center">
  <b>Made with ❤️ by Silas</b>
</p>

<p align="center">
  <sub>纯本地化运作，数据存储在本地</sub>
</p>
