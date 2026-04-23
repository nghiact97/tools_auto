package com.javarpa.game;

import com.javarpa.game.handler.*;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Bot engine chạy trên thread riêng, tự động hóa toàn bộ luồng:
 * mở game → đăng nhập → chọn server → vào game → (tùy chọn) tự reconnect.
 *
 * <p>Sử dụng Pipeline Pattern: mỗi bước (màn hình) là một {@link ScreenHandler}
 * độc lập. Bot engine chỉ điều phối thứ tự chạy giữa các handler.</p>
 *
 * <p>Pipeline mặc định:</p>
 * <ol>
 *   <li>{@link LauncherHandler} — Mở launcher game</li>
 *   <li>{@link PlayButtonHandler} — Click nút PLAY</li>
 *   <li>{@link LoginScreenHandler} — Chờ + Đăng nhập</li>
 *   <li>{@link ServerSelectHandler} — Chọn server/channel</li>
 *   <li>{@link InGameHandler} — Monitor in-game (disconnect detect)</li>
 * </ol>
 */
public class GameBot {

    // ================================================================
    //  STATE MACHINE
    // ================================================================

    public enum State {
        IDLE, LAUNCHING, WAITING_LOGIN, LOGGING_IN,
        WAITING_SERVER, SELECTING_SERVER, ENTERING_GAME,
        RUNNING, ERROR, STOPPED
    }

    // ================================================================
    //  PIPELINE — Thứ tự các handler
    // ================================================================

    private final List<ScreenHandler> pipeline = Arrays.asList(
        new LauncherHandler(),        // Bước 1: Mở launcher
        new PlayButtonHandler(),      // Bước 2: Click PLAY
        new LoginScreenHandler(),     // Bước 3: Chờ + Đăng nhập + Verify
        new ServerSelectHandler(),    // Bước 4: Chọn server/channel
        new InGameHandler()           // Bước 5: Monitor in-game
    );

    // ================================================================
    //  FIELDS
    // ================================================================

    private volatile boolean running = false;
    private volatile State   state   = State.IDLE;

    private BotContext       context;
    private GameProfile      profile;
    private Consumer<String> logger;
    private Consumer<State>  onState;
    private Thread           botThread;

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

        botThread = new Thread(this::run, "GameBot-Thread");
        botThread.setDaemon(true);
        botThread.start();
    }

    /** Dừng bot. */
    public void stop() {
        running = false;
        if (context != null) context.stop();
        if (botThread != null) botThread.interrupt();
        setState(State.STOPPED);
    }

    /** Tạm dừng / tiếp tục. */
    public void togglePause() {
        if (context == null) return;
        boolean newPaused = !context.isPaused();
        context.setPaused(newPaused);
        log(newPaused ? "⏸ Tạm dừng bot." : "▶ Tiếp tục bot.");
    }

    public State getState()    { return state; }
    public boolean isRunning() { return running; }

    // ================================================================
    //  MAIN LOOP
    // ================================================================

    private void run() {
        try {
            // Tạo context chia sẻ cho toàn bộ pipeline
            context = new BotContext(profile, msg -> log(msg));

            log("🤖 Bot bắt đầu — profile: " + profile.getProfileName());
            logCoordinates();

            do {
                if (!running) break;
                boolean success = runPipeline();
                if (!success) break;

            } while (running && profile.isAutoReconnect());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("⏹ Bot bị dừng.");
        } catch (Exception e) {
            setState(State.ERROR);
            log("❌ Lỗi bot: " + e.getMessage());
        } finally {
            running = false;
            if (context != null) context.stop();
            if (state != State.ERROR) setState(State.STOPPED);
        }
    }

    /**
     * Chạy toàn bộ pipeline theo thứ tự.
     * Mỗi handler được gọi detect() rồi handle().
     *
     * @return true nếu cần tiếp tục vòng lặp (reconnect),
     *         false nếu cần dừng
     */
    private boolean runPipeline() throws InterruptedException {
        for (ScreenHandler handler : pipeline) {
            if (!running) return false;

            // Cập nhật state dựa theo handler đang chạy
            setState(mapHandlerToState(handler));
            log("▶ " + handler.getName());

            // Detect màn hình
            handler.detect(context);

            // Thực hiện hành động
            ScreenResult result = handler.handle(context);

            switch (result) {
                case SUCCESS:
                    log("  ✓ " + handler.getName() + " hoàn thành.");
                    break; // tiếp tục handler tiếp theo

                case SKIP:
                    log("  ⏭ " + handler.getName() + " bỏ qua.");
                    break; // tiếp tục handler tiếp theo

                case RETRY:
                    log("  🔄 " + handler.getName() + " yêu cầu retry pipeline.");
                    return true; // quay lại đầu do-while → chạy lại pipeline

                case FAIL:
                    log("  ❌ " + handler.getName() + " thất bại — dừng bot.");
                    setState(State.ERROR);
                    running = false;
                    return false;
            }
        }

        // Pipeline chạy xong bình thường
        setState(State.RUNNING);
        return false; // không cần loop lại (trừ khi autoReconnect)
    }

    // ================================================================
    //  HELPERS
    // ================================================================

    /**
     * Map ScreenHandler → State để hiển thị trên UI.
     */
    private State mapHandlerToState(ScreenHandler handler) {
        if (handler instanceof LauncherHandler)     return State.LAUNCHING;
        if (handler instanceof PlayButtonHandler)   return State.ENTERING_GAME;
        if (handler instanceof LoginScreenHandler)  return State.LOGGING_IN;
        if (handler instanceof ServerSelectHandler) return State.SELECTING_SERVER;
        if (handler instanceof InGameHandler)       return State.RUNNING;
        return State.IDLE;
    }

    private void setState(State s) {
        state = s;
        if (onState != null) onState.accept(s);
    }

    private void log(String msg) {
        if (logger != null) logger.accept(msg);
    }

    private void logCoordinates() {
        log("📋 Tọa độ: PLAY(" + profile.getEnterGameX() + "," + profile.getEnterGameY()
            + ") User(" + profile.getUsernameX() + "," + profile.getUsernameY()
            + ") Pass(" + profile.getPasswordX() + "," + profile.getPasswordY()
            + ") Login(" + profile.getLoginBtnX() + "," + profile.getLoginBtnY()
            + ") Server(" + profile.getServerX() + "," + profile.getServerY() + ")");
    }
}
