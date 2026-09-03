# SVG Converter🎨⚡

**Vector Studio** is a modern, lightweight Android application designed for Android developers to seamlessly convert SVG (Scalable Vector Graphics) files and raw SVG code into Android-compatible **VectorDrawable XML** resources.

Built entirely with **Kotlin**, **Jetpack Compose**, and **Material 3**, Vector Studio supports single-file conversions as well as batch processing with ZIP export support.

---

## ✨ Features

- 📄 **Single & Batch Conversion:** Convert single SVG files or select multiple SVGs at once.
- 📋 **Paste Raw SVG:** Instantly convert raw `<svg>` code pasted directly into the app.
- 📦 **ZIP Export:** Export batch-converted VectorDrawable XMLs directly into a neatly named `.zip` archive.
- ⚙️ **Custom Output Preferences:**
  - Force dimensions to standard **24x24dp**.
  - Toggle `<?xml version="1.0"?>` declaration headers.
  - Minify XML output to reduce resource footprint.
  - Automatically strip/monochromatize hardcoded SVG colors.
  - Auto-copy generated XML to clipboard.
- 🎨 **Dynamic Material You Theme:** Native support for Light, Dark, and System Default themes.
- 🛡️ **Clean Architecture:** Lightweight, fast, and secure local file processing.

---

## 🛠️ Tech Stack & Libraries

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Design System:** [Material 3](https://m3.material.io/)
- **Architecture:** Unidirectional Data Flow (UDF) / Clean Architecture
- **Target SDK:** 35
- **Minimum SDK:** 24 (Android 7.0 Nougat)

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 11 or higher

### Installation & Setup

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/YOUR_USERNAME/VectorStudio.git](https://github.com/YOUR_USERNAME/VectorStudio.git)
   
