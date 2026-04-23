package com.javarpa.game.handler;

import com.javarpa.core.PixelDetector;
import com.javarpa.core.RobotActions;
import com.javarpa.game.GameProfile;

import java.awt.Color;

/**
 * Handler cho bước 4: Sau khi đăng nhập thành công, thực hiện toàn bộ luồng vào game.
 *
 * <p>Luồng xử lý Crossfire (cập nhật 24/04/2026):</p>
 * <ol>
 *   <li>Chờ home screen load</li>
 *   <li>Close Thông Báo (đóng popup)</li>
 *   <li>Click nút "Chọn kênh" trên thanh menu</li>
 *   <li>Chọn máy chủ yêu cầu (Tan Binh, Tu do 1, ...)</li>
 *   <li>Xác nhận hệ thống (popup confirm khi đổi server)</li>
 *   <li>Chọn kênh yêu cầu (kênh 1-6)</li>
 *   <li>Tạo phòng</li>
 *   <li>Chế độ AI</li>
 *   <li>Xác nhận tạo phòng</li>
 *   <li>Bắt đầu chơi game</li>
 * </ol>
 */
public class ServerSelectHandler implements ScreenHandler {

    /** Thời gian chờ home screen load sau login (ms). */
    private static final long HOME_SCREEN_WAIT_MS = 5000;

    /** Thời gian chờ bảng server/channel hiện ra (ms). */
    private static final long LIST_WAIT_MS = 1500;

    /** Thời gian chờ popup thông báo hiện ra (ms). */
    private static final long NOTIFY_WAIT_MS = 3000;

    /** Thời gian chờ sau khi xác nhận hệ thống (ms). */
    private static final long SYS_CONFIRM_WAIT_MS = 2000;

    /** Thời gian chờ phòng được tạo (ms). */
    private static final long ROOM_WAIT_MS = 2000;

    @Override
    public String getName() {
        return "🖥 Chọn Server/Channel + Tạo Phòng";
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

        // ── Bước 1: Close Thông Báo ──
        closeNotification(ctx);

        // ── Bước 2: Click nút "Chọn kênh" trên home screen ──
        if (!clickChooseChannelTab(ctx)) {
            return ScreenResult.SKIP;
        }

        // ── Bước 3: Chọn máy chủ yêu cầu ──
        if (!clickServer(ctx, serverName)) {
            return ScreenResult.SKIP;
        }

        // ── Bước 4: Xác nhận hệ thống (popup khi đổi server) ──
        clickSysConfirm(ctx);

        // ── Bước 5: Chọn kênh yêu cầu (kênh 1-6) ──
        if (!clickChannel(ctx, channelNumber)) {
            return ScreenResult.SKIP;
        }

        // ── Bước 6: Tạo phòng ──
        clickCreateRoom(ctx);

        // ── Bước 7: Chế độ AI ──
        clickAiMode(ctx);

        // ── Bước 8: Xác nhận tạo phòng ──
        clickConfirmRoom(ctx);

        // ── Bước 9: Bắt đầu chơi game ──
        clickStartGame(ctx);

        ctx.log("✅ Đã hoàn thành: [" + serverName + "] kênh " + channelNumber + " → Tạo phòng AI → Bắt đầu chơi!");
        return ScreenResult.SUCCESS;
    }

    // ================================================================
    //  BƯỚC 1: Close Thông Báo
    // ================================================================

    private void closeNotification(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        int x = profile.getCloseNotifyX();
        int y = profile.getCloseNotifyY();

        if (x <= 0 || y <= 0) {
            ctx.log("  ℹ Chưa set tọa độ [Close Thông Báo] — bỏ qua.");
            return;
        }

        ctx.log("  ⏳ Chờ popup thông báo (" + NOTIFY_WAIT_MS + "ms)...");
        ctx.sleep(NOTIFY_WAIT_MS);
        ctx.log("  → Click [Close Thông Báo] (" + x + ", " + y + ")");
        doubleClick(x, y);
        ctx.sleep(profile.getStepDelayMs());
    }

    // ================================================================
    //  BƯỚC 2: Click nút "Chọn kênh"
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
        doubleClick(x, y);

        // Chờ bảng máy chủ hiển thị
        ctx.log("  ⏳ Chờ bảng máy chủ hiển thị...");
        ctx.sleep(LIST_WAIT_MS);
        return true;
    }

    // ================================================================
    //  BƯỚC 3: Click máy chủ
    // ================================================================

    private boolean clickServer(BotContext ctx, String serverName) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        int[] coords = profile.getServerCoords(serverName);

        if (coords[0] <= 0 || coords[1] <= 0) {
            ctx.log("  ⚠ Không tìm được tọa độ máy chủ [" + serverName + "].");
            return false;
        }

        ctx.log("  → Double-click máy chủ [" + serverName + "] (" + coords[0] + ", " + coords[1] + ")");
        doubleClick(coords[0], coords[1]);

        // Chờ chuyển server
        ctx.log("  ⏳ Chờ chuyển máy chủ...");
        ctx.sleep(LIST_WAIT_MS);
        return true;
    }

    // ================================================================
    //  BƯỚC 4: Xác nhận hệ thống
    // ================================================================

    private void clickSysConfirm(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        int x = profile.getSysConfirmX();
        int y = profile.getSysConfirmY();

        if (x <= 0 || y <= 0) {
            ctx.log("  ℹ Chưa set tọa độ [Xác nhận hệ thống] — bỏ qua.");
            return;
        }

        ctx.log("  ⏳ Chờ popup xác nhận hệ thống (" + SYS_CONFIRM_WAIT_MS + "ms)...");
        ctx.sleep(SYS_CONFIRM_WAIT_MS);
        ctx.log("  → Click [Xác nhận hệ thống] (" + x + ", " + y + ")");
        doubleClick(x, y);
        ctx.sleep(profile.getStepDelayMs());
    }

    // ================================================================
    //  BƯỚC 5: Click kênh 1-6
    // ================================================================

    private boolean clickChannel(BotContext ctx, int channelNumber) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        int[] coords = profile.getChannelRowCoords(channelNumber);

        if (coords[0] <= 0 || coords[1] <= 0) {
            ctx.log("  ⚠ Không tìm được tọa độ kênh " + channelNumber + ".");
            return false;
        }

        ctx.log("  → Click kênh " + channelNumber + " (" + coords[0] + ", " + coords[1] + ")");
        doubleClick(coords[0], coords[1]);
        ctx.sleep(profile.getStepDelayMs());

        // Click nút "Vào kênh" nếu có
        clickEnterChannel(ctx);
        return true;
    }

    private void clickEnterChannel(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        int x = profile.getEnterChannelBtnX();
        int y = profile.getEnterChannelBtnY();

        if (x > 0 && y > 0) {
            ctx.log("  → Click nút [Vào kênh] (" + x + ", " + y + ")");
            doubleClick(x, y);
            ctx.sleep(LIST_WAIT_MS);
        }
    }

    // ================================================================
    //  BƯỚC 6: Tạo phòng
    // ================================================================

    private void clickCreateRoom(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        int x = profile.getCreateRoomX();
        int y = profile.getCreateRoomY();

        if (x <= 0 || y <= 0) {
            ctx.log("  ⚠ Chưa set tọa độ [Tạo phòng] — bỏ qua.");
            return;
        }

        ctx.log("  → Click [Tạo phòng] (" + x + ", " + y + ")");
        doubleClick(x, y);
        ctx.log("  ⏳ Chờ giao diện tạo phòng...");
        ctx.sleep(ROOM_WAIT_MS);
    }

    // ================================================================
    //  BƯỚC 7: Chế độ AI
    // ================================================================

    private void clickAiMode(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        int x = profile.getAiModeX();
        int y = profile.getAiModeY();

        if (x <= 0 || y <= 0) {
            ctx.log("  ⚠ Chưa set tọa độ [Chế độ AI] — bỏ qua.");
            return;
        }

        ctx.log("  → Click [Chế độ AI] (" + x + ", " + y + ")");
        doubleClick(x, y);
        ctx.sleep(profile.getStepDelayMs());
    }

    // ================================================================
    //  BƯỚC 8: Xác nhận tạo phòng
    // ================================================================

    private void clickConfirmRoom(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        int x = profile.getConfirmX();
        int y = profile.getConfirmY();

        if (x <= 0 || y <= 0) {
            ctx.log("  ⚠ Chưa set tọa độ [Xác nhận tạo phòng] — bỏ qua.");
            return;
        }

        ctx.log("  → Click [Xác nhận tạo phòng] (" + x + ", " + y + ")");
        doubleClick(x, y);
        ctx.log("  ⏳ Chờ phòng được tạo...");
        ctx.sleep(ROOM_WAIT_MS);
    }

    // ================================================================
    //  BƯỚC 9: Bắt đầu chơi game
    // ================================================================

    private void clickStartGame(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        int x = profile.getStartGameX();
        int y = profile.getStartGameY();

        if (x <= 0 || y <= 0) {
            ctx.log("  ⚠ Chưa set tọa độ [Bắt đầu chơi game] — bỏ qua.");
            return;
        }

        ctx.log("  → Click [Bắt đầu chơi game] (" + x + ", " + y + ")");
        doubleClick(x, y);
        ctx.sleep(profile.getStepDelayMs());
    }

    // ================================================================
    //  UTILITIES
    // ================================================================

    /**
     * Click 2 lần tại vị trí (x, y) — game Crossfire yêu cầu double-click.
     */
    private void doubleClick(int x, int y) {
        RobotActions.click(x, y);
        try { Thread.sleep(150); } catch (InterruptedException ignored) {}
        RobotActions.click(x, y);
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
}
