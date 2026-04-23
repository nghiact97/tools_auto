package com.javarpa.game.handler;

import com.javarpa.core.PixelDetector;
import com.javarpa.core.RobotActions;
import com.javarpa.game.GameProfile;

import java.awt.Color;

/**
 * Handler cho bước 4: Chờ + Chọn server/channel.
 *
 * <p>detect(): Pixel detect màn hình chọn server đã xuất hiện.
 * <br>handle(): Click tọa độ server/channel muốn chọn.</p>
 */
public class ServerSelectHandler implements ScreenHandler {

    @Override
    public String getName() {
        return "🖥 Chọn Server/Channel";
    }

    /**
     * Chờ màn hình chọn server xuất hiện.
     * Sử dụng pixel detect nếu cấu hình, hoặc chờ cố định.
     */
    @Override
    public boolean detect(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        ctx.log("⏳ Chờ màn hình chọn server...");

        if (profile.getServerDetectX() > 0 || profile.getServerDetectY() > 0) {
            Color target = hexToColor(profile.getServerDetectHex());
            boolean found = PixelDetector.waitForColor(
                profile.getServerDetectX(), profile.getServerDetectY(),
                target, 30, profile.getWaitTimeoutMs()
            );
            if (found) {
                ctx.log("✅ Màn hình chọn server đã xuất hiện.");
            } else {
                ctx.log("⚠ Timeout chờ server screen — thử tiếp...");
            }
        } else {
            // Không có pixel detect → chờ cố định
            ctx.sleep(profile.getStepDelayMs() * 3);
        }

        return true;
    }

    /**
     * Chọn server/channel theo thứ tự:
     * 1. Click nút "Chọn kênh" trên home screen (nếu cấu hình)
     * 2. Click tọa độ channel cụ thể (dựa theo serverName)
     * 3. Hoặc click tọa độ server trực tiếp (nếu serverX/Y có giá trị)
     */
    @Override
    public ScreenResult handle(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        String serverName = profile.getServerName();
        ctx.log("🖥 Chọn server: " + serverName);

        // Bước 1: Click nút "Chọn kênh" (nếu cần vào menu channel)
        if (profile.getChannelBtnX() > 0 && profile.getChannelBtnY() > 0) {
            ctx.log("  → Click nút Chọn kênh (" + profile.getChannelBtnX()
                + ", " + profile.getChannelBtnY() + ")");
            RobotActions.click(profile.getChannelBtnX(), profile.getChannelBtnY());
            ctx.sleep(profile.getStepDelayMs());
        }

        // Bước 2: Chọn channel theo tên
        int[] channelCoords = profile.getChannelCoords(serverName);
        if (channelCoords[0] > 0 && channelCoords[1] > 0) {
            ctx.log("  → Click channel '" + serverName + "' (" + channelCoords[0]
                + ", " + channelCoords[1] + ")");
            RobotActions.click(channelCoords[0], channelCoords[1]);
            ctx.sleep(profile.getStepDelayMs());
            return ScreenResult.SUCCESS;
        }

        // Bước 3: Fallback — click tọa độ server trực tiếp
        if (profile.getServerX() > 0 && profile.getServerY() > 0) {
            ctx.log("  → Click server (" + profile.getServerX()
                + ", " + profile.getServerY() + ")");
            RobotActions.click(profile.getServerX(), profile.getServerY());
            ctx.sleep(profile.getStepDelayMs());
            return ScreenResult.SUCCESS;
        }

        ctx.log("  ⚠ Chưa set tọa độ server — bỏ qua.");
        return ScreenResult.SKIP;
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
