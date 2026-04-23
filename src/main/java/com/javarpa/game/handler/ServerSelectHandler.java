package com.javarpa.game.handler;

import com.javarpa.core.PixelDetector;
import com.javarpa.core.RobotActions;
import com.javarpa.game.GameProfile;

import java.awt.Color;

/**
 * Handler cho bước 4: Chọn máy chủ + Chọn kênh (2 bước).
 *
 * <p>Luồng xử lý Crossfire:</p>
 * <ol>
 *   <li>Chờ home screen (sảnh chính) load xong</li>
 *   <li>Click nút "Chọn kênh" trên thanh menu</li>
 *   <li>Chờ bảng máy chủ hiển thị</li>
 *   <li>Click máy chủ mong muốn (Tân Binh, Tự do 1, ...)</li>
 *   <li>Chờ danh sách 6 kênh hiển thị</li>
 *   <li>Click kênh mong muốn (kênh 1-6)</li>
 *   <li>Click nút "Vào kênh"</li>
 * </ol>
 */
public class ServerSelectHandler implements ScreenHandler {

    /** Thời gian chờ home screen load sau login (ms). */
    private static final long HOME_SCREEN_WAIT_MS = 5000;

    /** Thời gian chờ bảng server/channel hiện ra (ms). */
    private static final long LIST_WAIT_MS = 1500;

    @Override
    public String getName() {
        return "🖥 Chọn Server/Channel";
    }

    /**
     * Chờ home screen (sảnh chính) xuất hiện sau khi đăng nhập.
     */
    @Override
    public boolean detect(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        ctx.log("⏳ Chờ sảnh chính (home screen) load...");

        if (profile.getServerDetectX() > 0 || profile.getServerDetectY() > 0) {
            Color target = hexToColor(profile.getServerDetectHex());
            boolean found = PixelDetector.waitForColor(
                profile.getServerDetectX(), profile.getServerDetectY(),
                target, 30, profile.getWaitTimeoutMs()
            );
            if (found) {
                ctx.log("✅ Sảnh chính đã xuất hiện.");
            } else {
                ctx.log("⚠ Timeout chờ home screen — thử tiếp...");
            }
        } else {
            ctx.log("  ⏳ Chờ " + HOME_SCREEN_WAIT_MS + "ms để sảnh chính load...");
            ctx.sleep(HOME_SCREEN_WAIT_MS);
            ctx.log("  ✅ Đã chờ xong.");
        }

        return true;
    }

    @Override
    public ScreenResult handle(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        String serverName = profile.getServerName();
        int channelNumber = profile.getChannelNumber();

        ctx.log("🖥 Mục tiêu: máy chủ [" + serverName + "] → kênh " + channelNumber);

        // ── Bước 1: Click nút "Chọn kênh" trên home screen ──
        if (!clickChooseChannelTab(ctx)) {
            return ScreenResult.SKIP;
        }

        // ── Bước 2: Click chọn máy chủ từ bảng ──
        if (!clickServer(ctx, serverName)) {
            return ScreenResult.SKIP;
        }

        // ── Bước 3: Click chọn kênh 1-6 ──
        if (!clickChannel(ctx, channelNumber)) {
            return ScreenResult.SKIP;
        }

        // ── Bước 4: Click nút "Vào kênh" ──
        clickEnterChannel(ctx);

        ctx.log("✅ Đã chọn máy chủ [" + serverName + "] kênh " + channelNumber + ".");
        return ScreenResult.SUCCESS;
    }

    // ================================================================
    //  BƯỚC 1: Click nút "Chọn kênh"
    // ================================================================

    private boolean clickChooseChannelTab(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        int x = profile.getChannelBtnX();
        int y = profile.getChannelBtnY();

        if (x <= 0 || y <= 0) {
            ctx.log("  ⚠ Chưa set tọa độ nút [Chọn kênh] — bỏ qua.");
            return false;
        }

        ctx.log("  → Click nút [Chọn kênh] (" + x + ", " + y + ")");
        RobotActions.click(x, y);

        // Chờ bảng máy chủ hiển thị
        ctx.log("  ⏳ Chờ bảng máy chủ hiển thị...");
        ctx.sleep(LIST_WAIT_MS);
        return true;
    }

    // ================================================================
    //  BƯỚC 2: Click máy chủ trong bảng
    // ================================================================

    private boolean clickServer(BotContext ctx, String serverName) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        int[] coords = profile.getServerCoords(serverName);

        if (coords[0] <= 0 || coords[1] <= 0) {
            ctx.log("  ⚠ Không tìm được tọa độ máy chủ [" + serverName + "].");
            return false;
        }

        ctx.log("  → Click máy chủ [" + serverName + "] (" + coords[0] + ", " + coords[1] + ")");
        RobotActions.click(coords[0], coords[1]);

        // Chờ danh sách kênh hiển thị
        ctx.log("  ⏳ Chờ danh sách kênh hiển thị...");
        ctx.sleep(LIST_WAIT_MS);
        return true;
    }

    // ================================================================
    //  BƯỚC 3: Click kênh 1-6
    // ================================================================

    private boolean clickChannel(BotContext ctx, int channelNumber) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        int[] coords = profile.getChannelRowCoords(channelNumber);

        if (coords[0] <= 0 || coords[1] <= 0) {
            ctx.log("  ⚠ Không tìm được tọa độ kênh " + channelNumber + ".");
            return false;
        }

        ctx.log("  → Click kênh " + channelNumber + " (" + coords[0] + ", " + coords[1] + ")");
        RobotActions.click(coords[0], coords[1]);
        ctx.sleep(profile.getStepDelayMs());
        return true;
    }

    // ================================================================
    //  BƯỚC 4: Click nút "Vào kênh"
    // ================================================================

    private void clickEnterChannel(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        int x = profile.getEnterChannelBtnX();
        int y = profile.getEnterChannelBtnY();

        if (x > 0 && y > 0) {
            ctx.log("  → Click nút [Vào kênh] (" + x + ", " + y + ")");
            RobotActions.click(x, y);
            ctx.sleep(profile.getStepDelayMs());
        } else {
            ctx.log("  ℹ Chưa set tọa độ nút [Vào kênh] — bỏ qua (có thể double-click kênh vào luôn).");
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
