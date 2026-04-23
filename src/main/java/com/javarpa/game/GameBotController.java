package com.javarpa.game;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.javarpa.core.PixelDetector;
import com.javarpa.util.CryptoUtil;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.awt.MouseInfo;
import java.awt.Point;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller cho Tab "🎮 Game Bot".
 * Được khởi tạo thủ công bởi MainController và nhận FXML fields qua injectFields().
 */
public class GameBotController {

    // ===== INJECTED FXML FIELDS =====

    private ComboBox<GameProfile> comboProfiles;
    private TextField fieldProfileName, fieldGameName, fieldExePath;
    private TextField fieldUsername;
    private PasswordField fieldPassword;
    private ComboBox<String> comboChannel;   // ComboBox chọn channel (thay TextField)
    private TextField fieldUsernameX, fieldUsernameY;
    private TextField fieldPasswordX, fieldPasswordY;
    private TextField fieldLoginBtnX, fieldLoginBtnY;
    private TextField fieldServerX, fieldServerY;
    private TextField fieldEnterGameX, fieldEnterGameY;
    private TextField fieldLoginDetectX, fieldLoginDetectY, fieldLoginDetectHex;
    private TextField fieldServerDetectX, fieldServerDetectY, fieldServerDetectHex;
    private Spinner<Integer> spinnerStepDelay;
    private Spinner<Integer> spinnerWaitTimeout;
    private Spinner<Integer> spinnerLaunchWait;
    private CheckBox checkAutoReconnect;
    private CheckBox checkRunAsAdmin;
    private Button btnStartBot, btnStopBot, btnPauseBot;
    private Label labelBotState;
    private TextArea textBotLog;

    // ===== STATE =====
    private final GameBot bot = new GameBot();
    private final List<GameProfile> profileList = new ArrayList<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PROFILES_FILE = Paths.get(
        System.getProperty("user.home"), ".javarpa", "game_profiles.json"
    );

    // ===== CHANNEL LIST (Crossfire) =====
    private static final String[] CHANNELS = {
        "Tân Binh", "Tự do 1", "Tự do 2", "Tự do 3", "Tự do 4"
    };

    // ===== INJECTION =====

    /**
     * Được gọi bởi MainController để inject tất cả FXML controls.
     * Nhận ComboBox<String> cho channel thay vì TextField.
     */
    public void injectFields(
            ComboBox<GameProfile> comboProfiles,
            TextField fieldProfileName, TextField fieldGameName, TextField fieldExePath,
            TextField fieldUsername, PasswordField fieldPassword, ComboBox<String> comboChannel,
            TextField fieldUsernameX, TextField fieldUsernameY,
            TextField fieldPasswordX, TextField fieldPasswordY,
            TextField fieldLoginBtnX, TextField fieldLoginBtnY,
            TextField fieldServerX,   TextField fieldServerY,
            TextField fieldEnterGameX, TextField fieldEnterGameY,
            TextField fieldLoginDetectX, TextField fieldLoginDetectY, TextField fieldLoginDetectHex,
            TextField fieldServerDetectX, TextField fieldServerDetectY, TextField fieldServerDetectHex,
            Spinner<Integer> spinnerStepDelay, Spinner<Integer> spinnerWaitTimeout, Spinner<Integer> spinnerLaunchWait,
            CheckBox checkAutoReconnect, CheckBox checkRunAsAdmin,
            Button btnStartBot, Button btnStopBot, Button btnPauseBot,
            Label labelBotState, TextArea textBotLog) {

        this.comboProfiles     = comboProfiles;
        this.fieldProfileName  = fieldProfileName;
        this.fieldGameName     = fieldGameName;
        this.fieldExePath      = fieldExePath;
        this.fieldUsername     = fieldUsername;
        this.fieldPassword     = fieldPassword;
        this.comboChannel      = comboChannel;
        this.fieldUsernameX    = fieldUsernameX;
        this.fieldUsernameY    = fieldUsernameY;
        this.fieldPasswordX    = fieldPasswordX;
        this.fieldPasswordY    = fieldPasswordY;
        this.fieldLoginBtnX    = fieldLoginBtnX;
        this.fieldLoginBtnY    = fieldLoginBtnY;
        this.fieldServerX      = fieldServerX;
        this.fieldServerY      = fieldServerY;
        this.fieldEnterGameX   = fieldEnterGameX;
        this.fieldEnterGameY   = fieldEnterGameY;
        this.fieldLoginDetectX = fieldLoginDetectX;
        this.fieldLoginDetectY = fieldLoginDetectY;
        this.fieldLoginDetectHex = fieldLoginDetectHex;
        this.fieldServerDetectX = fieldServerDetectX;
        this.fieldServerDetectY = fieldServerDetectY;
        this.fieldServerDetectHex = fieldServerDetectHex;
        this.spinnerStepDelay   = spinnerStepDelay;
        this.spinnerWaitTimeout  = spinnerWaitTimeout;
        this.spinnerLaunchWait   = spinnerLaunchWait;
        this.checkAutoReconnect  = checkAutoReconnect;
        this.checkRunAsAdmin     = checkRunAsAdmin;
        this.btnStartBot       = btnStartBot;
        this.btnStopBot        = btnStopBot;
        this.btnPauseBot       = btnPauseBot;
        this.labelBotState     = labelBotState;
        this.textBotLog        = textBotLog;
    }

    // ===== INIT =====

    public void initialize() {
        loadProfiles();
        refreshProfileCombo();

        comboProfiles.setOnAction(e -> {
            GameProfile sel = comboProfiles.getValue();
            if (sel != null) fillForm(sel);
        });

        // Setup channel ComboBox
        if (comboChannel != null) {
            comboChannel.getItems().addAll(CHANNELS);
            comboChannel.setValue(CHANNELS[0]); // mặc định "Tân Binh"
        }

        spinnerStepDelay.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(100, 10000, 800, 100));
        spinnerWaitTimeout.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1000, 120000, 15000, 1000));
        spinnerLaunchWait.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1000, 60000, 8000, 1000));

        updateBotUI(GameBot.State.IDLE);
        appendLog("🤖 Game Bot sẵn sàng.");

        // Auto-select profile đầu tiên để form có dữ liệu sẵn
        if (!profileList.isEmpty()) {
            comboProfiles.setValue(profileList.get(0));
            fillForm(profileList.get(0));
        }
    }

    // ===== PROFILE ACTIONS (gọi từ MainController @FXML handlers) =====

    public void onNewProfile() {
        GameProfile p = new GameProfile();
        p.setProfileName("Profile " + (profileList.size() + 1));
        profileList.add(p);
        refreshProfileCombo();
        comboProfiles.setValue(p);
        fillForm(p);
    }

    public void onSaveProfile() {
        GameProfile p = getOrCreateSelected();
        readFormIntoProfile(p);
        p.applyDefaults(); // fill tọa độ mặc định nếu = 0
        if (!profileList.contains(p)) profileList.add(p);
        saveProfiles();
        refreshProfileCombo();
        comboProfiles.setValue(p);
        fillForm(p); // cập nhật lại form với giá trị đã apply defaults
        appendLog("💾 Đã lưu profile: " + p.getProfileName());
    }

    public void onDeleteProfile() {
        GameProfile sel = comboProfiles.getValue();
        if (sel == null) return;
        profileList.remove(sel);
        saveProfiles();
        refreshProfileCombo();
        clearForm();
        appendLog("🗑 Đã xóa profile: " + sel.getProfileName());
    }

    public void onBrowseExe() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn file launcher game (.exe)");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
            "Executable", "*.exe", "*.bat", "*.cmd"));
        File f = fc.showOpenDialog(null);
        if (f != null) fieldExePath.setText(f.getAbsolutePath());
    }

    // ===== COORDINATE PICK ACTIONS =====

    public void onPickUsername()    { pickCoord(fieldUsernameX, fieldUsernameY, "Username field"); }
    public void onPickPassword()    { pickCoord(fieldPasswordX, fieldPasswordY, "Password field"); }
    public void onPickLoginBtn()    { pickCoord(fieldLoginBtnX, fieldLoginBtnY, "Login button"); }
    public void onPickServer()      { pickCoord(fieldServerX, fieldServerY, "Server"); }
    public void onPickEnterGame()   { pickCoord(fieldEnterGameX, fieldEnterGameY, "Enter Game button"); }
    public void onPickLoginDetect() { pickCoordWithColor(fieldLoginDetectX, fieldLoginDetectY, fieldLoginDetectHex, "Login detect pixel"); }
    public void onPickServerDetect(){ pickCoordWithColor(fieldServerDetectX, fieldServerDetectY, fieldServerDetectHex, "Server detect pixel"); }

    private void pickCoord(TextField xField, TextField yField, String label) {
        appendLog("📍 Hãy di chuột đến [" + label + "] trong 3 giây...");
        new Thread(() -> {
            try {
                for (int i = 3; i >= 1; i--) {
                    int sec = i;
                    Platform.runLater(() -> appendLog("  ⏳ " + sec + "..."));
                    Thread.sleep(1000);
                }
                Point p = MouseInfo.getPointerInfo().getLocation();
                Platform.runLater(() -> {
                    xField.setText(String.valueOf(p.x));
                    yField.setText(String.valueOf(p.y));
                    appendLog("  ✅ [" + label + "]: X=" + p.x + ", Y=" + p.y);
                });
            } catch (InterruptedException ignored) {}
        }, "Pick-Coord-Thread").start();
    }

    private void pickCoordWithColor(TextField xField, TextField yField, TextField hexField, String label) {
        appendLog("📍 Hãy di chuột đến [" + label + "] trong 3 giây...");
        new Thread(() -> {
            try {
                for (int i = 3; i >= 1; i--) {
                    int sec = i;
                    Platform.runLater(() -> appendLog("  ⏳ " + sec + "..."));
                    Thread.sleep(1000);
                }
                Point p = MouseInfo.getPointerInfo().getLocation();
                java.awt.Color c = PixelDetector.getColor(p.x, p.y);
                String hex = String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
                Platform.runLater(() -> {
                    xField.setText(String.valueOf(p.x));
                    yField.setText(String.valueOf(p.y));
                    hexField.setText(hex);
                    appendLog("  ✅ [" + label + "]: X=" + p.x + ", Y=" + p.y + ", Color=" + hex);
                });
            } catch (InterruptedException ignored) {}
        }, "Pick-Color-Thread").start();
    }

    // ===== BOT CONTROL =====

    public void onStartBot() {
        GameProfile profile = getOrCreateSelected();
        readFormIntoProfile(profile);

        if (profile.getUsername().isEmpty()) {
            appendLog("❌ Vui lòng nhập tài khoản trước khi chạy bot.");
            return;
        }

        appendLog("▶ Đang khởi động bot...");
        bot.start(profile,
            msg   -> Platform.runLater(() -> appendLog(msg)),
            state -> Platform.runLater(() -> updateBotUI(state))
        );
    }

    public void onStopBot() {
        bot.stop();
        appendLog("⏹ Đã dừng bot.");
        updateBotUI(GameBot.State.STOPPED);
    }

    public void onPauseBot() {
        bot.togglePause();
    }

    public void onClearLog() {
        textBotLog.clear();
    }

    // ===== HELPERS =====

    private void updateBotUI(GameBot.State state) {
        boolean running = state != GameBot.State.IDLE
                       && state != GameBot.State.STOPPED
                       && state != GameBot.State.ERROR;

        btnStartBot.setDisable(running);
        btnStopBot.setDisable(!running);
        btnPauseBot.setDisable(!running);

        String label;
        switch (state) {
            case IDLE:             label = "⏸ Chờ";           break;
            case LAUNCHING:        label = "🚀 Mở game";      break;
            case WAITING_LOGIN:    label = "⏳ Chờ login";    break;
            case LOGGING_IN:       label = "🔑 Đăng nhập";    break;
            case WAITING_SERVER:   label = "⏳ Chờ server";   break;
            case SELECTING_SERVER: label = "🖥 Chọn server";  break;
            case ENTERING_GAME:    label = "🎮 Vào game";     break;
            case RUNNING:          label = "✅ Đang chạy";    break;
            case ERROR:            label = "❌ Lỗi";          break;
            case STOPPED:          label = "⏹ Dừng";         break;
            default:               label = state.name();
        }
        labelBotState.setText(label);
    }

    private void appendLog(String msg) {
        if (textBotLog == null) return;
        textBotLog.appendText(msg + "\n");
    }

    private GameProfile getOrCreateSelected() {
        GameProfile sel = comboProfiles.getValue();
        return sel != null ? sel : new GameProfile();
    }

    private void readFormIntoProfile(GameProfile p) {
        p.setProfileName(fieldProfileName.getText().trim());
        p.setGameName(fieldGameName.getText().trim());
        p.setExePath(fieldExePath.getText().trim());
        p.setUsername(fieldUsername.getText().trim());

        String rawPw = fieldPassword.getText();
        if (!rawPw.isEmpty()) {
            p.setPasswordEnc(CryptoUtil.encrypt(rawPw));
        }

        // Đọc channel từ ComboBox
        if (comboChannel != null && comboChannel.getValue() != null) {
            p.setServerName(comboChannel.getValue());
        }

        p.setUsernameCoords(intOf(fieldUsernameX),  intOf(fieldUsernameY));
        p.setPasswordCoords(intOf(fieldPasswordX),  intOf(fieldPasswordY));
        p.setLoginBtnCoords(intOf(fieldLoginBtnX),  intOf(fieldLoginBtnY));
        p.setServerCoords(intOf(fieldServerX),      intOf(fieldServerY));
        p.setEnterGameCoords(intOf(fieldEnterGameX),intOf(fieldEnterGameY));

        p.setLoginDetect(intOf(fieldLoginDetectX), intOf(fieldLoginDetectY), fieldLoginDetectHex.getText());
        p.setServerDetect(intOf(fieldServerDetectX), intOf(fieldServerDetectY), fieldServerDetectHex.getText());

        p.setStepDelayMs(spinnerStepDelay.getValue());
        p.setWaitTimeoutMs(spinnerWaitTimeout.getValue());
        p.setLaunchWaitMs(spinnerLaunchWait.getValue());
        p.setAutoReconnect(checkAutoReconnect.isSelected());
        p.setRunAsAdmin(checkRunAsAdmin.isSelected());
    }

    private void fillForm(GameProfile p) {
        fieldProfileName.setText(p.getProfileName());
        fieldGameName.setText(p.getGameName());
        fieldExePath.setText(p.getExePath());
        fieldUsername.setText(p.getUsername());
        fieldPassword.setText(""); // không hiện password đã encrypt

        // Set channel ComboBox
        if (comboChannel != null) {
            String serverName = p.getServerName();
            if (serverName != null && !serverName.isEmpty()) {
                comboChannel.setValue(serverName);
            }
        }

        fieldUsernameX.setText(str(p.getUsernameX()));
        fieldUsernameY.setText(str(p.getUsernameY()));
        fieldPasswordX.setText(str(p.getPasswordX()));
        fieldPasswordY.setText(str(p.getPasswordY()));
        fieldLoginBtnX.setText(str(p.getLoginBtnX()));
        fieldLoginBtnY.setText(str(p.getLoginBtnY()));
        fieldServerX.setText(str(p.getServerX()));
        fieldServerY.setText(str(p.getServerY()));
        fieldEnterGameX.setText(str(p.getEnterGameX()));
        fieldEnterGameY.setText(str(p.getEnterGameY()));
        fieldLoginDetectX.setText(str(p.getLoginDetectX()));
        fieldLoginDetectY.setText(str(p.getLoginDetectY()));
        fieldLoginDetectHex.setText(p.getLoginDetectHex());
        fieldServerDetectX.setText(str(p.getServerDetectX()));
        fieldServerDetectY.setText(str(p.getServerDetectY()));
        fieldServerDetectHex.setText(p.getServerDetectHex());
        spinnerStepDelay.getValueFactory().setValue((int) p.getStepDelayMs());
        spinnerWaitTimeout.getValueFactory().setValue((int) p.getWaitTimeoutMs());
        spinnerLaunchWait.getValueFactory().setValue((int) p.getLaunchWaitMs());
        checkAutoReconnect.setSelected(p.isAutoReconnect());
        checkRunAsAdmin.setSelected(p.isRunAsAdmin());
    }

    private void clearForm() { fillForm(new GameProfile()); }

    private void refreshProfileCombo() {
        GameProfile selected = comboProfiles.getValue();
        comboProfiles.getItems().setAll(profileList);
        if (selected != null && profileList.contains(selected)) {
            comboProfiles.setValue(selected);
        }
    }

    private void loadProfiles() {
        try {
            if (!Files.exists(PROFILES_FILE)) return;
            String json = new String(Files.readAllBytes(PROFILES_FILE));
            Type listType = new com.google.gson.reflect.TypeToken<List<GameProfile>>(){}.getType();
            List<GameProfile> loaded = gson.fromJson(json, listType);
            if (loaded != null) {
                for (GameProfile p : loaded) p.applyDefaults();
                profileList.addAll(loaded);
            }
        } catch (Exception e) {
            System.err.println("[GameBot] Load profiles failed: " + e.getMessage());
        }
    }

    private void saveProfiles() {
        try {
            Files.createDirectories(PROFILES_FILE.getParent());
            Files.write(PROFILES_FILE, gson.toJson(profileList).getBytes());
        } catch (Exception e) {
            System.err.println("[GameBot] Save profiles failed: " + e.getMessage());
        }
    }

    private int intOf(TextField tf) {
        try { return Integer.parseInt(tf.getText().trim()); }
        catch (Exception e) { return 0; }
    }

    private String str(int v) { return String.valueOf(v); }
}
