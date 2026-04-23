package com.javarpa.game.handler;

import com.javarpa.core.PixelDetector;
import com.javarpa.game.GameProfile;

import java.awt.Color;

/**
 * Handler cho bước 5: Theo dõi trạng thái in-game và phát hiện disconnect.
 *
 * <p>detect(): Luôn trả true — bước này thực hiện khi đã vào game.
 * <br>handle(): Polling pixel detect mỗi 5s. Nếu phát hiện login screen
 * xuất hiện lại → trả RETRY để bot quay lại pipeline reconnect.</p>
 */
public class InGameHandler implements ScreenHandler {

    /** Khoảng thời gian polling kiểm tra disconnect (ms). */
    private static final long POLL_INTERVAL_MS = 5000;

    @Override
    public String getName() {
        return "✅ Trong game";
    }

    @Override
    public boolean detect(BotContext ctx) throws InterruptedException {
        // Luôn thực hiện — đã vào game ở bước trước
        return true;
    }

    /**
     * Theo dõi trạng thái in-game.
     *
     * <p>Nếu autoReconnect bật → polling liên tục kiểm tra disconnect.
     * Nếu không → bot kết thúc sau khi vào game thành công.</p>
     *
     * @return SUCCESS nếu bot kết thúc bình thường,
     *         RETRY nếu phát hiện disconnect (→ reconnect)
     */
    @Override
    public ScreenResult handle(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        ctx.log("✅ Đã vào game thành công!");

        if (!profile.isAutoReconnect()) {
            ctx.log("ℹ Auto Reconnect tắt — bot dừng sau khi vào game.");
            return ScreenResult.SUCCESS;
        }

        // Auto Reconnect bật — polling disconnect
        ctx.log("🔄 Auto Reconnect bật — đang theo dõi kết nối...");
        return monitorDisconnect(ctx);
    }

    // ================================================================
    //  DISCONNECT MONITORING
    // ================================================================

    /**
     * Polling pixel detect mỗi 5 giây.
     * Nếu login screen xuất hiện lại (pixel detect khớp) → disconnect.
     *
     * @return RETRY nếu phát hiện disconnect (để quay lại pipeline)
     */
    private ScreenResult monitorDisconnect(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();

        while (ctx.isRunning() && profile.isAutoReconnect()) {
            ctx.checkPause();
            ctx.sleep(POLL_INTERVAL_MS);

            if (isLoginScreenVisible(ctx)) {
                ctx.log("⚠ Phát hiện disconnect — đang reconnect...");
                return ScreenResult.RETRY;
            }
        }

        return ScreenResult.SUCCESS; // bot bị dừng bởi user
    }

    /**
     * Kiểm tra login screen có đang hiển thị không.
     * Dùng pixel detect đã cấu hình trong profile.
     */
    private boolean isLoginScreenVisible(BotContext ctx) {
        GameProfile profile = ctx.getProfile();

        if (profile.getLoginDetectX() <= 0 && profile.getLoginDetectY() <= 0) {
            return false; // không có pixel detect → không kiểm tra được
        }

        try {
            Color current = PixelDetector.getColor(
                profile.getLoginDetectX(), profile.getLoginDetectY()
            );
            Color expected = hexToColor(profile.getLoginDetectHex());
            return PixelDetector.colorsMatch(current, expected, 30);
        } catch (Exception e) {
            return false;
        }
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
