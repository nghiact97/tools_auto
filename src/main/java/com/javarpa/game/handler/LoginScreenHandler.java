package com.javarpa.game.handler;

import com.javarpa.core.PixelDetector;
import com.javarpa.core.RobotActions;
import com.javarpa.game.GameProfile;

import java.awt.Color;

/**
 * Handler cho bước 3: Phát hiện màn hình Login + Nhập tài khoản + Đăng nhập.
 *
 * <p>Đây là handler quan trọng nhất, gộp 3 nhiệm vụ:</p>
 * <ol>
 *   <li><b>Detect</b>: Chờ login screen xuất hiện (multi-point pixel detect)</li>
 *   <li><b>Enter credentials</b>: Focus window → nhập username → nhập password</li>
 *   <li><b>Verify</b>: Kiểm tra login thành công (login screen biến mất)</li>
 * </ol>
 *
 * <p>Hỗ trợ retry tự động nếu đăng nhập thất bại (sai mật khẩu, lỗi mạng...).</p>
 */
public class LoginScreenHandler implements ScreenHandler {

    @Override
    public String getName() {
        return "🔑 Đăng nhập";
    }

    /**
     * Chờ màn hình đăng nhập xuất hiện.
     * Sử dụng pixel detect (nếu cấu hình) hoặc polling process.
     */
    @Override
    public boolean detect(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        ctx.log("⏳ Chờ màn hình đăng nhập...");

        if (profile.getLoginDetectX() > 0 || profile.getLoginDetectY() > 0) {
            // Chế độ pixel detect: chờ đến khi pixel đúng màu
            return waitForLoginPixel(ctx);
        } else {
            // Không có pixel detect → polling chờ game client xuất hiện
            return waitForGameProcess(ctx);
        }
    }

    /**
     * Thực hiện toàn bộ luồng đăng nhập với retry.
     */
    @Override
    public ScreenResult handle(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        int maxRetry = profile.getLoginRetryCount();

        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            if (!ctx.isRunning()) return ScreenResult.FAIL;

            ctx.log("🔑 Đăng nhập (lần " + attempt + "/" + maxRetry + ")...");

            // Bước 1: Focus cửa sổ game
            focusGame(ctx);

            // Bước 2: Nhập Username
            enterUsername(ctx);

            // Bước 3: Nhập Password
            enterPassword(ctx);

            // Bước 4: Click nút Đăng nhập
            clickLoginButton(ctx);

            // Bước 5: Xác minh đăng nhập thành công
            if (verifyLoginSuccess(ctx)) {
                ctx.log("✅ Đăng nhập thành công!");
                return ScreenResult.SUCCESS;
            }

            // Đăng nhập thất bại
            if (attempt < maxRetry) {
                ctx.log("⚠ Đăng nhập chưa thành công — thử lại sau "
                    + profile.getLoginRetryDelayMs() + "ms...");
                ctx.sleep(profile.getLoginRetryDelayMs());
            }
        }

        ctx.log("❌ Đăng nhập thất bại sau " + maxRetry + " lần thử.");
        return ScreenResult.FAIL;
    }

    // ================================================================
    //  DETECT HELPERS
    // ================================================================

    /**
     * Chờ pixel detect khớp với màu đã cấu hình.
     * Trả về true khi pixel hiển thị đúng màu login screen.
     */
    private boolean waitForLoginPixel(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        Color target = hexToColor(profile.getLoginDetectHex());

        boolean found = PixelDetector.waitForColor(
            profile.getLoginDetectX(), profile.getLoginDetectY(),
            target, 30, profile.getWaitTimeoutMs()
        );

        if (found) {
            ctx.log("✅ Màn hình đăng nhập đã xuất hiện (pixel detect).");
        } else {
            ctx.log("⚠ Timeout chờ login screen — thử tiếp...");
        }
        return true; // vẫn thử đăng nhập dù timeout
    }

    /**
     * Polling kiểm tra game client process đã chạy chưa.
     * Tìm process dựa theo tên exe trong profile + các tên phổ biến của Crossfire.
     */
    private boolean waitForGameProcess(BotContext ctx) throws InterruptedException {
        long timeoutMs = 60000;
        long pollInterval = 2000;
        long elapsed = 0;

        ctx.log("🔍 Đang chờ game client xuất hiện (tối đa 60s)...");

        while (ctx.isRunning() && elapsed < timeoutMs) {
            if (isGameClientRunning(ctx)) {
                ctx.log("✅ Game client đã xuất hiện! Chờ thêm 5s để login screen load...");
                ctx.sleep(5000);
                return true;
            }
            ctx.sleep(pollInterval);
            elapsed += pollInterval;
            if (elapsed % 10000 == 0) {
                ctx.log("  ⏳ Đã chờ " + (elapsed / 1000) + "s...");
            }
        }

        ctx.log("⚠ Timeout 60s — không tìm thấy game client. Thử tiếp...");
        return true; // vẫn thử tiếp
    }

    /**
     * Kiểm tra game client (Crossfire) đã chạy chưa.
     * Tìm process crossfire.exe — đây là game client thực sự
     * (patcher_cf2.exe chỉ là launcher, sẽ thoát sau khi spawn game).
     */
    private boolean isGameClientRunning(BotContext ctx) {
        // Tìm game client thực sự
        return ctx.isProcessRunning("crossfire.exe")
            || ctx.isProcessRunning("cf.exe");
    }

    // ================================================================
    //  LOGIN FLOW
    // ================================================================

    /**
     * Click vào vùng game window để đảm bảo focus đúng cửa sổ.
     */
    private void focusGame(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        // Click vào vùng giữa game window để focus
        int clickX = profile.getUsernameX();
        int clickY = profile.getUsernameY() - 50; // phía trên ô username
        if (clickX > 0 && clickY > 0) {
            RobotActions.click(clickX, clickY);
            ctx.sleep(300);
        }
    }

    /**
     * Click ô Username → Select All → Paste username.
     * Giữ logic đơn giản: click → CTRL+A → paste.
     */
    private void enterUsername(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        int x = profile.getUsernameX();
        int y = profile.getUsernameY();

        if (x <= 0 && y <= 0) return;

        ctx.log("  → Click ô Username (" + x + ", " + y + ")");
        RobotActions.click(x, y);
        ctx.sleep(profile.getStepDelayMs() / 2);
        RobotActions.selectAll();  // CTRL+A
        ctx.sleep(200);
        ctx.typeViaSysClipboard(profile.getUsername());
        ctx.sleep(profile.getStepDelayMs());
    }

    /**
     * Click ô Password → Select All → Paste password (đã decrypt).
     * Giữ logic đơn giản: click → CTRL+A → paste.
     */
    private void enterPassword(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        int x = profile.getPasswordX();
        int y = profile.getPasswordY();

        if (x <= 0 && y <= 0) return;

        String password = ctx.decryptPassword();

        ctx.log("  → Click ô Password (" + x + ", " + y + ")");
        RobotActions.click(x, y);
        ctx.sleep(profile.getStepDelayMs() / 2);
        RobotActions.selectAll();  // CTRL+A
        ctx.sleep(200);
        ctx.typeViaSysClipboard(password);
        ctx.sleep(profile.getStepDelayMs());
    }

    /**
     * Click nút "Đăng Nhập" hoặc nhấn Enter nếu chưa cấu hình tọa độ.
     */
    private void clickLoginButton(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        int x = profile.getLoginBtnX();
        int y = profile.getLoginBtnY();

        if (x > 0 && y > 0) {
            ctx.log("  → Click nút Đăng nhập (" + x + ", " + y + ")");
            RobotActions.click(x, y);
        } else {
            ctx.log("  → Nhấn Enter để đăng nhập");
            RobotActions.enter();
        }
        ctx.sleep(profile.getStepDelayMs());
        ctx.log("  ✅ Đã gửi thông tin đăng nhập.");
    }

    /**
     * Xác minh đăng nhập thành công bằng cách kiểm tra login screen đã biến mất.
     *
     * <p>Logic: Nếu pixel detect vẫn khớp màu login screen sau 10 giây → thất bại.
     * Nếu pixel detect KHÔNG khớp nữa → login screen đã biến mất → thành công.</p>
     *
     * @return true nếu đăng nhập thành công, false nếu login screen vẫn hiển thị
     */
    private boolean verifyLoginSuccess(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();

        // Nếu không cấu hình pixel detect → không verify, coi như thành công
        if (profile.getLoginDetectX() <= 0 && profile.getLoginDetectY() <= 0) {
            ctx.log("  ℹ Không có pixel detect — bỏ qua verify, chờ thêm...");
            ctx.sleep(profile.getStepDelayMs() * 3);
            return true;
        }

        ctx.log("  🔍 Đang xác minh đăng nhập...");
        Color target = hexToColor(profile.getLoginDetectHex());
        long verifyTimeout = 10000; // chờ tối đa 10s
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < verifyTimeout) {
            if (!ctx.isRunning()) return false;

            Color current = PixelDetector.getColor(
                profile.getLoginDetectX(), profile.getLoginDetectY()
            );

            // Pixel KHÔNG còn khớp màu login screen → login screen đã biến mất
            if (!PixelDetector.colorsMatch(current, target, 30)) {
                return true; // thành công!
            }

            ctx.sleep(500);
        }

        return false; // vẫn hiện login screen → thất bại
    }

    // ================================================================
    //  UTILITIES
    // ================================================================

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
}
