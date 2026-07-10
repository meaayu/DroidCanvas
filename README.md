# 🎨 DroidCanvas

> An infinite digital canvas for artists to collect, arrange, and organize reference images — built natively for Android.

<div align="center">

![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue)
![Status](https://img.shields.io/badge/status-early%20development-orange)

[📥 Download APK](#installation) • [🚀 Quick Start](#quick-start) • [✨ Features](#features) • [🛣️ Roadmap](#roadmap) • [🤝 Contribute](#contributing)

</div>

---

## 💡 About

DroidCanvas is your creative companion for organizing visual references. Inspired by desktop tools like PureRef, it brings a professional reference board experience directly to your Android device.

**Why DroidCanvas?**
- ✅ **Privacy First** — No accounts, no cloud lock-in. Everything stays on your device
- ✅ **Intuitive** — Familiar gestures and controls from day one
- ✅ **Powerful** — Transform and arrange references with pixel-perfect precision
- ✅ **Reliable** — Your work is automatically saved and always there when you need it

---

## ✨ Features

<table>
  <tr>
    <td width="50%">
      
### 🌌 Infinite Workspace
Pan and zoom freely without borders or limits. Create boards as large as your imagination.

### 👌 Precise Manipulation
- Drag to reposition
- Pinch to scale
- Rotate with natural gestures
- Corner handles for pixel-perfect control

### 💾 Automatic Persistence
Canvas state and all placements saved locally via Room database. Pick up exactly where you left off.

### 📱 Modern UI
Built with Jetpack Compose & Material 3. Supports dynamic dark theming and edge-to-edge rendering.

### 🔄 In-App Updates
Automatically checks GitHub Releases for new versions. One-tap installation from settings.

### 🎭 Creative Tools
- Grayscale toggle
- Flip horizontal/vertical
- Animated GIF support
- Auto-arrange on import
- Multi-image batch import

    </td>
  </tr>
</table>

---

## 🚀 Quick Start

### Installation

**Option 1: Download APK**
```
1. Visit GitHub Releases
2. Download the latest APK
3. Install on your Android device (API 26+)
```

**Option 2: Build from Source**
```bash
git clone https://github.com/meaayu/DroidCanvas.git
cd DroidCanvas
./gradlew assembleDebug
```

**Requirements:** Android 8.0 (API 26) or higher

### Getting Started
1. **Import** — Tap to load images from your gallery
2. **Arrange** — Drag, rotate, and scale references to your liking
3. **Save** — Changes are saved automatically to your device

---

## 🏗️ Architecture

| Component | Technology |
|-----------|-----------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Design System** | Material 3 |
| **Database** | Room (SQLite) |
| **Pattern** | Single-activity, Composable-based |

---

## 🛣️ Roadmap

### ✅ Completed
- [x] Grayscale toggle
- [x] Flip transformations (horizontal/vertical)
- [x] Animated GIF support
- [x] Auto-arrange & packing on import
- [x] Multi-image import

### 🔨 In Development
- [ ] Crop tool
- [ ] Board templates
- [ ] Layer groups
- [ ] Export to image
- [ ] Gesture customization

### 💭 Under Consideration
- [ ] Tablet optimization
- [ ] Keyboard shortcuts
- [ ] Undo/Redo
- [ ] Cloud sync (optional)

**Have a feature idea?** [Open an issue](https://github.com/meaayu/DroidCanvas/issues/new) and let us know!

---

## 🤝 Contributing

We'd love your help! Whether it's code, bug reports, or feature ideas — all contributions matter.

### How to Contribute
1. **Fork** the repository
2. **Create** a feature branch: `git checkout -b feature/amazing-feature`
3. **Commit** your changes: `git commit -m 'Add amazing feature'`
4. **Push** to your branch: `git push origin feature/amazing-feature`
5. **Open** a Pull Request

### Development Setup
- Android Studio Giraffe or newer
- Kotlin 1.9+
- Minimum SDK: API 26
- Target SDK: API 35 (or latest)

---

## 📝 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for full details.

---

## 🙏 Acknowledgments

Built with ❤️ using:
- **Kotlin** — for expressive, safe code
- **Jetpack Compose** — for modern Android UI
- **Material Design 3** — for beautiful, consistent design
- **Open-source community** — for amazing libraries and inspiration

---

<div align="center">

**Questions?** Open an issue or start a discussion!

[⬆ Back to Top](#-droidcanvas)

</div>
