package com.javarpa.controller;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.javarpa.core.GlobalHotkey;
import com.javarpa.core.PixelDetector;
import com.javarpa.core.ScreenCapture;
import com.javarpa.license.LicenseManager;
import com.javarpa.macro.MacroPlayer;
import com.javarpa.macro.MacroRecorder;
import com.javarpa.macro.MacroScript;
import com.javarpa.task.TaskEngine;
import com.javarpa.util.ImageUtil;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main JavaFX controller — wires together all UI elements and services.
 */
public class MainController implements Initializable {

    // === FXML Bindings: Header ===
    @FXML private Label labelStatus;
    @FXML private Label labelLicense;
    @FXML private Label labelTime;

    // === FXML Bindings: Dashboard ===
    @FXML private Label labelTaskName;
    @FXML private Label labelLoop;
    @FXML private Label labelElapsed;
    @FXML private ProgressBar progressTask;
    @FXML private Label labelMousePos;
    @FXML private Pane pixelColorBox;
    @FXML private Label labelPixelColor;
    @FXML private TextArea textOcrResult;
    @FXML private ImageView previewImage;
    @FXML private Button btnRun;
    @FXML private Button btnPause;
    @FXML private Button btnStop;
    @FXML private Button btnRecord;
    @FXML private Button btnOpenScript;
    @FXML private Label labelHwid;
    @FXML private TextField fieldLicenseKey;
    @FXML private Label labelLicenseInfo;

    // === FXML Bindings: Macro Tab ===
    @FXML private Button btnMacroRecord;
    @FXML private Button btnMacroStop;
    @FXML private Button btnMacroPlay;
    @FXML private Button btnMacroSave;
    @FXML private Button btnMacroLoad;
    @FXML private Slider sliderSpeed;
    @FXML private Label labelSpeed;
    @FXML private Label labelMacroName;
    @FXML private Label labelMacroCount;
    @FXML private ListView<String> listMacroActions;
    @FXML private Spinner<Integer> spinnerRepeat;
    @FXML private CheckBox checkLoop;

    // === FXML Bindings: Settings ===
    @FXML private ComboBox<String> comboHotkeyRun;
    @FXML private ComboBox<String> comboHotkeyRecord;
    @FXML private ComboBox<String> comboHotkeyEmergency;
    @FXML private Spinner<Integer> spinnerActionDelay;
    @FXML private ComboBox<String> comboOcrLang;
    @FXML private TextField fieldTessPath;

    // === FXML Bindings: Status Bar ===
    @FXML private Label labelLog;

    // === Services ===
    private final MacroRecorder macroRecorder = new MacroRecorder();
    private final MacroPlayer macroPlayer = new MacroPlayer();
    private final TaskEngine taskEngine = new TaskEngine();
    private MacroScript currentMacro = null;
    private boolean taskRunning = false;

    // === Timers ===
    private Timer uiTimer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupHwid();
        setupLicense();
        setupMouseTracker();
        setupClock();
        setupHotkeys();
        setupMacroTab();
        setupSettingsTab();
        checkLicenseStatus();
        log("JavaRPA Tool đã khởi động.");
    }

    // =============================================
    // INITIALIZATION
    // =============================================

    private void setupHwid() {
        String hwid = LicenseManager.getHWID();
        labelHwid.setText(hwid);
    }

    private void setupLicense() {
        if (LicenseManager.isActivated()) {
            int days = LicenseManager.getDaysRemaining();
            updateLicenseUI(true, "✅ Đã kích hoạt — còn " + days + " ngày");
        }
    }

    private void setupMouseTracker() {
        // Track mouse position and pixel color every 100ms
        uiTimer = new Timer("UI-Tracker", true);
        uiTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    Point mouse = MouseInfo.getPointerInfo().getLocation();
                    Color pixel = PixelDetector.getColor(mouse.x, mouse.y);
                    String hex = ImageUtil.colorToHex(pixel);
                    String javafxHex = hex.replace("#", "");

                    Platform.runLater(() -> {
                        labelMousePos.setText("X: " + mouse.x + ", Y: " + mouse.y);
                        labelPixelColor.setText(hex);
                        pixelColorBox.setStyle("-fx-background-color: " + hex + "; -fx-background-radius: 4;");
                    });
                } catch (Exception ignored) {}
            }
        }, 0, 100);
    }

    private void setupClock() {
        uiTimer.scheduleAtFixedRate(new TimerTask() {
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss")
                                            .withZone(ZoneId.systemDefault());
            @Override
            public void run() {
                String time = fmt.format(Instant.now());
                Platform.runLater(() -> labelTime.setText(time));
            }
        }, 0, 1000);
    }

    private void setupHotkeys() {
        GlobalHotkey hotkey = GlobalHotkey.getInstance();
        // F6: Run/Stop toggle
        hotkey.register(NativeKeyEvent.VC_F6, () -> Platform.runLater(this::onRun));
        // F7: Record macro toggle
        hotkey.register(NativeKeyEvent.VC_F7, () -> Platform.runLater(this::onMacroRecord));
        // F8: Emergency stop
        hotkey.register(NativeKeyEvent.VC_F8, this::emergencyStop);
    }

    private void setupMacroTab() {
        sliderSpeed.valueProperty().addListener((obs, old, val) -> {
            double speed = Math.round(val.doubleValue() * 4) / 4.0;
            labelSpeed.setText(String.format("%.2fx", speed));
            macroPlayer.setSpeed(speed);
        });
    }

    private void setupSettingsTab() {
        comboHotkeyRun.getItems().addAll("F6", "F5", "F9", "F10");
        comboHotkeyRun.setValue("F6");
        comboHotkeyRecord.getItems().addAll("F7", "F8", "F9");
        comboHotkeyRecord.setValue("F7");
        comboHotkeyEmergency.getItems().addAll("F8", "Escape", "F12");
        comboHotkeyEmergency.setValue("F8");
        comboOcrLang.getItems().addAll("eng", "vie", "eng+vie");
        comboOcrLang.setValue("eng+vie");
    }

    private void checkLicenseStatus() {
        if (!LicenseManager.isActivated()) {
            labelLicense.setText("⚠ Chưa kích hoạt");
            labelLicense.getStyleClass().setAll("license-warn");
        } else {
            int days = LicenseManager.getDaysRemaining();
            labelLicense.setText("✅ " + days + " ngày còn lại");
            labelLicense.getStyleClass().setAll("license-ok");
        }
    }

    // =============================================
    // DASHBOARD ACTIONS
    // =============================================

    @FXML
    private void onRun() {
        if (taskRunning) {
            taskEngine.stop();
            taskRunning = false;
            setStatus("⏹ Đã dừng", "status-idle");
            btnRun.setText("▶ Chạy");
            log("Task đã dừng.");
        } else {
            taskRunning = true;
            setStatus("● Đang chạy", "status-running");
            btnRun.setText("⏹ Dừng");
            labelTaskName.setText("Demo Task");
            log("Task đang chạy...");
            // TODO: integrate actual TaskConfig from user config
        }
    }

    @FXML
    private void onPause() {
        taskEngine.togglePause();
        TaskEngine.Status s = taskEngine.getStatus();
        if (s == TaskEngine.Status.PAUSED) {
            setStatus("⏸ Tạm dừng", "status-idle");
            log("Task tạm dừng.");
        } else {
            setStatus("● Đang chạy", "status-running");
            log("Task tiếp tục.");
        }
    }

    @FXML
    private void onStop() {
        taskEngine.stop();
        macroPlayer.stop();
        taskRunning = false;
        setStatus("⏹ Đã dừng", "status-idle");
        btnRun.setText("▶ Chạy");
        progressTask.setProgress(0);
        log("Đã dừng tất cả.");
    }

    @FXML
    private void onRecord() {
        if (!macroRecorder.isRecording()) {
            macroRecorder.start("Quick Macro");
            btnRecord.setText("⏹ Dừng ghi");
            btnRecord.getStyleClass().add("recording-pulse");
            log("🔴 Đang ghi macro...");
        } else {
            currentMacro = macroRecorder.stop();
            btnRecord.setText("🔴 Ghi Macro");
            btnRecord.getStyleClass().remove("recording-pulse");
            refreshMacroList();
            log("Ghi xong: " + currentMacro.getActionCount() + " hành động.");
        }
    }

    @FXML
    private void onOpenScript() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Mở Macro Script");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Macro Files", "*.macro", "*.json")
        );
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            try {
                currentMacro = MacroScript.loadFromFile(file);
                macroPlayer.load(currentMacro);
                refreshMacroList();
                log("Đã mở: " + file.getName());
            } catch (IOException e) {
                log("❌ Lỗi mở file: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onCaptureOcr() {
        new Thread(() -> {
            BufferedImage img = ScreenCapture.captureFullScreen();
            // Show preview
            javafx.scene.image.Image fxImg = SwingFXUtils.toFXImage(img, null);
            Platform.runLater(() -> {
                previewImage.setImage(fxImg);
                textOcrResult.setText("OCR: (Cần tessdata để đọc text)");
            });
            log("📸 Đã chụp màn hình.");
        }).start();
    }

    @FXML
    private void onCopyHwid() {
        String hwid = labelHwid.getText();
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(hwid);
        clipboard.setContent(content);
        log("📋 Đã copy HWID: " + hwid);
    }

    @FXML
    private void onActivate() {
        String key = fieldLicenseKey.getText().trim();
        if (key.isEmpty()) {
            labelLicenseInfo.setText("⚠ Vui lòng nhập key.");
            labelLicenseInfo.setStyle("-fx-text-fill: #d29922;");
            return;
        }
        boolean success = LicenseManager.activate(key);
        if (success) {
            int days = LicenseManager.getDaysRemaining();
            updateLicenseUI(true, "✅ Kích hoạt thành công! Còn " + days + " ngày.");
            log("✅ Kích hoạt thành công! Còn " + days + " ngày.");
        } else {
            labelLicenseInfo.setText("❌ Key không hợp lệ hoặc đã hết hạn.");
            labelLicenseInfo.setStyle("-fx-text-fill: #f85149;");
            log("❌ Kích hoạt thất bại.");
        }
        checkLicenseStatus();
    }

    // =============================================
    // MACRO TAB ACTIONS
    // =============================================

    @FXML
    private void onMacroRecord() {
        if (!macroRecorder.isRecording()) {
            macroRecorder.start("New Macro");
            btnMacroRecord.setText("⏺ Đang ghi...");
            btnMacroRecord.getStyleClass().add("recording-pulse");
            log("🔴 Đang ghi macro...");
        } else {
            currentMacro = macroRecorder.stop();
            btnMacroRecord.setText("🔴 Bắt đầu ghi");
            btnMacroRecord.getStyleClass().remove("recording-pulse");
            macroPlayer.load(currentMacro);
            refreshMacroList();
            log("Ghi xong: " + currentMacro.getActionCount() + " hành động.");
        }
    }

    @FXML
    private void onMacroStopRecord() {
        if (macroRecorder.isRecording()) {
            currentMacro = macroRecorder.stop();
            btnMacroRecord.setText("🔴 Bắt đầu ghi");
            btnMacroRecord.getStyleClass().remove("recording-pulse");
            macroPlayer.load(currentMacro);
            refreshMacroList();
            log("Dừng ghi: " + currentMacro.getActionCount() + " hành động.");
        }
    }

    @FXML
    private void onMacroPlay() {
        if (currentMacro == null) {
            log("⚠ Không có macro để phát. Hãy ghi hoặc mở file macro.");
            return;
        }
        macroPlayer.load(currentMacro);
        int repeat = checkLoop.isSelected() ? -1 : spinnerRepeat.getValue();
        macroPlayer.playLoop(repeat);
        log("▶ Đang phát macro: " + currentMacro.getName());
    }

    @FXML
    private void onMacroSave() {
        if (currentMacro == null) {
            log("⚠ Không có macro để lưu.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Lưu Macro Script");
        chooser.setInitialFileName(currentMacro.getName().replace(" ", "_") + ".macro");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Macro Files", "*.macro")
        );
        File file = chooser.showSaveDialog(null);
        if (file != null) {
            try {
                currentMacro.saveToFile(file);
                log("💾 Đã lưu: " + file.getName());
            } catch (IOException e) {
                log("❌ Lỗi lưu: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onMacroLoad() {
        onOpenScript();  // Reuse same dialog
    }

    // =============================================
    // SETTINGS ACTIONS
    // =============================================

    @FXML
    private void onSaveHotkeys() {
        log("💾 Đã lưu cài đặt phím tắt.");
    }

    @FXML
    private void onSaveSettings() {
        log("💾 Đã lưu cài đặt.");
    }

    @FXML
    private void onBrowseTessdata() {
        javafx.stage.DirectoryChooser chooser = new javafx.stage.DirectoryChooser();
        chooser.setTitle("Chọn thư mục tessdata");
        File dir = chooser.showDialog(null);
        if (dir != null) {
            fieldTessPath.setText(dir.getAbsolutePath());
            log("Tessdata path: " + dir.getAbsolutePath());
        }
    }

    // =============================================
    // HELPERS
    // =============================================

    private void emergencyStop() {
        Platform.runLater(() -> {
            macroPlayer.stop();
            taskEngine.stop();
            macroRecorder.stop();
            taskRunning = false;
            setStatus("⏹ Dừng khẩn cấp", "status-error");
            btnRun.setText("▶ Chạy");
            log("🛑 Dừng khẩn cấp!");
        });
    }

    private void setStatus(String text, String styleClass) {
        Platform.runLater(() -> {
            labelStatus.setText(text);
            labelStatus.getStyleClass().setAll(styleClass);
        });
    }

    private void log(String message) {
        Platform.runLater(() -> labelLog.setText(message));
        System.out.println("[JavaRPA] " + message);
    }

    private void updateLicenseUI(boolean activated, String message) {
        Platform.runLater(() -> {
            labelLicenseInfo.setText(message);
            if (activated) {
                labelLicenseInfo.setStyle("-fx-text-fill: #3fb950;");
            }
        });
    }

    private void refreshMacroList() {
        if (currentMacro == null) return;
        Platform.runLater(() -> {
            listMacroActions.getItems().clear();
            for (int i = 0; i < currentMacro.getActions().size(); i++) {
                listMacroActions.getItems().add(
                    String.format("%3d. %s", i + 1, currentMacro.getActions().get(i).toString())
                );
            }
            labelMacroName.setText(currentMacro.getName());
            labelMacroCount.setText(currentMacro.getActionCount() + " hành động");
        });
    }
}
