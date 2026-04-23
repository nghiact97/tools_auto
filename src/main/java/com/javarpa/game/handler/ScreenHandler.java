package com.javarpa.game.handler;

/**
 * Interface chung cho mỗi màn hình game.
 * Mỗi handler quản lý 1 giao diện duy nhất trong luồng bot.
 *
 * <p>Ví dụ luồng: LauncherHandler → PlayButtonHandler → LoginScreenHandler
 * → ServerSelectHandler → InGameHandler</p>
 */
public interface ScreenHandler {

    /**
     * Tên hiển thị của handler (dùng cho log/debug).
     */
    String getName();

    /**
     * Kiểm tra xem màn hình này có đang hiển thị trên screen không.
     * Sử dụng pixel detect hoặc process check để xác nhận.
     *
     * @param ctx BotContext chứa profile, logger, running flag
     * @return true nếu màn hình này đang hiển thị
     * @throws InterruptedException nếu bot bị dừng giữa chừng
     */
    boolean detect(BotContext ctx) throws InterruptedException;

    /**
     * Thực hiện hành động trên màn hình này.
     * Ví dụ: nhập tài khoản, click nút, chọn server...
     *
     * @param ctx BotContext chứa profile, logger, running flag
     * @return ScreenResult cho biết kết quả (SUCCESS, RETRY, FAIL, SKIP)
     * @throws InterruptedException nếu bot bị dừng giữa chừng
     */
    ScreenResult handle(BotContext ctx) throws InterruptedException;
}
