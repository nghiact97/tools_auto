package com.javarpa.game.handler;

import com.javarpa.core.RobotActions;
import com.javarpa.game.GameProfile;
import com.javarpa.util.CryptoUtil;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Context chia sẻ giữa tất cả ScreenHandler.
 * Chứa profile, logger, running flag, và các tiện ích dùng chung.
 *
 * <p>Thay vì mỗi handler phải truy cập trực tiếp GameBot,
 * tất cả đều đi qua BotContext → dễ test và quản lý.</p>
 */
public class BotContext {

    private final GameProfile profile;
    private final Consumer<String> logger;
    private volatile boolean running = true;
    private volatile boolean paused  = false;

    public BotContext(GameProfile profile, Consumer<String> logger) {
        this.profile = profile;
        this.logger  = logger;
    }

    // =============== GETTERS ===============

    public GameProfile getProfile() { return profile; }
    public boolean isRunning()      { return running; }
    public boolean isPaused()       { return paused; }

    // =============== CONTROLS ===============

    public void stop()        { running = false; }
    public void setPaused(boolean p) { paused = p; }

    // =============== LOGGING ===============

    /** Ghi log — callback sẽ đẩy message lên UI thread. */
    public void log(String msg) {
        if (logger != null) logger.accept(msg);
    }

    // =============== SLEEP & PAUSE ===============

    /**
     * Sleep có thể dừng sớm nếu bot bị stop.
     * Tự động xử lý pause trong khi chờ.
     */
    public void sleep(long ms) throws InterruptedException {
        if (ms <= 0) return;
        long end = System.currentTimeMillis() + ms;
        while (System.currentTimeMillis() < end) {
            if (!running) throw new InterruptedException("Bot stopped");
            checkPause();
            Thread.sleep(Math.min(100, Math.max(1, end - System.currentTimeMillis())));
        }
    }

    /** Chờ nếu đang paused. */
    public void checkPause() throws InterruptedException {
        while (paused && running) Thread.sleep(200);
    }

    // =============== TIỆN ÍCH CHUNG ===============

    /**
     * Giải mã password đã encrypt bằng AES.
     * Fallback trả lại plain text nếu decrypt thất bại.
     */
    public String decryptPassword() {
        String enc = profile.getPasswordEnc();
        if (enc == null || enc.isEmpty()) return "";
        try {
            return CryptoUtil.decrypt(enc);
        } catch (Exception e) {
            return enc; // fallback plain text
        }
    }

    /**
     * Dùng clipboard để type text (hỗ trợ Unicode/ký tự đặc biệt).
     * An toàn hơn Robot.keyPress cho password có ký tự đặc biệt.
     */
    public void typeViaSysClipboard(String text) throws InterruptedException {
        java.awt.Toolkit.getDefaultToolkit()
            .getSystemClipboard()
            .setContents(new java.awt.datatransfer.StringSelection(text), null);
        Thread.sleep(100);
        RobotActions.paste(); // CTRL+V
        Thread.sleep(150);
    }

    /**
     * Focus cửa sổ game bằng cách click vào vùng title bar.
     * Sử dụng ALT+TAB fallback nếu cần.
     */
    public void focusGameWindow() throws InterruptedException {
        try {
            // Thử focus bằng PowerShell + Win32 API
            String cmd = "powershell.exe -NoProfile -NonInteractive -Command \""
                + "(Get-Process | Where-Object {$_.MainWindowTitle -match 'CrossFire|Cross Fire'} "
                + "| Select-Object -First 1).MainWindowHandle | ForEach-Object {"
                + "Add-Type -TypeDefinition 'using System; using System.Runtime.InteropServices; "
                + "public class Win32 { [DllImport(\\\"user32.dll\\\")] public static extern bool "
                + "SetForegroundWindow(IntPtr hWnd); }'; "
                + "[Win32]::SetForegroundWindow($_)}\"";

            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", cmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.waitFor(3, TimeUnit.SECONDS);
            Thread.sleep(300); // chờ window focus xong
        } catch (Exception e) {
            log("⚠ Không thể focus cửa sổ game: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra process có đang chạy không (Windows).
     */
    public boolean isProcessRunning(String processName) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "tasklist.exe", "/FI", "IMAGENAME eq " + processName, "/NH"
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            String output = new String(p.getInputStream().readAllBytes()).trim();
            p.waitFor(3, TimeUnit.SECONDS);
            return output.toLowerCase().contains(processName.toLowerCase());
        } catch (Exception e) {
            log("⚠ Không thể kiểm tra process: " + e.getMessage());
            return false;
        }
    }
}
