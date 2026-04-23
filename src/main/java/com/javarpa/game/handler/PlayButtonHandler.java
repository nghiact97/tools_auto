package com.javarpa.game.handler;

import com.javarpa.core.RobotActions;
import com.javarpa.game.GameProfile;

/**
 * Handler cho bước 2: Click nút PLAY (Vào Game) trên launcher.
 *
 * <p>detect(): Luôn trả true — bước này luôn cần thực hiện sau khi mở launcher.
 * <br>handle(): Click tọa độ nút PLAY đã được cấu hình trong profile.</p>
 */
public class PlayButtonHandler implements ScreenHandler {

    @Override
    public String getName() {
        return "🎮 Click nút Vào Game";
    }

    @Override
    public boolean detect(BotContext ctx) throws InterruptedException {
        // Luôn thực hiện bước này — launcher đã mở ở bước trước
        return true;
    }

    @Override
    public ScreenResult handle(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        int x = profile.getEnterGameX();
        int y = profile.getEnterGameY();

        if (x <= 0 && y <= 0) {
            ctx.log("⚠ Chưa set tọa độ nút Vào Game — nhấn Enter...");
            RobotActions.enter();
            ctx.sleep(profile.getStepDelayMs() * 2);
            return ScreenResult.SUCCESS;
        }

        ctx.sleep(profile.getStepDelayMs());
        ctx.log("  → Click tọa độ (" + x + ", " + y + ")");
        RobotActions.click(x, y);
        ctx.log("  ✅ Đã click nút Vào Game.");
        ctx.sleep(profile.getStepDelayMs() * 2);

        return ScreenResult.SUCCESS;
    }
}
