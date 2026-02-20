# OMaster - Master Mode Color Tuning Library

<p align="center">
  <a href="README.md">🇨🇳 中文</a> | 
  <a href="README_EN.md">🇺🇸 English</a>
</p>

<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="120" alt="OMaster Logo"/>
</p>

<p align="center">
  <b>A professional photography color tuning parameter management tool for OPPO / OnePlus / Realme phones</b>
</p>

<p align="center">
  <a href="https://github.com/iCurrer/OMaster/releases">
    <img src="https://img.shields.io/badge/Version-v1.2.0-FF6B35.svg?style=flat-square" alt="Version"/>
  </a>
  <a href="https://creativecommons.org/licenses/by-nc-sa/4.0/">
    <img src="https://img.shields.io/badge/License-CC%20BY--NC--SA%204.0-orange.svg?style=flat-square" alt="License"/>
  </a>
  <img src="https://img.shields.io/badge/Platform-Android%206+-brightgreen.svg?style=flat-square" alt="Platform"/>
  <img src="https://img.shields.io/badge/Tech-Jetpack%20Compose-4285F4.svg?style=flat-square" alt="Tech"/>
</p>

<p align="center">
  <a href="https://github.com/iCurrer/OMaster/releases">
    <b>⬇️ Download Latest Version</b>
  </a>
</p>

---

## 📸 Tired of searching for camera parameters?

Every time you want to take a satisfying photo, you have to search through the vast ocean of internet information like **finding a needle in a haystack**, wasting time and not necessarily finding the right one 😫

Now, **OMaster** provides you with a clean and simple platform where all data is clear at a glance, helping you say goodbye to parameter anxiety ✨

---

## ✨ Core Features

### 🎨 Rich Preset Library
- **19+ Professional Presets** - Covering film, vintage, fresh, B&W, food and more styles
- **Pro & Auto Dual Modes** - Support both professional and automatic mode parameters
- **New Preset Marking** - New presets display NEW badge and are pinned to top

### ☁️ Cloud Configuration Updates
- Support fetching latest configurations from cloud
- Support custom update sources

### ⭐ Favorites Management
- One-click favorite for liked presets
- Quick access to commonly used parameters
- Local storage, no network required

### 🛠️ Custom Presets
- Create your own color tuning parameters
- Support editing custom presets
- Import custom cover images

### 🔲 Floating Window Mode
- Display parameters in floating window while shooting
- Swipe left/right to switch presets
- Semi-transparent design won't block the viewfinder

### 📱 Clean and Elegant Interface
- Pure black background + Hasselblad Orange accent
- Smooth animation transitions
- Waterfall card layout

---

## 🎬 Feature Preview

| Home Browse | Preset Details | Floating Window |
|---------|---------|--------|
| Waterfall display of all presets | View complete parameters and samples | Reference while shooting |
| Support category filtering | Image carousel showcasing effects | Can be collapsed to floating ball |

---

## 📥 Download & Install

### Method 1: GitHub Releases
Go to [Releases page](https://github.com/iCurrer/OMaster/releases) to download the latest APK

### Method 2: China Mirrors
- Pgyer: [https://www.pgyer.com/omaster-android](https://www.pgyer.com/omaster-android)
- Lanzou: [https://wwbwy.lanzouu.com/b016klqmib](https://wwbwy.lanzouu.com/b016klqmib)

### System Requirements
- Android 6 (API 23) and above
- Supports Master Mode on OPPO / OnePlus / Realme phones

---

## 🛠️ Tech Stack

| Technology | Purpose |
|------|------|
| **Kotlin** | Main development language |
| **Jetpack Compose** | Modern UI framework |
| **Material Design 3** | Design language |
| **Coil** | Image loading |
| **Gson** | JSON parsing |
| **Kotlin Serialization** | Type-safe navigation |

---

## 📋 Parameter Description

OMaster supports the following Master Mode parameters:

| Parameter Category | Specific Parameters |
|---------|---------|
| **Basic Parameters** | Filter, Soft Light, Tone, Saturation, Warm-Cool, Cyan-Magenta, Sharpness, Vignette |
| **Professional Parameters** | ISO, Shutter Speed, Exposure Compensation, Color Temperature, Hue |

---

## 📝 Changelog

See [CHANGELOG.md](CHANGELOG.md)

---

## ❓ FAQ

### Floating window won't turn on?

Some ColorOS / OxygenOS systems may recognize this app as an "unknown source app", thus restricting floating window permission.

**Solution:**

1. Open **Settings** → **Apps** → **App Management**
2. Find **OMaster**, tap **Permission Management**
3. Tap the **⋮** icon in the top right, select **"Remove all authorization restrictions"**
4. Return to the app and re-enable floating window permission

> ⚠️ Note: After removing restrictions, please ensure only "Floating Window" permission is granted. Other sensitive permissions can be granted as needed.

---

## 🔒 Privacy Notice

- All data stored locally, no network required
- Floating window permission only used for displaying parameter window
- Statistics feature requires user consent

---

## 🤝 Contribution Guide

Welcome to submit Issues and Pull Requests!

### Submit New Presets

> [!IMPORTANT]
> Cloud presets have been migrated to an independent repository for maintenance
>
> If you are only updating cloud presets, do not submit Pull Requests directly to the OMaster main repository

If you want to contribute new color tuning presets:
1. Go to [OMaster Community](https://github.com/fengyec2/OMaster-Community)
2. Fork the community repository
3. Add preset data in `presets.json`
4. ~~*Add sample images in `app/src/main/assets/images/`*~~ (to be determined)
5. Submit Pull Request

### Preset Data Format

```json
{
  "presets": [
    {
      "name": "New Preset",
      "coverPath": "images/new_preset_01.webp",
      "galleryImages": [
        "images/new_preset_02.webp",
        "images/new_preset_03.webp"
      ],
      "author": "@Author",
      "mode": "auto",
      "iso": null,
      "shutterSpeed": null,
      "exposureCompensation": null,
      "colorTemperature": null,
      "colorHue": null,
      "whiteBalance": null,
      "colorTone": null,
      "filter": "Filter Type",
      "softLight": "Soft Light Intensity",
      "tone": 0,
      "saturation": 19,
      "warmCool": -5,
      "cyanMagenta": 0,
      "sharpness": 15,
      "vignette": "On",
      "shootingTips": "[Environment] XXXX\n[Scenes] XX, XX, XX, XX\n[Tips] Suitable for XXXX scenes, XXXX colors, recommended XXXX"
    }
  ]
}
```

---

## 📄 License

This project is licensed under [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/)

- **BY** (Attribution) - Must retain copyright notice when using
- **NC** (Non-Commercial) - Commercial use prohibited
- **SA** (ShareAlike) - Modifications must use same license

---

## 🙏 Acknowledgments

- **Asset Providers:**
  - [@OPPO Imaging](https://xhslink.com/m/8c2gJYGlCTR)
  - [@蘭州白鴿](https://xhslink.com/m/4h5lx4Lg37n)
  - [@派瑞特凯](https://xhslink.com/m/AkrgUI0kgg1)
  - [@ONESTEP™](https://xhslink.com/m/4LZ8zRdNCSv)
  - [@盒子叔](https://xhslink.com/m/4mje9mimNXJ)
  - **[@Aurora](https://xhslink.com/m/2Ebow4iyVOE)** ⭐ New

---

## 📞 Contact Us

- Submit [GitHub Issue](https://github.com/iCurrer/OMaster/issues)
- Email: iboy66lee@qq.com

---

<p align="center">
  <b>Made with ❤️ by Silas</b>
</p>

<p align="center">
  <sub>Pure local operation, data stored locally</sub>
</p>
