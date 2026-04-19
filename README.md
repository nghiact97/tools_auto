# JavaRPA Tool

🤖 **Robotic Process Automation Desktop Tool** — Java 21 + JavaFX 21

## Tính năng

| Feature | Mô tả |
|---|---|
| 📸 Screen Capture | Chụp toàn màn hình / vùng |
| 🎨 Pixel Detection | Đọc màu pixel, tìm màu, so sánh vùng |
| 🖱️ Robot Actions | Click, type, hotkey, scroll, drag & drop |
| 🎙️ Macro Recorder | Ghi toàn bộ thao tác chuột + phím |
| ▶️ Macro Player | Phát lại macro với tốc độ tùy chỉnh |
| 📖 OCR Reader | Đọc text từ màn hình (Tess4J) |
| ⌨️ Global Hotkey | Phím tắt toàn cục (JNativeHook) |
| 🔑 License System | Key offline HWID-based + AES |
| ⚡ Task Engine | Chạy task tự động có loop, delay, callback |

## Yêu cầu

- **JDK 21** (LTS) — [Tải tại đây](https://adoptium.net/)
- **Windows 10/11**
- (Tùy chọn) WiX Toolset để đóng gói .exe

## Cài JDK 21

1. Tải JDK 21 từ: https://adoptium.net/temurin/releases/?version=21
2. Chọn **Windows x64 JDK .msi**
3. Cài đặt, mặc định sẽ set `JAVA_HOME` tự động
4. Kiểm tra: `java --version` → phải hiển thị `21`

## Build & Chạy

```bash
# Chạy trong development
gradlew.bat run

# Build fat JAR
gradlew.bat shadowJar

# Đóng gói .exe (cần WiX Toolset)
installer\build-exe.bat
```

## Phím tắt

| Phím | Chức năng |
|---|---|
| F6 | Chạy / Dừng task |
| F7 | Bắt đầu / Dừng ghi Macro |
| F8 | Dừng khẩn cấp tất cả |

## Cấu trúc Project

```
src/main/java/com/javarpa/
├── App.java                    # Entry point
├── controller/
│   └── MainController.java     # Main UI controller
├── core/
│   ├── ScreenCapture.java      # Chụp màn hình
│   ├── PixelDetector.java      # Phát hiện pixel/màu
│   ├── RobotActions.java       # Click, type, hotkey
│   ├── OcrReader.java          # OCR (Tess4J)
│   └── GlobalHotkey.java       # Phím tắt toàn cục
├── macro/
│   ├── MacroAction.java        # Model action
│   ├── MacroScript.java        # Script (JSON)
│   ├── MacroRecorder.java      # Ghi thao tác
│   └── MacroPlayer.java        # Phát lại
├── task/
│   ├── TaskConfig.java         # Cấu hình task
│   └── TaskEngine.java         # Chạy task
├── license/
│   ├── LicenseManager.java     # Quản lý key
│   ├── KeyGenerator.java       # Tạo key (admin)
│   └── HwidUtil.java           # Hardware ID
└── util/
    ├── CryptoUtil.java         # AES encryption
    ├── ImageUtil.java          # Xử lý ảnh
    └── FileUtil.java           # File I/O
```

## License System

1. App lấy HWID → Hiển thị cho user
2. User gửi HWID → Admin dùng `KeyGenerator` tạo key:
   ```bash
   java -cp javarpa.jar com.javarpa.license.KeyGenerator <HWID> 365
   ```
3. User nhập key vào app → Kích hoạt
