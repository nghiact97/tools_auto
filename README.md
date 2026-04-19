<div align="center">

# 🤖 JavaRPA Tool

**Robotic Process Automation Desktop Tool**  
*Tự động hóa thao tác màn hình — Java 11 + JavaFX 11*

[![Java](https://img.shields.io/badge/Java-11%2B-orange?logo=openjdk)](https://adoptium.net/)
[![JavaFX](https://img.shields.io/badge/JavaFX-11-blue?logo=java)](https://openjfx.io/)
[![Gradle](https://img.shields.io/badge/Gradle-8.7-02303A?logo=gradle)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Windows-0078D6?logo=windows)](https://www.microsoft.com/windows)

</div>

---

## 📋 Giới Thiệu

**JavaRPA Tool** là ứng dụng desktop giúp **tự động hóa các thao tác trên màn hình máy tính** — tương tự AutoHotkey nhưng được xây dựng hoàn toàn bằng Java. Dành cho mục đích **thí nghiệm và học tập** các kỹ thuật RPA (Robotic Process Automation).

---

## ✨ Tính Năng

| # | Tính năng | Mô tả |
|---|-----------|-------|
| 📸 | **Screen Capture** | Chụp toàn màn hình hoặc vùng bất kỳ |
| 🎨 | **Pixel Detection** | Đọc màu pixel, tìm kiếm màu, chờ màu xuất hiện |
| 🖱️ | **Robot Actions** | Click, double click, right click, type, hotkey, scroll, drag & drop |
| 🎙️ | **Macro Recorder** | Ghi lại toàn bộ thao tác chuột + bàn phím |
| ▶️ | **Macro Player** | Phát lại macro với tốc độ tùy chỉnh (0.25x → 4x) |
| 📖 | **OCR Reader** | Đọc text từ màn hình (Tesseract engine) |
| ⌨️ | **Global Hotkey** | Phím tắt hoạt động ngay cả khi app không được focus |
| 🔑 | **License System** | Key offline HWID-based + mã hóa AES-128 |
| ⚡ | **Task Engine** | Chạy chuỗi thao tác tự động có loop, delay, callback |

---

## 🚀 Bắt Đầu Nhanh

### Yêu Cầu

- **JDK 11+** — [Tải tại Adoptium](https://adoptium.net/temurin/releases/?version=11)
- **Windows 10/11**

### Chạy Ứng Dụng

```powershell
# Clone project
git clone https://github.com/nghiact97/tools_auto.git
cd tools_auto

# Chạy ngay (Gradle tự download)
.\gradlew.bat run

# Hoặc double-click
run.bat
```

> `run.bat` tự động yêu cầu quyền **Admin** (cần thiết cho global hotkey).

### Build JAR

```powershell
.\gradlew.bat shadowJar
# Output: build/libs/javarpa-1.0.jar
```

---

## ⌨️ Phím Tắt

| Phím | Chức năng |
|------|-----------|
| `F6` | Chạy / Dừng task |
| `F7` | Bắt đầu / Dừng ghi Macro |
| `F8` | **Dừng khẩn cấp** tất cả |

---

## 🏗️ Kiến Trúc

```
src/main/java/com/javarpa/
├── App.java                        # Entry point
├── controller/
│   └── MainController.java         # UI Controller (Dashboard, Macro, Settings)
├── core/
│   ├── ScreenCapture.java          # Chụp màn hình
│   ├── PixelDetector.java          # Phát hiện pixel/màu
│   ├── RobotActions.java           # Click, type, hotkey, scroll
│   ├── OcrReader.java              # OCR (Tess4J wrapper)
│   └── GlobalHotkey.java           # JNativeHook system hotkeys
├── macro/
│   ├── MacroAction.java            # Model action (11 loại)
│   ├── MacroScript.java            # Script JSON save/load
│   ├── MacroRecorder.java          # Ghi thao tác
│   └── MacroPlayer.java            # Phát lại + speed control
├── task/
│   ├── TaskConfig.java             # Fluent builder API
│   └── TaskEngine.java             # Async runner + callbacks
├── license/
│   ├── LicenseManager.java         # Verify key offline
│   ├── KeyGenerator.java           # Admin tool tạo key
│   └── HwidUtil.java               # Hardware ID (MAC + OS)
└── util/
    ├── CryptoUtil.java             # AES-128 encrypt/decrypt
    ├── ImageUtil.java              # Image processing
    └── FileUtil.java               # File I/O
```

---

## 🔑 License System

Hệ thống key **offline** — không cần internet:

```
1. App lấy HWID của máy  →  Hiển thị cho user
2. User gửi HWID         →  Admin tạo key:
   java -cp javarpa-1.0.jar com.javarpa.license.KeyGenerator <HWID> 365
3. User nhập key vào app →  Kích hoạt thành công
```

Key chứa: `AES_encrypt(HWID | expiry_date)`

---

## 🛠️ Tech Stack

| Thành phần | Phiên bản |
|-----------|----------|
| Java | 11 LTS |
| JavaFX | 11 |
| Gradle | 8.7 (wrapper) |
| JNativeHook | 2.2.2 |
| Tess4J (OCR) | 4.5.5 |
| Gson (JSON) | 2.11.0 |
| SLF4J | 2.0.13 |

---

## 📦 Đóng Gói .exe

> Cần **JDK 21** + **WiX Toolset**

```powershell
# Cài JDK 21 từ https://adoptium.net
# Cài WiX: https://wixtoolset.org/releases/

.\gradlew.bat shadowJar
installer\build-exe.bat
# Output: installer\output\JavaRPA-1.0.0.exe
```

---

## 🧪 Test Macro Order

```powershell
# Verify MacroPlayer phát lại đúng thứ tự
.\gradlew.bat run --args="test"
# Hoặc chạy trực tiếp:
java -cp ... com.javarpa.test.MacroOrderTest
```

---

## ⚠️ Disclaimer

Tool này được xây dựng cho mục đích **thí nghiệm và học tập** kỹ thuật RPA. Người dùng chịu trách nhiệm về việc sử dụng tool theo đúng pháp luật và điều khoản dịch vụ của các ứng dụng liên quan.

---

<div align="center">

Made with ☕ Java · 🤖 RPA · ❤️ Learning

</div>
