# ChronoDots 📅✨

<p align="center">
  <img src="https://readme-typing-svg.herokuapp.com?font=Fira+Code&pause=1000&color=2196F3&background=00000000&center=true&vCenter=true&width=435&lines=Premium+Calendar+Widgets;Glassmorphism+%2B+Material+You;Year+in+Pixels;Perfect+Dots+Aesthetic" alt="Typing Animation" />
</p>

> **Time, visualized beautifully.**  
> A premium "Year in Pixels" widget suite for Android, featuring iOS-style Glassmorphism and pixel-perfect rendering.

![Android API](https://img.shields.io/badge/API-26%2B-green.svg?style=for-the-badge&logo=android)
![Java](https://img.shields.io/badge/Java-17-orange.svg?style=for-the-badge&logo=java)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-purple.svg?style=for-the-badge&logo=kotlin)
![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)

---

## 🎨 Visual Excellence

ChronoDots brings the elegance of iOS widgets to Android with a custom rendering engine designed for aesthetics and performance.

### 🔮 Glassmorphism "Liquid Glass"
- **Real-time Blur**: Dynamically samples your wallpaper to create a frosted glass effect.
- **Hardware Acceleration**: Uses Android 12+ `RenderEffect` for butter-smooth 60fps rendering.
- **Reflection Fallback**: Uses advanced Java Reflection to safely downgrade on older devices without crashing.
- **iOS Squircle Corners**: continuous curvature (22dp radius) clipped perfectly to match modern system aesthetics.

### 🎭 Chameleon Mode
- **Material You Integration**: Automatically extracts colors from your wallpaper.
- **Dynamic Harmony**: Generates complementary palettes for dots and accents that always look good.

---

## 🚀 Key Features

| Widget | Description |
|--------|-------------|
| **Year View** | A 365-day heat map of your year. Visualize progress at a glance. |
| **Month View** | Focused current-month view with emoji status indicators. |
| **Week Strip** | A compact, horizontal ribbon for your immediate schedule. |

- **Zero-Jank Resizing**: Custom `WidgetResizeHandler` ensures smooth resizing on Nova, OneUI, and Pixel Launcher.
- **Smart Typography**: Implements "San Francisco" style font metrics with custom letter spacing (0.03em).
- **Pixel Perfect**: `ANTI_ALIAS`, `DITHER`, and `FILTER_BITMAP` flags enabled globally.

---

## 🛠️ Technical Engineering

This isn't just a pretty face. Under the hood, ChronoDots is built for stability and cross-device compatibility.

### 📱 Cross-Device Engine (`DeviceCompatHelper`)
We handle the quirks so you don't have to. Optimized for:
- **Samsung OneUI**: Enforces dimension floors to prevent undersized widgets.
- **Xiaomi / MIUI**: Handles dense grid scaling and aggressive battery restrictions.
- **OnePlus / OxygenOS**: Corrects DPI reporting errors.
- **Huawei / EMUI**: Graceful fallback for devices restricting blur effects.

### 🧠 Intelligent Memory Management
- **Low-RAM Mode**: Devices with <3GB RAM automatically switch to a high-performance "Solid Glass" rendering mode.
- **OOM Protection**: Large 4K wallpapers are intelligently downscaled before processing.

---

## 💻 Building the Project

1. **Prerequisites**: Android Studio Hedgehog+ | JDK 17
2. **Clone the repo**:
   ```bash
   git clone https://github.com/Harmitx7/ChronoDots.git
   ```
3. **Build**:
   The project uses a mixed Kotlin/Java codebase. If you encounter build errors, run:
   **Build > Clean Project** to flush the compiler cache.

---

## 📅 Roadmap

- [x] **Glassmorphism V2**: Hardware rendering & Noise textures
- [x] **Smart Resizing**: Launcher-aware throttling
- [x] **Theme Engine**: Dynamic colors & presets
- [ ] **Interactive Dots**: Click-to-log functionality
- [ ] **Wear OS**: Companion watch face

---

<p align="center">Made with ❤️ for Android</p>
