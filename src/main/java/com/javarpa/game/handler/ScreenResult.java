package com.javarpa.game.handler;

/**
 * Kết quả trả về sau khi một ScreenHandler xử lý xong.
 * GameBot engine dựa vào giá trị này để quyết định bước tiếp theo.
 */
public enum ScreenResult {

    /** Thành công — chuyển sang handler tiếp theo trong pipeline. */
    SUCCESS,

    /** Cần thử lại — quay lại đầu pipeline (dùng cho reconnect). */
    RETRY,

    /** Thất bại nghiêm trọng — dừng bot, báo lỗi. */
    FAIL,

    /** Bỏ qua bước này — handler xác định không cần xử lý (vd: game đã mở sẵn). */
    SKIP
}
