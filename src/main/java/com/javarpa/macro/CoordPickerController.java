package com.javarpa.macro;

import com.javarpa.core.PixelDetector;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Color;

/**
 * Controller cho panel "🎯 Game Coordinate Picker" trong tab Macro.
 *
 * <p>Cho phép người dùng:
 * <ul>
 *   <li>Capture tọa độ + màu pixel của bất kỳ điểm nào trên màn hình</li>
 *   <li>Đặt nhãn mô tả cho từng điểm (VD: "Nút PLAY", "Ô username")</li>
 *   <li>Xem danh sách đầy đủ trong TableView</li>
 *   <li>Gửi tọa độ đã chọn thẳng sang trường tương ứng trong Game Bot</li>
 *   <li>Copy toàn bộ danh sách dưới dạng JSON ra clipboard</li>
 * </ul>
 * </p>
 *
 * <p>Được khởi tạo thủ công bởi MainController và nhận FXML controls qua inject().</p>
 */
public class CoordPickerController {

    // ===== INJECTED FXML CONTROLS =====

    /** TextField để user nhập tên/nhãn cho điểm cần bắt */
    private TextField fieldPickerLabel;

    /** Nút bắt đầu đếm ngược + capture */
    private Button btnPickerCapture;

    /** Nút bắt liên tục (capture mỗi giây trong N giây) */
    private Button btnPickerContinuous;

    /** Label đếm ngược / trạng thái */
    private Label labelPickerStatus;

    /** TableView hiển thị danh sách entry đã capture */
    private TableView<CoordinateEntry> tableCoords;

    /** TableColumn cho index (số thứ tự) */
    private TableColumn<CoordinateEntry, String> colIndex;

    /** TableColumn cho X */
    private TableColumn<CoordinateEntry, String> colX;

    /** TableColumn cho Y */
    private TableColumn<CoordinateEntry, String> colY;

    /** TableColumn cho màu HEX + ô màu */
    private TableColumn<CoordinateEntry, String> colColor;

    /** TableColumn cho label */
    private TableColumn<CoordinateEntry, String> colLabel;

    /** TableColumn cho thời gian */
    private TableColumn<CoordinateEntry, String> colTime;

    /** Pane hiển thị màu pixel của entry đang chọn */
    private Pane paneSelectedColor;

    /** Label hex của entry đang chọn */
    private Label labelSelectedHex;

    /** ComboBox chọn trường Game Bot để gửi tọa độ vào */
    private ComboBox<String> comboSendTarget;

    /** Nút gửi tọa độ sang Game Bot */
    private Button btnSendToBot;

    /** Nút copy JSON */
    private Button btnCopyJson;

    /** Nút xóa dòng đang chọn */
    private Button btnDeleteEntry;

    /** Nút xóa toàn bộ danh sách */
    private Button btnClearAll;

    // ===== GAME BOT FIELD REFERENCES (inject từ GameBotController) =====

    private javafx.scene.control.TextField gameBotUsernameX, gameBotUsernameY;
    private javafx.scene.control.TextField gameBotPasswordX, gameBotPasswordY;
    private javafx.scene.control.TextField gameBotLoginBtnX, gameBotLoginBtnY;
    private javafx.scene.control.TextField gameBotServerX,   gameBotServerY;
    private javafx.scene.control.TextField gameBotEnterGameX, gameBotEnterGameY;
    private javafx.scene.control.TextField gameBotLoginDetectX, gameBotLoginDetectY, gameBotLoginDetectHex;
    private javafx.scene.control.TextField gameBotServerDetectX, gameBotServerDetectY, gameBotServerDetectHex;

    /** Hàm log để ghi vào status bar của MainController */
    private java.util.function.Consumer<String> logConsumer;

    // ===== INTERNAL STATE =====

    /** Danh sách entries đã capture (observable để TableView tự cập nhật) */
    private final ObservableList<CoordinateEntry> entries = FXCollections.observableArrayList();

    /** Tên các trường Game Bot (hiển thị trong combo gửi) */
    public static final String[] GAME_BOT_FIELDS = {
        "Ô Username (X,Y)",
        "Ô Password (X,Y)",
        "Nút Login (X,Y)",
        "Click Server (X,Y)",
        "Nút Vào Game (X,Y)",
        "Login Detect Pixel (X,Y,HEX)",
        "Server Detect Pixel (X,Y,HEX)"
    };

    private volatile boolean capturing = false;

    // ===== INJECT =====

    /**
     * Inject tất cả FXML controls của Coordinate Picker panel.
     * Gọi từ MainController trước khi gọi initialize().
     */
    public void inject(
            TextField fieldPickerLabel,
            Button btnPickerCapture,
            Button btnPickerContinuous,
            Label labelPickerStatus,
            TableView<CoordinateEntry> tableCoords,
            TableColumn<CoordinateEntry, String> colIndex,
            TableColumn<CoordinateEntry, String> colX,
            TableColumn<CoordinateEntry, String> colY,
            TableColumn<CoordinateEntry, String> colColor,
            TableColumn<CoordinateEntry, String> colLabel,
            TableColumn<CoordinateEntry, String> colTime,
            Pane paneSelectedColor,
            Label labelSelectedHex,
            ComboBox<String> comboSendTarget,
            Button btnSendToBot,
            Button btnCopyJson,
            Button btnDeleteEntry,
            Button btnClearAll,
            java.util.function.Consumer<String> log) {

        this.fieldPickerLabel    = fieldPickerLabel;
        this.btnPickerCapture    = btnPickerCapture;
        this.btnPickerContinuous = btnPickerContinuous;
        this.labelPickerStatus   = labelPickerStatus;
        this.tableCoords         = tableCoords;
        this.colIndex            = colIndex;
        this.colX                = colX;
        this.colY                = colY;
        this.colColor            = colColor;
        this.colLabel            = colLabel;
        this.colTime             = colTime;
        this.paneSelectedColor   = paneSelectedColor;
        this.labelSelectedHex    = labelSelectedHex;
        this.comboSendTarget     = comboSendTarget;
        this.btnSendToBot        = btnSendToBot;
        this.btnCopyJson         = btnCopyJson;
        this.btnDeleteEntry      = btnDeleteEntry;
        this.btnClearAll         = btnClearAll;
        this.logConsumer         = log;
    }

    /**
     * Inject references đến các TextField của Game Bot tab.
     * Gọi SAU khi GameBotController.injectFields() đã được gọi.
     */
    public void injectGameBotFields(
            javafx.scene.control.TextField usernameX, javafx.scene.control.TextField usernameY,
            javafx.scene.control.TextField passwordX, javafx.scene.control.TextField passwordY,
            javafx.scene.control.TextField loginBtnX, javafx.scene.control.TextField loginBtnY,
            javafx.scene.control.TextField serverX,   javafx.scene.control.TextField serverY,
            javafx.scene.control.TextField enterGameX, javafx.scene.control.TextField enterGameY,
            javafx.scene.control.TextField loginDetectX, javafx.scene.control.TextField loginDetectY,
            javafx.scene.control.TextField loginDetectHex,
            javafx.scene.control.TextField serverDetectX, javafx.scene.control.TextField serverDetectY,
            javafx.scene.control.TextField serverDetectHex) {

        this.gameBotUsernameX    = usernameX;
        this.gameBotUsernameY    = usernameY;
        this.gameBotPasswordX    = passwordX;
        this.gameBotPasswordY    = passwordY;
        this.gameBotLoginBtnX    = loginBtnX;
        this.gameBotLoginBtnY    = loginBtnY;
        this.gameBotServerX      = serverX;
        this.gameBotServerY      = serverY;
        this.gameBotEnterGameX   = enterGameX;
        this.gameBotEnterGameY   = enterGameY;
        this.gameBotLoginDetectX   = loginDetectX;
        this.gameBotLoginDetectY   = loginDetectY;
        this.gameBotLoginDetectHex = loginDetectHex;
        this.gameBotServerDetectX   = serverDetectX;
        this.gameBotServerDetectY   = serverDetectY;
        this.gameBotServerDetectHex = serverDetectHex;
    }

    // ===== INIT =====

    /** Khởi tạo bindings và listeners. Gọi sau inject(). */
    public void initialize() {
        setupTable();
        setupCombo();
        setupSelectionListener();
    }

    // ===== TABLE SETUP =====

    @SuppressWarnings("unchecked")
    private void setupTable() {
        // Gán CellValueFactory cho từng cột
        colIndex.setCellValueFactory(cell -> {
            int idx = tableCoords.getItems().indexOf(cell.getValue()) + 1;
            return new javafx.beans.property.SimpleStringProperty(String.valueOf(idx));
        });
        colX.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(String.valueOf(cell.getValue().getX())));
        colY.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(String.valueOf(cell.getValue().getY())));
        colColor.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getHexColor()));
        colLabel.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getLabel()));
        colTime.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getCapturedAtStr()));

        // Custom cell factory cho cột màu: hiển thị ô màu + hex
        colColor.setCellFactory(col -> new TableCell<CoordinateEntry, String>() {
            private final Pane colorBox = new Pane();
            private final Label hexLabel = new Label();
            private final javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(6, colorBox, hexLabel);
            {
                colorBox.setPrefSize(14, 14);
                colorBox.setStyle("-fx-border-color: #30363d; -fx-border-width:1; -fx-border-radius:3; -fx-background-radius:3;");
                hexLabel.setStyle("-fx-text-fill: #58a6ff; -fx-font-family:'JetBrains Mono','Courier New',monospace; -fx-font-size:11px;");
                hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            }
            @Override
            protected void updateItem(String hex, boolean empty) {
                super.updateItem(hex, empty);
                if (empty || hex == null) { setGraphic(null); return; }
                colorBox.setStyle(colorBox.getStyle() + " -fx-background-color:" + hex + ";");
                hexLabel.setText(hex);
                setGraphic(hbox);
            }
        });

        // Zebra striping + selection style
        tableCoords.setRowFactory(tv -> {
            TableRow<CoordinateEntry> row = new TableRow<>();
            row.itemProperty().addListener((obs, old, item) -> {
                if (item == null) row.setStyle("");
            });
            return row;
        });

        tableCoords.setItems(entries);
        tableCoords.setPlaceholder(new Label("Chưa có tọa độ. Nhấn [🎯 Bắt tọa độ] để bắt đầu."));
    }

    private void setupCombo() {
        comboSendTarget.getItems().addAll(GAME_BOT_FIELDS);
        comboSendTarget.setValue(GAME_BOT_FIELDS[0]);
    }

    private void setupSelectionListener() {
        tableCoords.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) return;
            // Cập nhật ô màu preview
            if (paneSelectedColor != null) {
                paneSelectedColor.setStyle(
                    "-fx-background-color:" + sel.getHexColor() + ";" +
                    "-fx-border-color:#30363d;-fx-border-width:1;-fx-border-radius:4;-fx-background-radius:4;"
                );
            }
            if (labelSelectedHex != null) {
                labelSelectedHex.setText(sel.getHexColor());
            }
        });
    }

    // ===== CAPTURE ACTIONS =====

    /**
     * Bắt đầu đếm ngược 3 giây rồi capture tọa độ + màu pixel.
     * Gọi từ FXML handler onPickerCapture() trong MainController.
     */
    public void onCapture() {
        if (capturing) return;
        String label = fieldPickerLabel.getText().trim();
        if (label.isEmpty()) label = "Điểm " + (entries.size() + 1);
        final String finalLabel = label;

        capturing = true;
        btnPickerCapture.setDisable(true);
        btnPickerContinuous.setDisable(true);
        log("🎯 Di chuột đến [" + finalLabel + "] trong 3 giây...");

        String countdownStyle = "-fx-text-fill: #d29922; -fx-font-weight:700;";
        String readyStyle = "-fx-text-fill: #3fb950; -fx-font-weight:700;";

        new Thread(() -> {
            try {
                for (int i = 3; i >= 1; i--) {
                    final int sec = i;
                    Platform.runLater(() -> {
                        labelPickerStatus.setText("⏳ " + sec + "s...");
                        labelPickerStatus.setStyle(countdownStyle);
                    });
                    Thread.sleep(1000);
                }
                // Capture
                Point p = MouseInfo.getPointerInfo().getLocation();
                Color c = PixelDetector.getColor(p.x, p.y);
                String hex = String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
                CoordinateEntry entry = new CoordinateEntry(finalLabel, p.x, p.y, hex);

                Platform.runLater(() -> {
                    entries.add(entry);
                    tableCoords.getSelectionModel().selectLast();
                    tableCoords.scrollTo(entries.size() - 1);
                    labelPickerStatus.setText("✅ (" + p.x + ", " + p.y + ")  " + hex);
                    labelPickerStatus.setStyle(readyStyle);
                    fieldPickerLabel.clear();
                    log("  ✅ Captured [" + finalLabel + "]: X=" + p.x + ", Y=" + p.y + ", Color=" + hex);
                    btnPickerCapture.setDisable(false);
                    btnPickerContinuous.setDisable(false);
                });
            } catch (InterruptedException ignored) {
            } finally {
                capturing = false;
                Platform.runLater(() -> {
                    btnPickerCapture.setDisable(false);
                    btnPickerContinuous.setDisable(false);
                });
            }
        }, "CoordPicker-Thread").start();
    }

    /**
     * Capture liên tục mỗi giây trong 5 giây (tổng 5 điểm).
     * Hữu ích khi cần bắt nhiều nút liên tiếp.
     */
    public void onCaptureContinuous() {
        if (capturing) return;
        capturing = true;
        btnPickerCapture.setDisable(true);
        btnPickerContinuous.setDisable(true);
        log("⚡ Capture liên tục trong 5 giây (1 điểm/giây)...");

        String countStyle  = "-fx-text-fill: #58a6ff; -fx-font-weight:700;";
        String doneStyle   = "-fx-text-fill: #3fb950; -fx-font-weight:700;";

        new Thread(() -> {
            try {
                // Đếm ngược 2 giây trước khi bắt đầu
                for (int i = 2; i >= 1; i--) {
                    final int s = i;
                    Platform.runLater(() -> {
                        labelPickerStatus.setText("🚀 Bắt đầu sau " + s + "s...");
                        labelPickerStatus.setStyle(countStyle);
                    });
                    Thread.sleep(1000);
                }

                int count = 0;
                for (int tick = 1; tick <= 5; tick++) {
                    Point p = MouseInfo.getPointerInfo().getLocation();
                    Color c = PixelDetector.getColor(p.x, p.y);
                    String hex = String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
                    String baseLabel = fieldPickerLabel.getText().trim();
                    if (baseLabel.isEmpty()) baseLabel = "Điểm";
                    final String entryLabel = baseLabel + " #" + tick;
                    final CoordinateEntry entry = new CoordinateEntry(entryLabel, p.x, p.y, hex);
                    final int t = tick;
                    count++;
                    final int captured = count;
                    Platform.runLater(() -> {
                        entries.add(entry);
                        tableCoords.getSelectionModel().selectLast();
                        tableCoords.scrollTo(entries.size() - 1);
                        labelPickerStatus.setText("⚡ " + t + "/5 — (" + p.x + ", " + p.y + ")");
                        labelPickerStatus.setStyle(countStyle);
                        log("  ⚡ #" + t + " [" + entryLabel + "]: X=" + p.x + ", Y=" + p.y + ", " + hex);
                    });
                    Thread.sleep(1000);
                }
                final int total = count;
                Platform.runLater(() -> {
                    labelPickerStatus.setText("✅ Xong: " + total + " điểm đã bắt");
                    labelPickerStatus.setStyle(doneStyle);
                    fieldPickerLabel.clear();
                    btnPickerCapture.setDisable(false);
                    btnPickerContinuous.setDisable(false);
                });
            } catch (InterruptedException ignored) {
            } finally {
                capturing = false;
                Platform.runLater(() -> {
                    btnPickerCapture.setDisable(false);
                    btnPickerContinuous.setDisable(false);
                });
            }
        }, "CoordPicker-Continuous-Thread").start();
    }

    // ===== TABLE ACTIONS =====

    /** Xóa dòng đang chọn trong bảng. */
    public void onDeleteEntry() {
        int idx = tableCoords.getSelectionModel().getSelectedIndex();
        if (idx < 0) return;
        CoordinateEntry removed = entries.remove(idx);
        log("🗑 Đã xóa: [" + removed.getLabel() + "]");
        // Refresh để cập nhật cột index
        tableCoords.refresh();
    }

    /** Xóa toàn bộ danh sách. */
    public void onClearAll() {
        entries.clear();
        labelPickerStatus.setText("Sẵn sàng");
        labelPickerStatus.setStyle("-fx-text-fill: #6e7681;");
        log("🗑 Đã xóa toàn bộ danh sách tọa độ.");
    }

    // ===== SEND TO GAME BOT =====

    /**
     * Lấy entry đang được chọn trong bảng và điền X, Y (và HEX nếu có)
     * vào đúng trường Game Bot tương ứng với giá trị từ comboSendTarget.
     */
    public void onSendToBot() {
        CoordinateEntry sel = tableCoords.getSelectionModel().getSelectedItem();
        if (sel == null) {
            log("⚠ Chưa chọn dòng nào trong bảng.");
            return;
        }
        String target = comboSendTarget.getValue();
        if (target == null) return;

        String xStr = String.valueOf(sel.getX());
        String yStr = String.valueOf(sel.getY());
        String hex  = sel.getHexColor();

        switch (target) {
            case "Ô Username (X,Y)":
                setField(gameBotUsernameX, xStr);
                setField(gameBotUsernameY, yStr);
                break;
            case "Ô Password (X,Y)":
                setField(gameBotPasswordX, xStr);
                setField(gameBotPasswordY, yStr);
                break;
            case "Nút Login (X,Y)":
                setField(gameBotLoginBtnX, xStr);
                setField(gameBotLoginBtnY, yStr);
                break;
            case "Click Server (X,Y)":
                setField(gameBotServerX, xStr);
                setField(gameBotServerY, yStr);
                break;
            case "Nút Vào Game (X,Y)":
                setField(gameBotEnterGameX, xStr);
                setField(gameBotEnterGameY, yStr);
                break;
            case "Login Detect Pixel (X,Y,HEX)":
                setField(gameBotLoginDetectX,   xStr);
                setField(gameBotLoginDetectY,   yStr);
                setField(gameBotLoginDetectHex, hex);
                break;
            case "Server Detect Pixel (X,Y,HEX)":
                setField(gameBotServerDetectX,   xStr);
                setField(gameBotServerDetectY,   yStr);
                setField(gameBotServerDetectHex, hex);
                break;
            default:
                log("⚠ Không nhận ra trường đích: " + target);
                return;
        }
        log("➡ Đã gửi [" + sel.getLabel() + "] → " + target
            + " (X=" + sel.getX() + ", Y=" + sel.getY() + ")");
    }

    // ===== COPY JSON =====

    /** Copy toàn bộ danh sách entries dưới dạng JSON array vào clipboard. */
    public void onCopyJson() {
        if (entries.isEmpty()) {
            log("⚠ Danh sách trống, không có gì để copy.");
            return;
        }
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < entries.size(); i++) {
            sb.append("  ").append(entries.get(i).toJson());
            if (i < entries.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        String json = sb.toString();

        ClipboardContent content = new ClipboardContent();
        content.putString(json);
        Clipboard.getSystemClipboard().setContent(content);
        log("📋 Đã copy " + entries.size() + " entries ra clipboard dưới dạng JSON.");
    }

    // ===== HELPERS =====

    private void setField(javafx.scene.control.TextField field, String value) {
        if (field != null) field.setText(value);
    }

    private void log(String msg) {
        if (logConsumer != null) logConsumer.accept(msg);
    }

    /** Số lượng entries hiện tại. */
    public int getEntryCount() { return entries.size(); }
}
