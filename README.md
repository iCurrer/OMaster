# OMaster - 大师模式调色参数库

<p align="center">
  <a href="README.md">🇨🇳 中文</a> | 
  <a href="README_EN.md">🇺🇸 English</a>
</p>

<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="120" alt="OMaster Logo"/>
</p>

<p align="center">
  <b>为各品牌手机打造的摄影调色参数管理工具</b>
</p>

<p align="center">
  <a href="https://github.com/iCurrer/OMaster/releases">
    <img src="https://img.shields.io/github/v/release/iCurrer/OMaster" alt="Version"/>
  </a>
  <a href="https://creativecommons.org/licenses/by-nc-sa/4.0/deed.zh">
    <img src="https://img.shields.io/badge/协议-CC%20BY--NC--SA%204.0-orange.svg?style=flat-square" alt="License"/>
  </a>
  <img src="https://img.shields.io/badge/平台-Android%206+-brightgreen.svg?style=flat-square" alt="Platform"/>
  <img src="https://img.shields.io/badge/技术-Jetpack%20Compose-4285F4.svg?style=flat-square" alt="Tech"/>
  <img src="https://img.shields.io/github/actions/workflow/status/iCurrer/OMaster/beta-release.yml" alt="Build"/>
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
- **23+ 款专业预设** - 涵盖胶片、复古、清新、黑白、美食等多种风格
- **新预设置顶标记** - 新增预设显示 NEW 标签并置顶展示

### ☁️ 配置云更新
- 支持从云端获取最新配置
- 支持自定义更新源

### ⭐ 收藏管理
- 一键收藏喜欢的预设
- 快速访问常用参数
- 本地存储，无需网络

### 🛠️ 全平台支持
- 支持创建自定义预设
- 支持各大主流平台专业相机
- 支持远程订阅更新

### 🔲 悬浮窗模式
- 拍照时可悬浮显示参数
- 支持左右滑动切换预设
- 半透明设计不遮挡取景

### 📱 简洁优雅的界面
- 纯黑背景 + 各大摄影品牌配色
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
前往 [Releases 页面](https://github.com/iCurrer/OMaster/releases) 下载最新版本 APK

### 方式二：国内镜像
- 蒲公英：[https://www.pgyer.com/omaster-android](https://www.pgyer.com/omaster-android)
- 蓝奏云：[https://wwbwy.lanzouu.com/b016klqmib](https://wwbwy.lanzouu.com/b016klqmib)

### 系统要求
- Android 6 (API 23) 及以上
- 你的手机系统允许浮窗显示在相机应用上方

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

OMaster 支持的大师模式参数包括但不限于：

| 参数类别 | 具体参数 |
|---------|---------|
| **基础参数** | 滤镜、柔光、影调、饱和度、冷暖、青品、锐度、暗角 |
| **专业参数** | ISO、快门速度、曝光补偿、色温、色调 |

---

## 📝 更新日志

见 [更新日志](CHANGELOG.md)

---

## ❓ 常见问题

### 悬浮窗无法开启怎么办？

部分 ColorOS / OxygenOS 系统可能会将本应用识别为"未知来源应用"，从而限制悬浮窗权限授权。

**解决方法：**

1. 打开**设置** → **应用** → **应用管理**
2. 找到 **OMaster**，点击**权限管理**
3. 点击右上角 **⋮** 图标，选择**解除所有授权限制**
4. 返回应用重新开启悬浮窗权限

> [!WARNING]
> 
> 解除限制后，请确保只授予"悬浮窗"权限，其他敏感权限可根据需要选择是否授予。

---

## 🔒 隐私说明

- 所有数据本地存储
- 悬浮窗权限仅用于显示参数窗口
- 统计功能需用户同意后开启

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

### 提交新预设

> [!IMPORTANT]
> 云端预设已迁移至独立仓库维护
> 
> 如果只是更新云端预设，不要直接在 OMaster 主仓库提交 Pull Request

如果你想贡献新的调色预设：

1. 前往 [OMaster-Community](https://github.com/fengyec2/OMaster-Community) 社区仓库
2. Fork 社区仓库
3. 新建一个分支（例如 `preset` ）
4. 在新分支（例如 `preset` ）的 `presets.json` 中添加预设数据
5. 在主分支（就是 `main` 分支）修改 README
6. 提交 Pull Request，把你的新分支（例如 `preset` ）合并到 `fengyec2/OMaster-Community:main` 中

---

## 📄 开源协议

本项目采用 [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/deed.zh) 开源

- **署名** (BY) - 使用时需保留版权声明
- **非商业性使用** (NC) - 禁止商业用途
- **相同方式共享** (SA) - 修改后需使用相同协议

---

## 🙏 致谢

- **项目维护：**

| | |
|---|---|
| <a href="https://github.com/iCurrer"><img src="https://avatars.githubusercontent.com/u/61453275?v=4" width="80px;" alt="Silas"/><br /><sub><b>Silas</b></sub></a><br /><a href="https://github.com/iCurrer/OMaster/commits?author=iCurrer" title="Code">💻</a> <a href="#design-Silas" title="Design">🎨</a> <a href="https://github.com/iCurrer/OMaster/commits?author=iCurrer" title="Documentation">📖</a> <a href="#ideas-Silas" title="Ideas, Planning, & Feedback">🤔</a> <a href="#maintenance-Silas" title="Maintenance">🚧</a> | <a href="https://github.com/fengyec2"><img src="https://avatars.githubusercontent.com/u/85821538?v=4" width="80px;" alt="Luminary"/><br /><sub><b>Luminary</b></sub></a><br /><a href="https://github.com/iCurrer/OMaster/commits?author=fengyec2" title="Code">💻</a> <a href="https://github.com/iCurrer/OMaster/commits?author=fengyec2" title="Documentation">📖</a> <a href="#ideas-Luminary" title="Ideas">💡</a>|

- **素材提供：**
  - [@OPPO影像](https://xhslink.com/m/8c2gJYGlCTR)
  - [@蘭州白鴿](https://xhslink.com/m/4h5lx4Lg37n)
  - [@派瑞特凯](https://xhslink.com/m/AkrgUI0kgg1)
  - [@ONESTEP™](https://xhslink.com/m/4LZ8zRdNCSv)
  - [@盒子叔](https://xhslink.com/m/4mje9mimNXJ)
  - **[@Aurora](https://xhslink.com/m/2Ebow4iyVOE)** ⭐ 新增

---

## 📞 联系我们

- 提交 [GitHub Issue](https://github.com/iCurrer/OMaster/issues)
- 发送邮件至：iboy66lee@qq.com

---

<p align="center">
  <b>Made with ❤️ by Silas</b>
</p>

<p align="center">
  <sub>纯本地化运作，数据存储在本地</sub>
</p>