package com.javarpa.game.handler;

import com.javarpa.game.GameProfile;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Handler cho bước 1: Mở launcher game (.exe).
 *
 * <p>detect(): Kiểm tra process launcher đã chạy chưa → nếu đã chạy thì SKIP.
 * <br>handle(): Mở exe file, hỗ trợ RunAsAdmin qua PowerShell.</p>
 */
public class LauncherHandler implements ScreenHandler {

    @Override
    public String getName() {
        return "🚀 Mở Launcher";
    }

    @Override
    public boolean detect(BotContext ctx) throws InterruptedException {
        String exePath = ctx.getProfile().getExePath();
        if (exePath == null || exePath.trim().isEmpty()) {
            return false; // không có exe → skip
        }

        File exeFile = new File(exePath);
        String exeName = exeFile.getName();
        // Nếu process đã chạy → không cần mở lại
        return !ctx.isProcessRunning(exeName);
    }

    @Override
    public ScreenResult handle(BotContext ctx) throws InterruptedException {
        GameProfile profile = ctx.getProfile();
        String exePath = profile.getExePath();

        // Không có exe path → bỏ qua
        if (exePath == null || exePath.trim().isEmpty()) {
            ctx.log("⚠ Không có đường dẫn exe — bỏ qua bước mở game.");
            return ScreenResult.SKIP;
        }

        File exeFile = new File(exePath);

        // Kiểm tra file tồn tại
        if (!exeFile.exists()) {
            ctx.log("❌ File không tồn tại: " + exePath);
            return ScreenResult.FAIL;
        }

        // Kiểm tra process đã chạy chưa
        String exeName = exeFile.getName();
        if (ctx.isProcessRunning(exeName)) {
            ctx.log("ℹ Process [" + exeName + "] đã đang chạy — bỏ qua mở exe.");
            return ScreenResult.SKIP;
        }

        // Mở exe
        try {
            if (profile.isRunAsAdmin()) {
                launchAsAdmin(ctx, exePath);
            } else {
                launchDirect(ctx, exeFile);
            }
        } catch (IOException e) {
            ctx.log("❌ Lỗi mở exe: " + e.getMessage());
            ctx.log("💡 Hint: Nếu game cần quyền Admin → tick 'Run as Admin' và chạy Tool bằng run.bat");
            return ScreenResult.FAIL;
        }

        // Chờ launcher khởi động
        long waitMs = profile.getLaunchWaitMs();
        ctx.log("⏳ Chờ " + waitMs + "ms để game khởi động...");
        ctx.sleep(waitMs);

        return ScreenResult.SUCCESS;
    }

    // ==================== PRIVATE ====================

    private void launchAsAdmin(BotContext ctx, String exePath) throws IOException, InterruptedException {
        ctx.log("🚀 [Admin] Đang mở: " + exePath);
        String safeExePath = exePath.replace("'", "''");
        ProcessBuilder pb = new ProcessBuilder(
            "powershell.exe", "-NoProfile", "-NonInteractive", "-Command",
            "Start-Process -FilePath '" + safeExePath + "' -Verb RunAs -ErrorAction Stop"
        );
        pb.redirectErrorStream(true);
        Process ps = pb.start();

        String psOut = new String(ps.getInputStream().readAllBytes()).trim();
        boolean exited = ps.waitFor(5, TimeUnit.SECONDS);
        int code = exited ? ps.exitValue() : -1;

        if (!psOut.isEmpty()) {
            ctx.log("  [PS] " + psOut);
        }
        if (code != 0) {
            ctx.log("⚠ PowerShell exit " + code + " — game có thể không mở được.");
        } else {
            ctx.log("✅ Đã gửi lệnh mở exe (Admin).");
        }
    }

    private void launchDirect(BotContext ctx, File exeFile) throws IOException {
        ctx.log("🚀 Đang mở: " + exeFile.getAbsolutePath());
        ProcessBuilder pb = new ProcessBuilder(exeFile.getAbsolutePath());
        pb.directory(exeFile.getParentFile());
        pb.start();
        ctx.log("✅ Đã gửi lệnh mở exe.");
    }
}
