package com.javarpa.game;

import com.javarpa.core.PixelDetector;
import com.javarpa.core.RobotActions;
import com.javarpa.util.CryptoUtil;

import java.awt.Color;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Bot engine chạy trên thread riêng, tự động hóa toàn bộ luồng:
 * mở game → đăng nhập → chọn server → vào game → (tùy chọn) tự reconnect.
 */
public class GameBot {

    public enum State {
        IDLE, LAUNCHING, WAITING_LOGIN, LOGGING_IN,
        WAITING_SERVER, SELECTING_SERVER, ENTERING_GAME,
        RUNNING, ERROR, STOPPED
    }

    private volatile boolean running   = false;
    private volatile boolean paused    = false;
    private volatile State   state     = State.IDLE;

    private GameProfile profile;
    private Consumer<String> logger;     // callback ghi log lên UI
    private Consumer<State>  onState;    // callback cập nhật trạng thái lên UI

    private Thread botThread;

    // ================================================================
    //  PUBLIC API
    // ================================================================

    /** Khởi động bot với profile và callbacks. */
    public void start(GameProfile profile, Consumer<String> logger, Consumer<State> onState) {
        if (running) return;
        this.profile = profile;
        this.logger  = logger;
        this.onState = onState;
        running = true;
        paused  = false;

        botThread = new Thread(this::run, "GameBot-Thread");
        botThread.setDaemon(true);
        botThread.start();
    }

    /** Dừng bot. */
    public void stop() {
        running = false;
        if (botThread != null) botThread.interrupt();
        setState(State.STOPPED);
    }

    /** Tạm dừng / tiếp tục. */
    public void togglePause() {
        paused = !paused;
        log(paused ? "⏸ Tạm dừng bot." : "▶ Tiếp tục bot.");
    }

    public State getState() { return state; }
    public boolean isRunning() { return running; }

    // ================================================================
    //  MAIN LOOP
    // ================================================================

    private void run() {
        try {
            log("🤖 Bot bắt đầu — profile: " + profile.getProfileName());

            do {
                if (!running) break;

                // BƯỚC 1: Mở launcher game
                stepLaunch();
                if (!running) break;

                // BƯỚC 2: Chờ màn hình login
                stepWaitLogin();
                if (!running) break;

                // BƯỚC 3: Điền tài khoản + đăng nhập
                stepLogin();
                if (!running) break;

                // BƯỚC 4: Chờ màn hình chọn server
                stepWaitServer();
                if (!running) break;

                // BƯỚC 5: Chọn server
                stepSelectServer();
                if (!running) break;

                // BƯỚC 6: Vào game
                stepEnterGame();
                if (!running) break;

                // BƯỚC 7: Giữ trạng thái RUNNING, polling disconnect
                setState(State.RUNNING);
                log("✅ Đã vào game thành công!");

                if (profile.isAutoReconnect()) {
                    log("🔄 Auto Reconnect bật — đang theo dõi kết nối...");
                    monitorDisconnect();
                } else {
                    // Bot kết thúc sau khi vào game
                    running = false;
                }

            } while (running && profile.isAutoReconnect());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("⏹ Bot bị dừng.");
        } catch (Exception e) {
            setState(State.ERROR);
            log("❌ Lỗi bot: " + e.getMessage());
        } finally {
            running = false;
            if (state != State.ERROR) setState(State.STOPPED);
        }
    }

    // ================================================================
    //  STEPS
    // ================================================================

    /** Bước 1: Mở game exe (có thể chạy với quyền Admin). */
    private void stepLaunch() throws IOException, InterruptedException {
        setState(State.LAUNCHING);
        String exePath = profile.getExePath();

        if (exePath == null || exePath.trim().isEmpty()) {
            log("⚠ Không có đường dẫn exe — bỏ qua bước mở game.");
            return;
        }

        // Kiểm tra file tồn tại trước khi launch
        java.io.File exeFile = new java.io.File(exePath);
        if (!exeFile.exists()) {
            log("❌ File không tồn tại: " + exePath);
            throw new IOException("Exe file not found: " + exePath);
        }

        try {
            if (profile.isRunAsAdmin()) {
                log("🚀 [Admin] Đang mở: " + exePath);
                // PowerShell Start-Process cho phép elevate — cần tool chạy as Admin trước
                String safeExePath = exePath.replace("'", "''");
                ProcessBuilder pb = new ProcessBuilder(
                    "powershell.exe", "-NoProfile", "-NonInteractive", "-Command",
                    "Start-Process -FilePath '" + safeExePath + "' -Verb RunAs -ErrorAction Stop"
                );
                pb.redirectErrorStream(true);
                Process ps = pb.start();

                // Log bất kỳ error nào từ PowerShell
                String psOut = new String(ps.getInputStream().readAllBytes()).trim();
                boolean exited = ps.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
                int code = exited ? ps.exitValue() : -1;

                if (!psOut.isEmpty()) {
                    log("  [PS] " + psOut);
                }
                if (code != 0) {
                    log("⚠ PowerShell exit " + code + " — game có thể không mở được. Thử chạy Tool với quyền Admin!");
                } else {
                    log("✅ Đã gửi lệnh mở exe (Admin).");
                }

            } else {
                log("🚀 Đang mở: " + exePath);
                // Direct ProcessBuilder — xử lý Unicode + spaces tốt nhất, không qua cmd/start
                ProcessBuilder pb = new ProcessBuilder(exePath);
                pb.directory(exeFile.getParentFile()); // set working dir = thư mục chứa exe
                pb.start();
                log("✅ Đã gửi lệnh mở exe.");
            }

        } catch (IOException e) {
            log("❌ Lỗi mở exe: " + e.getMessage());
            log("💡 Hint: Nếu game cần quyền Admin → tick 'Run as Admin' và chạy Tool bằng run.bat");
            throw e;
        }

        long waitMs = profile.getLaunchWaitMs();
        log("⏳ Chờ " + waitMs + "ms để game khởi động...");
        sleep(waitMs);
    }


    /** Bước 2: Chờ màn hình đăng nhập load. */
    private void stepWaitLogin() throws InterruptedException {
        setState(State.WAITING_LOGIN);
        log("⏳ Chờ màn hình đăng nhập...");

        if (profile.getLoginDetectX() > 0 || profile.getLoginDetectY() > 0) {
            Color target = hexToColor(profile.getLoginDetectHex());
            boolean found = PixelDetector.waitForColor(
                profile.getLoginDetectX(), profile.getLoginDetectY(),
                target, 30, profile.getWaitTimeoutMs()
            );
            if (!found) {
                log("⚠ Timeout chờ login screen — thử tiếp...");
            } else {
                log("✅ Màn hình đăng nhập đã xuất hiện.");
            }
        } else {
            log("ℹ Không có pixel detect login — chờ " + (profile.getStepDelayMs() * 3) + "ms...");
            sleep(profile.getStepDelayMs() * 3);
        }
    }

    /** Bước 3: Click username, điền thông tin, đăng nhập. */
    private void stepLogin() throws InterruptedException {
        setState(State.LOGGING_IN);
        log("🔑 Đang đăng nhập...");

        String password = decryptPassword(profile.getPasswordEnc());

        // Click vào ô Username
        if (profile.getUsernameX() > 0) {
            log("  → Click ô Username (" + profile.getUsernameX() + ", " + profile.getUsernameY() + ")");
            RobotActions.click(profile.getUsernameX(), profile.getUsernameY());
            sleep(profile.getStepDelayMs() / 2);
            RobotActions.selectAll();  // CTRL+A
            sleep(200);
            typeViaSysClipboard(profile.getUsername());
            sleep(profile.getStepDelayMs());
        }

        // Click vào ô Password
        if (profile.getPasswordX() > 0) {
            log("  → Click ô Password (" + profile.getPasswordX() + ", " + profile.getPasswordY() + ")");
            RobotActions.click(profile.getPasswordX(), profile.getPasswordY());
            sleep(profile.getStepDelayMs() / 2);
            RobotActions.selectAll();
            sleep(200);
            typeViaSysClipboard(password);
            sleep(profile.getStepDelayMs());
        }

        // Click nút Đăng nhập
        if (profile.getLoginBtnX() > 0) {
            log("  → Click nút Đăng nhập (" + profile.getLoginBtnX() + ", " + profile.getLoginBtnY() + ")");
            RobotActions.click(profile.getLoginBtnX(), profile.getLoginBtnY());
        } else {
            log("  → Nhấn Enter để đăng nhập");
            RobotActions.enter();
        }
        sleep(profile.getStepDelayMs());
        log("✅ Đã gửi thông tin đăng nhập.");
    }

    /** Bước 4: Chờ màn hình chọn server. */
    private void stepWaitServer() throws InterruptedException {
        setState(State.WAITING_SERVER);
        log("⏳ Chờ màn hình chọn server...");

        if (profile.getServerDetectX() > 0 || profile.getServerDetectY() > 0) {
            Color target = hexToColor(profile.getServerDetectHex());
            boolean found = PixelDetector.waitForColor(
                profile.getServerDetectX(), profile.getServerDetectY(),
                target, 30, profile.getWaitTimeoutMs()
            );
            if (!found) {
                log("⚠ Timeout chờ server screen — thử tiếp...");
            } else {
                log("✅ Màn hình chọn server đã xuất hiện.");
            }
        } else {
            sleep(profile.getStepDelayMs() * 3);
        }
    }

    /** Bước 5: Click vào server muốn chọn. */
    private void stepSelectServer() throws InterruptedException {
        setState(State.SELECTING_SERVER);
        log("🖥 Chọn server: " + profile.getServerName());

        if (profile.getServerX() > 0) {
            log("  → Click server (" + profile.getServerX() + ", " + profile.getServerY() + ")");
            RobotActions.click(profile.getServerX(), profile.getServerY());
            sleep(profile.getStepDelayMs());
        } else {
            log("  ⚠ Chưa set tọa độ server — bỏ qua.");
        }
    }

    /** Bước 6: Click nút Vào Game. */
    private void stepEnterGame() throws InterruptedException {
        setState(State.ENTERING_GAME);
        log("🎮 Click nút Vào Game...");

        if (profile.getEnterGameX() > 0) {
            sleep(profile.getStepDelayMs());
            RobotActions.click(profile.getEnterGameX(), profile.getEnterGameY());
            sleep(profile.getStepDelayMs() * 2);
        } else {
            log("  ⚠ Chưa set tọa độ nút Vào Game — nhấn Enter...");
            RobotActions.enter();
            sleep(profile.getStepDelayMs() * 2);
        }
    }

    /** Theo dõi disconnect và tự reconnect. */
    private void monitorDisconnect() throws InterruptedException {
        // Polling pixel detect mỗi 5s — nếu login screen xuất hiện lại → reconnect
        while (running && profile.isAutoReconnect()) {
            checkPause();
            sleep(5000);
            if (profile.getLoginDetectX() > 0) {
                Color current = PixelDetector.getColor(
                    profile.getLoginDetectX(), profile.getLoginDetectY()
                );
                Color expected = hexToColor(profile.getLoginDetectHex());
                if (PixelDetector.colorsMatch(current, expected, 30)) {
                    log("⚠ Phát hiện disconnect — đang reconnect...");
                    return; // quay lại vòng lặp chính
                }
            }
        }
    }

    // ================================================================
    //  UTILITIES
    // ================================================================

    /**
     * Dùng clipboard để type text (hỗ trợ Unicode / tiếng Việt, ký tự đặc biệt).
     * An toàn hơn Robot.keyPress cho password có ký tự đặc biệt.
     */
    private void typeViaSysClipboard(String text) throws InterruptedException {
        java.awt.Toolkit.getDefaultToolkit()
            .getSystemClipboard()
            .setContents(new java.awt.datatransfer.StringSelection(text), null);
        sleep(100);
        RobotActions.paste(); // CTRL+V
        sleep(150);
    }

    private void sleep(long ms) throws InterruptedException {
        if (ms <= 0) return;
        long end = System.currentTimeMillis() + ms;
        while (System.currentTimeMillis() < end) {
            if (!running) throw new InterruptedException("Bot stopped");
            checkPause();
            Thread.sleep(Math.min(100, end - System.currentTimeMillis()));
        }
    }

    private void checkPause() throws InterruptedException {
        while (paused && running) Thread.sleep(200);
    }

    private void setState(State s) {
        state = s;
        if (onState != null) onState.accept(s);
    }

    private void log(String msg) {
        if (logger != null) logger.accept(msg);
    }

    private Color hexToColor(String hex) {
        try {
            hex = hex.replace("#", "");
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return new Color(r, g, b);
        } catch (Exception e) {
            return Color.WHITE;
        }
    }

    private String decryptPassword(String enc) {
        if (enc == null || enc.isEmpty()) return "";
        try {
            return CryptoUtil.decrypt(enc);
        } catch (Exception e) {
            return enc; // fallback plain text
        }
    }
}
