# DroidCanvas

**An infinite digital canvas for artists to collect, arrange, and organize reference images — built natively for Android.**

<!-- Badges: update the URLs once you have a license file and first release -->
![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue)
![Status](https://img.shields.io/badge/status-early%20development-orange)

<!-- Add a hero screenshot or short GIF of the canvas in use here — this is the first thing visitors look at -->
<!-- ![RefCanvas screenshot](docs/screenshot-hero.png) -->

---

## Overview

DroidCanvas gives artists complete freedom to position reference images exactly how they need them for their creative workflow. Inspired by desktop tools like PureRef, it brings the same idea — a distraction-free, boundless corkboard for visual references — natively to Android, built from the ground up to be intuitive, fast, and lightweight.

No accounts, no cloud lock-in. Your boards live on your device.

## Features

- 🌌 **Infinite workspace** — pan and zoom freely with no borders or layout limits
- 👌 **Intuitive manipulation** — drag to reposition, pinch or use corner handles to scale and rotate with pixel-perfect precision
- 💾 **Local persistence** — canvas state, image placements, and settings are saved automatically to a local Room database, so you always pick up where you left off
- 📱 **Modern Material 3 UI** — built natively with Jetpack Compose, with dynamic dark theming and fluid edge-to-edge rendering
- 🔄 **In-app updates** — automatically checks for new versions via the GitHub Releases API, with a manual check available in settings

## Screenshots

<!--
Add 2–4 screenshots or a short demo GIF here once available. Suggested shots:
1. A populated canvas with several arranged references
2. The import flow
3. Dark mode
4. Settings / update check dialog
-->

| Canvas | Import | Dark mode |
|---|---|---|
| _coming soon_ | _coming soon_ | _coming soon_ |

## How it works

1. **Import references** — tap the import action to load images from your device's gallery directly onto the infinite canvas.
2. **Arrange & customize** — move, rotate, and scale references. Use layer controls to bring items forward or send them backward.
3. **Navigate effortlessly** — use natural multi-touch gestures to pan, zoom, and rotate the entire workspace view.

## Installation

### Download

<!-- Once you have a release, link it here -->
Grab the latest APK from the [Releases](https://github.com/meaayu/droidcanvas/releases) page.

### Build from source

```bash
git clone https://github.com/meaayu/droidcanvas.git
cd droidcanvas
```

Open the project in Android Studio (Giraffe or newer recommended) and run it on a device or emulator, or build from the command line:

```bash
./gradlew assembleDebug
```

<!-- Fill in your actual minimum SDK / target SDK once confirmed -->
**Requirements:** Android 8.0 (API 26) or higher.

## Tech stack

- **Language:** Kotlin
- **UI:** Jetpack Compose, Material 3
- **Persistence:** Room
- **Architecture:** <!-- e.g. MVVM, single-activity, etc. — fill in once settled -->

## Roadmap

Planned or under consideration:

- [x] Grayscale toggle
- [x] Flip horizontal / vertical
- [ ] Crop tool
- [x] Animated GIF support
- [x] Auto-arrange / packing on import
- [x] Multi-image import

Have a feature request? Open an issue.

## Contributing

Contributions, bug reports, and feature suggestions are welcome. If you'd like to contribute:

1. Fork the repo
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes
4. Open a pull request

<!-- Add contribution guidelines / code style notes here as the project matures -->

## License

<!-- Confirm and add a LICENSE file to the repo root so this badge and line are accurate -->
This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

Built with ❤️ using Kotlin.
