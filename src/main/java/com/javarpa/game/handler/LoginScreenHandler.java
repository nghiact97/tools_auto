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
 *   <li><b>Detect</b>: Chờ login screen xuất hiện (auto-detect hoặc pixel detect)</li>
 *   <li><b>Enter credentials</b>: Focus window → nhập username → nhập password</li>
 *   <li><b>Verify</b>: Kiểm tra login thành công (login screen biến mất)</li>
 * </ol>
 *
 * <h3>Detection tự động (CrossFire):</h3>
 * <p>Dựa trên phân tích UI game CrossFire, nút "Đăng Nhập" có màu ĐỎ đặc trưng.
 * Khi login screen chưa hiện, vị trí đó hiển thị splash art (không đỏ).
 * Bot kiểm tra nhiều điểm cùng lúc để xác nhận chính xác login screen đã hiện.</p>
 *
 * <p>Hỗ trợ retry tự động nếu đăng nhập thất bại.</p>
 */
public class LoginScreenHandler implements ScreenHandler {

    // ================================================================
    //  MÀU ĐẶC TRƯNG CỦA LOGIN SCREEN (phân tích từ ảnh game)
    // ================================================================

    /**
     * Nút "Đăng Nhập" trong CrossFire có nền ĐỎ đặc trưng.
     * Khi login screen chưa hiện, vị trí này là splash art (hoàn toàn khác).
     *
     * Điều kiện: R > 100, G < 90, B < 90 (bao phủ dark red → bright red)
     */
    private static final int LOGIN_BTN_MIN_RED   = 100;
    private static final int LOGIN_BTN_MAX_GREEN = 90;
    private static final int LOGIN_BTN_MAX_BLUE  = 90;

    /**
     * Vùng form login (ô input, nền form) có màu TỐI đặc trưng.
     * Brightness < 80 (trên thang 0-255)
     */
    private static final int FORM_MAX_BRIGHTNESS = 80;

    @Override
    public String getName() {
        return "🔑 Đăng nhập";
    }

    // ================================================================
    //  DETECT — chờ login screen xuất hiện
    // ================================================================

    @Override
    public boolean detect(BotContext ctx) throws InterruptedException {
        ctx.log("⏳ Chờ game khởi động...");
        waitForGameProcess(ctx);
        return waitForLoginScreenVisible(ctx);
    }

    /**
     * Chờ login screen hiển thị.
     * Ưu tiên: Auto-detect (nút đỏ) → Pixel detect (cấu hình) → Chờ cố định
     */
    private boolean waitForLoginScreenVisible(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();

        // === ƯU TIÊN 1: Auto-detect nút "Đăng Nhập" đỏ (KHÔNG CẦN CẤU HÌNH) ===
        if (profile.getLoginBtnX() > 0 && profile.getLoginBtnY() > 0) {
            return waitForLoginAutoDetect(ctx);
        }

        // === ƯU TIÊN 2: Pixel detect (người dùng cấu hình thủ công) ===
        if (profile.getLoginDetectX() > 0 && profile.getLoginDetectY() > 0) {
            return waitForLoginPixel(ctx);
        }

        // === FALLBACK: Chờ cố định ===
        long wait = profile.getLaunchWaitMs();
        ctx.log("⚠ Chưa có tọa độ nút Đăng nhập — chờ " + wait + "ms...");
        ctx.sleep(wait);
        return true;
    }

    // ================================================================
    //  AUTO-DETECT: Dựa trên phân tích UI CrossFire
    // ================================================================

    /**
     * Tự động phát hiện login screen bằng cách kiểm tra nhiều điểm đặc trưng:
     *
     * <ol>
     *   <li><b>Nút "Đăng Nhập"</b> tại (loginBtnX, loginBtnY) — phải có màu ĐỎ</li>
     *   <li><b>Vùng form</b> tại (usernameX, usernameY) — phải TỐI (nền input)</li>
     * </ol>
     *
     * <p>Khi login screen chưa hiện, cả 2 vị trí đều hiện splash art → màu KHÁC.
     * Khi login screen đã hiện, nút đỏ + form tối → xác nhận chính xác.</p>
     */
    private boolean waitForLoginAutoDetect(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        long timeout = 120_000L; // 2 phút — đủ cho game load chậm
        long poll = 1000;
        long elapsed = 0;

        int btnX = profile.getLoginBtnX();
        int btnY = profile.getLoginBtnY();
        int userX = profile.getUsernameX();
        int userY = profile.getUsernameY();

        ctx.log("🔍 Auto-detect login screen (CrossFire)...");
        ctx.log("   Kiểm tra: Nút Đăng Nhập (" + btnX + "," + btnY + ") = ĐỎ?");
        if (userX > 0 && userY > 0) {
            ctx.log("   Kiểm tra: Ô Username (" + userX + "," + userY + ") = TỐI?");
        }

        while (ctx.isRunning() && elapsed < timeout) {
            boolean loginBtnRed = isLoginButtonRed(btnX, btnY);
            boolean formDark = (userX <= 0 || userY <= 0) || isFormAreaDark(userX, userY);

            if (loginBtnRed && formDark) {
                ctx.log("✅ Login screen đã hiển thị! (chờ " + (elapsed / 1000) + "s)");
                ctx.log("   Nút Đăng Nhập: ĐỎ ✓ | Form: TỐI ✓");
                ctx.sleep(500); // buffer nhỏ để UI ổn định
                return true;
            }

            ctx.sleep(poll);
            elapsed += poll;

            // Log debug mỗi 5s
            if (elapsed % 5000 == 0) {
                Color btnColor = PixelDetector.getColor(btnX, btnY);
                String btnHex = colorToHex(btnColor);
                String status = loginBtnRed ? "ĐỎ ✓" : "chưa (" + btnHex + ")";
                ctx.log("  ⏳ " + (elapsed / 1000) + "s | Nút: " + status
                    + " | Form: " + (formDark ? "TỐI ✓" : "chưa"));
            }
        }

        ctx.log("❌ Timeout " + (timeout / 1000) + "s — login screen chưa xuất hiện.");
        ctx.log("   Nút Đăng Nhập tại (" + btnX + "," + btnY + ") không có màu đỏ.");
        return false;
    }

    /**
     * Kiểm tra pixel tại vị trí nút "Đăng Nhập" có phải màu ĐỎ không.
     * Nút CrossFire có background gradient đỏ đậm → đỏ sáng.
     */
    private boolean isLoginButtonRed(int x, int y) {
        Color c = PixelDetector.getColor(x, y);
        return c.getRed() > LOGIN_BTN_MIN_RED
            && c.getGreen() < LOGIN_BTN_MAX_GREEN
            && c.getBlue() < LOGIN_BTN_MAX_BLUE;
    }

    /**
     * Kiểm tra vùng form login có tối không (nền input field / form background).
     * Trước khi load, vị trí này hiện splash art (sáng hơn).
     */
    private boolean isFormAreaDark(int x, int y) {
        Color c = PixelDetector.getColor(x, y);
        int brightness = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
        return brightness < FORM_MAX_BRIGHTNESS;
    }

    // ================================================================
    //  HANDLE — thực hiện đăng nhập
    // ================================================================

    @Override
    public ScreenResult handle(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        int maxRetry = profile.getLoginRetryCount();

        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            if (!ctx.isRunning()) return ScreenResult.FAIL;

            ctx.log("🔑 Đăng nhập (lần " + attempt + "/" + maxRetry + ")...");

            // Xác nhận login screen vẫn hiển thị trước khi nhập
            if (!confirmLoginScreenReady(ctx, attempt)) {
                if (attempt < maxRetry) {
                    ctx.log("⚠ Login screen chưa xuất hiện — thử lại sau "
                        + profile.getLoginRetryDelayMs() + "ms...");
                    ctx.sleep(profile.getLoginRetryDelayMs());
                    continue;
                }
                ctx.log("❌ Không phát hiện login screen sau " + maxRetry + " lần.");
                return ScreenResult.FAIL;
            }

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
    //  XÁC NHẬN LOGIN SCREEN SẴN SÀNG (trước mỗi lần nhập)
    // ================================================================

    /**
     * Kiểm tra nhanh login screen vẫn đang hiển thị.
     * Dùng auto-detect (nhanh, check 2 pixel) hoặc pixel detect.
     */
    private boolean confirmLoginScreenReady(BotContext ctx, int attempt)
            throws InterruptedException {
        GameProfile profile = ctx.getProfile();

        // Auto-detect: check nút đỏ + form tối
        if (profile.getLoginBtnX() > 0 && profile.getLoginBtnY() > 0) {
            boolean btnRed = isLoginButtonRed(profile.getLoginBtnX(), profile.getLoginBtnY());
            boolean formDark = (profile.getUsernameX() <= 0 || profile.getUsernameY() <= 0)
                || isFormAreaDark(profile.getUsernameX(), profile.getUsernameY());

            if (btnRed && formDark) {
                ctx.log("  ✅ Login screen xác nhận: Nút ĐỎ + Form TỐI");
                return true;
            }

            // Chưa thấy → poll thêm tối đa 30s
            ctx.log("  🔍 Login screen chưa sẵn sàng — chờ thêm...");
            long pollTimeout = 30_000L;
            long elapsed = 0;
            while (ctx.isRunning() && elapsed < pollTimeout) {
                if (isLoginButtonRed(profile.getLoginBtnX(), profile.getLoginBtnY())) {
                    ctx.log("  ✅ Login screen đã sẵn sàng! (" + (elapsed / 1000) + "s)");
                    ctx.sleep(300);
                    return true;
                }
                ctx.sleep(1000);
                elapsed += 1000;
            }
            return false;
        }

        // Pixel detect fallback
        if (profile.getLoginDetectX() > 0 && profile.getLoginDetectY() > 0) {
            Color target = hexToColor(profile.getLoginDetectHex());
            Color now = PixelDetector.getColor(
                profile.getLoginDetectX(), profile.getLoginDetectY());
            if (PixelDetector.colorsMatch(now, target, 30)) {
                return true;
            }
            // Poll thêm
            long elapsed = 0;
            while (ctx.isRunning() && elapsed < 60_000L) {
                now = PixelDetector.getColor(
                    profile.getLoginDetectX(), profile.getLoginDetectY());
                if (PixelDetector.colorsMatch(now, target, 30)) {
                    ctx.log("  ✅ Login screen detected (pixel) — " + (elapsed / 1000) + "s");
                    ctx.sleep(300);
                    return true;
                }
                ctx.sleep(1500);
                elapsed += 1500;
            }
            return false;
        }

        // Không có gì → chờ cố định lần đầu
        if (attempt == 1) {
            ctx.sleep(profile.getLaunchWaitMs());
        }
        return true;
    }

    // ================================================================
    //  PIXEL DETECT (fallback)
    // ================================================================

    private boolean waitForLoginPixel(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        Color target = hexToColor(profile.getLoginDetectHex());

        Color current = PixelDetector.getColor(
            profile.getLoginDetectX(), profile.getLoginDetectY());
        String currentHex = colorToHex(current);
        ctx.log("🔍 Pixel detect tại (" + profile.getLoginDetectX()
            + "," + profile.getLoginDetectY() + ")");
        ctx.log("   Mong đợi: " + profile.getLoginDetectHex()
            + " | Hiện tại: " + currentHex);

        boolean found = PixelDetector.waitForColor(
            profile.getLoginDetectX(), profile.getLoginDetectY(),
            target, 30, profile.getWaitTimeoutMs()
        );

        if (found) {
            ctx.log("✅ Login screen đã xuất hiện (pixel detect).");
            ctx.sleep(500);
            return true;
        } else {
            ctx.log("⚠ Timeout — login screen chưa hiển thị.");
            return false;
        }
    }

    // ================================================================
    //  GAME PROCESS DETECTION
    // ================================================================

    private boolean waitForGameProcess(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        long timeoutMs    = profile.getWaitTimeoutMs();
        long pollInterval = 2000;
        long elapsed      = 0;

        ctx.log("\uD83D\uDD0D Đang chờ game client xuất hiện (tối đa " + (timeoutMs / 1000) + "s)...");

        while (ctx.isRunning() && elapsed < timeoutMs) {
            if (isGameClientRunning(ctx)) {
                ctx.log("  \u2705 Game client đã xuất hiện! Chờ thêm 5s để login screen load...");
                ctx.sleep(5000);
                return true;
            }
            ctx.sleep(pollInterval);
            elapsed += pollInterval;
            if (elapsed % 10000 == 0) {
                ctx.log("  \u23F3 Đã chờ " + (elapsed / 1000) + "s...");
            }
        }

        long extra = profile.getLaunchWaitMs();
        ctx.log("  \u26A0 Không phát hiện tiến trình game — chờ thêm " + extra + "ms...");
        ctx.sleep(extra);
        return true;
    }

    private boolean isGameClientRunning(BotContext ctx) {
        return ctx.isProcessRunning("crossfire.exe")
            || ctx.isProcessRunning("cf.exe")
            || ctx.isProcessRunning("CrossFire.exe")
            || ctx.isProcessRunning("CFGame.exe")
            || ctx.isProcessRunning("cg.exe");
    }

    // ================================================================
    //  LOGIN FLOW
    // ================================================================

    private void focusGame(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        int clickX = profile.getUsernameX();
        int clickY = profile.getUsernameY() - 50;
        if (clickX > 0 && clickY > 0) {
            RobotActions.click(clickX, clickY);
            ctx.sleep(300);
        }
    }

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

    // ================================================================
    //  VERIFY LOGIN SUCCESS
    // ================================================================

    /**
     * Xác minh đăng nhập thành công: login screen phải BIẾN MẤT.
     *
     * <p>Auto-detect: Nút "Đăng Nhập" đỏ sẽ BIẾN MẤT khi chuyển sang server screen.
     * Pixel detect: Pixel login sẽ không còn khớp màu.</p>
     */
    private boolean verifyLoginSuccess(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();

        // Auto-detect verify: nút đỏ phải biến mất
        if (profile.getLoginBtnX() > 0 && profile.getLoginBtnY() > 0) {
            ctx.log("  🔍 Xác minh: chờ nút Đăng Nhập biến mất...");
            long verifyTimeout = 15_000;
            long start = System.currentTimeMillis();

            while (System.currentTimeMillis() - start < verifyTimeout) {
                if (!ctx.isRunning()) return false;

                // Nút KHÔNG CÒN ĐỎ → login screen đã biến mất → thành công!
                if (!isLoginButtonRed(profile.getLoginBtnX(), profile.getLoginBtnY())) {
                    ctx.log("  ✅ Nút Đăng Nhập đã biến mất → đăng nhập OK!");
                    return true;
                }
                ctx.sleep(500);
            }
            ctx.log("  ❌ Nút Đăng Nhập vẫn còn → đăng nhập thất bại.");
            return false;
        }

        // Pixel detect verify
        if (profile.getLoginDetectX() > 0 && profile.getLoginDetectY() > 0) {
            Color target = hexToColor(profile.getLoginDetectHex());
            ctx.log("  🔍 Xác minh đăng nhập (pixel detect)...");
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 10_000) {
                if (!ctx.isRunning()) return false;
                Color current = PixelDetector.getColor(
                    profile.getLoginDetectX(), profile.getLoginDetectY());
                if (!PixelDetector.colorsMatch(current, target, 30)) {
                    return true;
                }
                ctx.sleep(500);
            }
            return false;
        }

        // Không detect → chờ rồi coi như OK
        ctx.log("  ℹ Không có detect — chờ thêm...");
        ctx.sleep(profile.getStepDelayMs() * 3);
        return true;
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

    private String colorToHex(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }
}
