package com.javarpa.macro;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Lưu thông tin đầy đủ của một điểm/nút game đã capture:
 * tọa độ (x, y), màu pixel, label mô tả, timestamp.
 *
 * <p>Được dùng trong Coordinate Picker của tab Macro
 * để phân tích UI game và cấu hình Game Bot.</p>
 */
public class CoordinateEntry {

    private static final DateTimeFormatter TIME_FMT =
        DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    // ===== FIELDS =====

    /** Nhãn mô tả do user tự đặt (VD: "Nút PLAY", "Ô username") */
    private String label;

    /** Tọa độ X tuyệt đối trên màn hình */
    private int x;

    /** Tọa độ Y tuyệt đối trên màn hình */
    private int y;

    /** Màu pixel tại (x, y) dạng "#RRGGBB" */
    private String hexColor;

    /** Thời điểm capture (epoch millis) */
    private long capturedAt;

    // ===== CONSTRUCTORS =====

    public CoordinateEntry() {}

    public CoordinateEntry(String label, int x, int y, String hexColor) {
        this.label       = label;
        this.x           = x;
        this.y           = y;
        this.hexColor    = hexColor;
        this.capturedAt  = System.currentTimeMillis();
    }

    // ===== GETTERS / SETTERS =====

    public String getLabel()    { return label; }
    public void setLabel(String v) { label = v; }

    public int getX()           { return x; }
    public void setX(int v)     { x = v; }

    public int getY()           { return y; }
    public void setY(int v)     { y = v; }

    public String getHexColor() { return hexColor; }
    public void setHexColor(String v) { hexColor = v; }

    public long getCapturedAt() { return capturedAt; }
    public void setCapturedAt(long v) { capturedAt = v; }

    /** Thời gian capture dạng chuỗi HH:mm:ss */
    public String getCapturedAtStr() {
        if (capturedAt == 0) return "--:--:--";
        return TIME_FMT.format(Instant.ofEpochMilli(capturedAt));
    }

    // ===== TO JSON (manual, không cần Gson) =====

    /**
     * Xuất entry ra chuỗi JSON.
     * VD: {"label":"Nút PLAY","x":960,"y":205,"hex":"#1A2B3C","time":"14:30:05"}
     */
    public String toJson() {
        return String.format(
            "{\"label\":\"%s\",\"x\":%d,\"y\":%d,\"hex\":\"%s\",\"time\":\"%s\"}",
            escapeJson(label), x, y,
            hexColor != null ? hexColor : "#000000",
            getCapturedAtStr()
        );
    }

    /** Escape ký tự đặc biệt trong JSON string */
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public String toString() {
        return String.format("[%s] (%d, %d) %s", getCapturedAtStr(), x, y,
            label != null ? label : "");
    }
}
