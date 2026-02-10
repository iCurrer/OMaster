# OMaster - 大师模式调色参数库

<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="120" alt="OMaster Logo"/>
</p>

<p align="center">
  <b>专为 OPPO / 一加 / Realme 手机打造的摄影调色参数管理工具</b>
</p>

<p align="center">
  <a href="https://github.com/iCurrer/OMaster/releases">
    <img src="https://img.shields.io/badge/版本-v1.0.0-FF6B35.svg?style=flat-square" alt="Version"/>
  </a>
  <a href="https://github.com/iCurrer/OMaster/blob/main/LICENSE">
    <img src="https://img.shields.io/badge/许可证-MIT-blue.svg?style=flat-square" alt="License"/>
  </a>
  <img src="https://img.shields.io/badge/平台-Android%2014+-brightgreen.svg?style=flat-square" alt="Platform"/>
  <img src="https://img.shields.io/badge/技术-Jetpack%20Compose-4285F4.svg?style=flat-square" alt="Tech"/>
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
| **专业参数** | 曝光补偿、色温、色调、白平衡、色调风格 |

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
      "filter": "滤镜类型",
      "whiteBalance": "白平衡",
      "colorTone": "色调风格",
      "exposureCompensation": "曝光补偿",
      "colorTemperature": 5500,
      "colorHue": 0,
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

本项目采用 [MIT 许可证](LICENSE) 开源

---

## 🙏 致谢

- 素材提供：[@OPPO影像](https://xhslink.com/m/8c2gJYGlCTR)
- 设计灵感：哈苏相机品牌色系
- 开发框架：Jetpack Compose

---

## 📞 联系我们

如有问题或建议，欢迎通过以下方式联系：

- 提交 [GitHub Issue](https://github.com/iCurrer/OMaster/issues)
- 发送邮件至：（待添加）

---

<p align="center">
  <b>Made with ❤️ by Silas</b>
</p>

<p align="center">
  <sub>纯本地化运作，数据存储在本地</sub>
</p>
