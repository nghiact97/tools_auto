package com.javarpa.game;

/**
 * Lưu toàn bộ cấu hình bot cho một game:
 * đường dẫn exe, tài khoản, toạ độ UI, pixel detect.
 * Được serialize/deserialize bằng Gson.
 */
public class GameProfile {

    // ========== THÔNG TIN CƠ BẢN ==========
    private String profileName = "Mặc định";
    private String gameName    = "Game";
    private String exePath     = "";          // đường dẫn launcher .exe

    // ========== TÀI KHOẢN ==========
    private String username    = "";
    private String passwordEnc = "";          // AES-128 encrypted

    // ========== MÁY CHỦ + KÊNH ==========
    private String serverName    = "Tan Binh";  // tên máy chủ (Tan Binh, Tu do 1, ...)
    private int    channelNumber = 1;            // kênh mong muốn (1-6)

    // ========== TOẠ ĐỘ UI ==========
    private int usernameX  = 1163, usernameY  = 735;
    private int passwordX  = 1152, passwordY  = 768;
    private int loginBtnX  = 1376, loginBtnY  = 764;
    private int serverX    = 0, serverY    = 0;
    private int enterGameX = 960, enterGameY = 205;

    // ========== PIXEL DETECT ==========
    private int loginDetectX     = 0, loginDetectY     = 0;
    private String loginDetectHex = "#FFFFFF";

    private int serverDetectX     = 0, serverDetectY     = 0;
    private String serverDetectHex = "#FFFFFF";

    // ========== CÀI ĐẶT BOT ==========
    private long stepDelayMs    = 800;
    private long waitTimeoutMs  = 15000;
    private long launchWaitMs   = 8000;
    private boolean autoReconnect = false;
    private boolean runAsAdmin    = false;

    // ========== LOGIN RETRY ==========
    private int loginRetryCount    = 3;
    private long loginRetryDelayMs = 3000;

    // ========== DEFAULT VALUES (Crossfire) ==========
    private static final int DEF_USERNAME_X = 1163, DEF_USERNAME_Y = 735;
    private static final int DEF_PASSWORD_X = 1152, DEF_PASSWORD_Y = 768;
    private static final int DEF_LOGINBTN_X = 1376, DEF_LOGINBTN_Y = 764;
    private static final int DEF_ENTERGAME_X = 960, DEF_ENTERGAME_Y = 205;

    // Nút "Chọn kênh" trên home screen — default mới từ dữ liệu capture
    private int channelBtnX = 0, channelBtnY = 0;
    private static final int DEF_CHANNELBTN_X = 626, DEF_CHANNELBTN_Y = 196;

    // Nút "Vào kênh" sau khi chọn kênh
    private int enterChannelBtnX = 0, enterChannelBtnY = 0;
    private static final int DEF_ENTERCHANNELBTN_X = 1406, DEF_ENTERCHANNELBTN_Y = 909;

    // ========== TỌA ĐỘ TỪNG MÁY CHỦ (lưu riêng từng cặp X,Y) ==========
    // Giá trị mặc định từ dữ liệu thực tế đã capture
    private int serverTanBinhX = 0, serverTanBinhY = 0;
    private int serverTuDo1X  = 0, serverTuDo1Y  = 0;
    private int serverTuDo2X  = 0, serverTuDo2Y  = 0;
    private int serverTuDo3X  = 0, serverTuDo3Y  = 0;
    private int serverTuDo4X  = 0, serverTuDo4Y  = 0;

    private static final int DEF_SERVER_TANBINH_X = 911,  DEF_SERVER_TANBINH_Y = 328;
    private static final int DEF_SERVER_TUDO1_X   = 1248, DEF_SERVER_TUDO1_Y   = 332;
    private static final int DEF_SERVER_TUDO2_X   = 928,  DEF_SERVER_TUDO2_Y   = 360;
    private static final int DEF_SERVER_TUDO3_X   = 1228, DEF_SERVER_TUDO3_Y   = 357;
    private static final int DEF_SERVER_TUDO4_X   = 911,  DEF_SERVER_TUDO4_Y   = 385;

    // ========== TỌA ĐỘ TỪNG KÊNH (lưu riêng từng cặp X,Y) ==========
    // Giá trị mặc định từ dữ liệu thực tế đã capture
    private int channel1X = 0, channel1Y = 0;
    private int channel2X = 0, channel2Y = 0;
    private int channel3X = 0, channel3Y = 0;
    private int channel4X = 0, channel4Y = 0;
    private int channel5X = 0, channel5Y = 0;
    private int channel6X = 0, channel6Y = 0;

    private static final int DEF_CH1_X = 1298, DEF_CH1_Y = 328;
    private static final int DEF_CH2_X = 1305, DEF_CH2_Y = 357;
    private static final int DEF_CH3_X = 1304, DEF_CH3_Y = 386;
    private static final int DEF_CH4_X = 1309, DEF_CH4_Y = 414;
    private static final int DEF_CH5_X = 1348, DEF_CH5_Y = 440;
    private static final int DEF_CH6_X = 1329, DEF_CH6_Y = 471;

    // ========== NÚT TẠO PHÒNG / CHẾ ĐỘ AI / XÁC NHẬN / HỦY / BẮT ĐẦU ==========
    private int createRoomX = 0, createRoomY = 0;
    private int aiModeX     = 0, aiModeY     = 0;
    private int confirmX    = 0, confirmY    = 0;
    private int cancelRoomX = 0, cancelRoomY = 0;
    private int startGameX  = 0, startGameY  = 0;
    private int sysConfirmX = 0, sysConfirmY = 0;
    private int closeNotifyX = 0, closeNotifyY = 0;

    private static final int DEF_CREATE_ROOM_X = 841,  DEF_CREATE_ROOM_Y = 715;
    private static final int DEF_AI_MODE_X     = 729,  DEF_AI_MODE_Y     = 374;
    private static final int DEF_CONFIRM_X     = 1242, DEF_CONFIRM_Y     = 827;
    private static final int DEF_CANCEL_ROOM_X = 1342, DEF_CANCEL_ROOM_Y = 829;
    private static final int DEF_START_GAME_X  = 1384, DEF_START_GAME_Y  = 716;
    private static final int DEF_SYS_CONFIRM_X = 960,  DEF_SYS_CONFIRM_Y = 624;
    private static final int DEF_CLOSE_NOTIFY_X = 957, DEF_CLOSE_NOTIFY_Y = 876;

    // ========== NÚT GAMEPLAY MỚI (từ dữ liệu capture) ==========
    // Nhập mật khẩu phong (khi tạo phòng có khóa)
    private int enterPasswordX = 0, enterPasswordY = 0;
    // Cài đặt (mở menu cài đặt)
    private int settingsX = 0, settingsY = 0;
    // Tab Điều khiển trong cài đặt
    private int controlsTabX = 0, controlsTabY = 0;
    // Nút Mặc định (reset controls)
    private int defaultBtnX = 0, defaultBtnY = 0;
    // Xác nhận cài đặt
    private int confirmSettingsX = 0, confirmSettingsY = 0;
    // Lưu cài đặt
    private int saveSettingsX = 0, saveSettingsY = 0;
    // Xác nhận sau khi lưu cài đặt
    private int confirmAfterSaveX = 0, confirmAfterSaveY = 0;
    // Trở lại game
    private int backToGameX = 0, backToGameY = 0;
    // Sảnh mới
    private int newLobbyX = 0, newLobbyY = 0;
    // Sảnh chính
    private int mainLobbyX = 0, mainLobbyY = 0;
    // Kho đồ
    private int inventoryX = 0, inventoryY = 0;

    private static final int DEF_ENTER_PASSWORD_X   = 1082, DEF_ENTER_PASSWORD_Y   = 621;
    private static final int DEF_SETTINGS_X          = 1218, DEF_SETTINGS_Y          = 303;
    private static final int DEF_CONTROLS_TAB_X      = 578,  DEF_CONTROLS_TAB_Y      = 497;
    private static final int DEF_DEFAULT_BTN_X       = 841,  DEF_DEFAULT_BTN_Y       = 448;
    private static final int DEF_CONFIRM_SETTINGS_X  = 1002, DEF_CONFIRM_SETTINGS_Y  = 650;
    private static final int DEF_SAVE_SETTINGS_X     = 960,  DEF_SAVE_SETTINGS_Y     = 692;
    private static final int DEF_CONFIRM_AFTER_SAVE_X = 591, DEF_CONFIRM_AFTER_SAVE_Y = 402;
    private static final int DEF_BACK_TO_GAME_X      = 362,  DEF_BACK_TO_GAME_Y      = 566;
    private static final int DEF_NEW_LOBBY_X         = 538,  DEF_NEW_LOBBY_Y         = 202;
    private static final int DEF_MAIN_LOBBY_X        = 943,  DEF_MAIN_LOBBY_Y        = 388;
    private static final int DEF_INVENTORY_X         = 625,  DEF_INVENTORY_Y         = 167;

    // ========== DANH SÁCH MÁY CHỦ ==========
    /** Tên các máy chủ có trong game. */
    public static final String[] SERVER_NAMES = {
        "Tan Binh", "Tu do 1", "Tu do 2", "Tu do 3", "Tu do 4"
    };

    /** Số kênh tối đa trong mỗi máy chủ. */
    public static final int MAX_CHANNELS = 6;

    /**
     * Áp dụng tọa độ mặc định cho các field = 0.
     * Gọi sau khi Gson deserialize từ JSON để fill giá trị thiếu.
     */
    public void applyDefaults() {
        if (usernameX   == 0 && usernameY   == 0) { usernameX   = DEF_USERNAME_X;   usernameY   = DEF_USERNAME_Y; }
        if (passwordX   == 0 && passwordY   == 0) { passwordX   = DEF_PASSWORD_X;   passwordY   = DEF_PASSWORD_Y; }
        if (loginBtnX   == 0 && loginBtnY   == 0) { loginBtnX   = DEF_LOGINBTN_X;   loginBtnY   = DEF_LOGINBTN_Y; }
        if (enterGameX  == 0 && enterGameY  == 0) { enterGameX  = DEF_ENTERGAME_X;  enterGameY  = DEF_ENTERGAME_Y; }

        // Nút Chọn kênh — default mới từ dữ liệu capture (626, 196)
        if (channelBtnX == 0 && channelBtnY == 0) { channelBtnX = DEF_CHANNELBTN_X; channelBtnY = DEF_CHANNELBTN_Y; }
        // Migration: sửa tọa độ cũ sai
        if (channelBtnX == 680 && channelBtnY == 168) { channelBtnX = DEF_CHANNELBTN_X; channelBtnY = DEF_CHANNELBTN_Y; }
        if (channelBtnX == 640 && channelBtnY == 197) { channelBtnX = DEF_CHANNELBTN_X; channelBtnY = DEF_CHANNELBTN_Y; }

        if (enterChannelBtnX == 0 && enterChannelBtnY == 0) {
            enterChannelBtnX = DEF_ENTERCHANNELBTN_X;
            enterChannelBtnY = DEF_ENTERCHANNELBTN_Y;
        }

        // Tọa độ máy chủ — migration: reset tọa độ cũ về giá trị mới (captured 24/04/2026)
        if (serverTanBinhX == 0 && serverTanBinhY == 0 || serverTanBinhX == 883 && serverTanBinhY == 327) { serverTanBinhX = DEF_SERVER_TANBINH_X; serverTanBinhY = DEF_SERVER_TANBINH_Y; }
        if (serverTuDo1X   == 0 && serverTuDo1Y   == 0 || serverTuDo1X == 1231 && serverTuDo1Y == 341) { serverTuDo1X   = DEF_SERVER_TUDO1_X;   serverTuDo1Y   = DEF_SERVER_TUDO1_Y; }
        if (serverTuDo2X   == 0 && serverTuDo2Y   == 0 || serverTuDo2X == 1412 && serverTuDo2Y == 174) { serverTuDo2X   = DEF_SERVER_TUDO2_X;   serverTuDo2Y   = DEF_SERVER_TUDO2_Y; }
        if (serverTuDo3X   == 0 && serverTuDo3Y   == 0 || serverTuDo3X == 1176 && serverTuDo3Y == 601) { serverTuDo3X   = DEF_SERVER_TUDO3_X;   serverTuDo3Y   = DEF_SERVER_TUDO3_Y; }
        if (serverTuDo4X   == 0 && serverTuDo4Y   == 0 || serverTuDo4X == 1401 && serverTuDo4Y == 170) { serverTuDo4X   = DEF_SERVER_TUDO4_X;   serverTuDo4Y   = DEF_SERVER_TUDO4_Y; }

        // Tọa độ kênh — migration: reset tọa độ cũ về giá trị mới (captured 24/04/2026)
        if (channel1X == 0 && channel1Y == 0 || channel1X == 1207 && channel1Y == 446) { channel1X = DEF_CH1_X; channel1Y = DEF_CH1_Y; }
        if (channel2X == 0 && channel2Y == 0 || channel2X == 1401 && channel2Y == 168) { channel2X = DEF_CH2_X; channel2Y = DEF_CH2_Y; }
        if (channel3X == 0 && channel3Y == 0 || channel3X == 1405 && channel3Y == 175) { channel3X = DEF_CH3_X; channel3Y = DEF_CH3_Y; }
        if (channel4X == 0 && channel4Y == 0 || channel4X == 1375 && channel4Y == 202) { channel4X = DEF_CH4_X; channel4Y = DEF_CH4_Y; }
        if (channel5X == 0 && channel5Y == 0 || channel5X == 471  && channel5Y == 747) { channel5X = DEF_CH5_X; channel5Y = DEF_CH5_Y; }
        if (channel6X == 0 && channel6Y == 0 || channel6X == 882  && channel6Y == 496) { channel6X = DEF_CH6_X; channel6Y = DEF_CH6_Y; }

        // Nút Tạo phòng / Chế độ AI / Xác nhận / Hủy / Bắt đầu — migration (captured 24/04/2026)
        if (createRoomX == 0 && createRoomY == 0 || createRoomX == 1607 && createRoomY == 535) { createRoomX = DEF_CREATE_ROOM_X; createRoomY = DEF_CREATE_ROOM_Y; }
        if (aiModeX     == 0 && aiModeY     == 0 || aiModeX == 1639 && aiModeY == 621)         { aiModeX     = DEF_AI_MODE_X;     aiModeY     = DEF_AI_MODE_Y; }
        if (confirmX    == 0 && confirmY    == 0 || confirmX == 1234 && confirmY == 791)        { confirmX    = DEF_CONFIRM_X;      confirmY    = DEF_CONFIRM_Y; }
        if (cancelRoomX == 0 && cancelRoomY == 0) { cancelRoomX = DEF_CANCEL_ROOM_X; cancelRoomY = DEF_CANCEL_ROOM_Y; }
        if (startGameX  == 0 && startGameY  == 0) { startGameX  = DEF_START_GAME_X;  startGameY  = DEF_START_GAME_Y; }
        if (sysConfirmX == 0 && sysConfirmY == 0) { sysConfirmX = DEF_SYS_CONFIRM_X; sysConfirmY = DEF_SYS_CONFIRM_Y; }
        if (closeNotifyX == 0 && closeNotifyY == 0) { closeNotifyX = DEF_CLOSE_NOTIFY_X; closeNotifyY = DEF_CLOSE_NOTIFY_Y; }

        // Nút Gameplay mới
        if (enterPasswordX   == 0 && enterPasswordY   == 0) { enterPasswordX   = DEF_ENTER_PASSWORD_X;   enterPasswordY   = DEF_ENTER_PASSWORD_Y; }
        if (settingsX         == 0 && settingsY         == 0) { settingsX         = DEF_SETTINGS_X;         settingsY         = DEF_SETTINGS_Y; }
        if (controlsTabX      == 0 && controlsTabY      == 0) { controlsTabX      = DEF_CONTROLS_TAB_X;      controlsTabY      = DEF_CONTROLS_TAB_Y; }
        if (defaultBtnX       == 0 && defaultBtnY       == 0) { defaultBtnX       = DEF_DEFAULT_BTN_X;       defaultBtnY       = DEF_DEFAULT_BTN_Y; }
        if (confirmSettingsX  == 0 && confirmSettingsY  == 0) { confirmSettingsX  = DEF_CONFIRM_SETTINGS_X;  confirmSettingsY  = DEF_CONFIRM_SETTINGS_Y; }
        if (saveSettingsX     == 0 && saveSettingsY     == 0) { saveSettingsX     = DEF_SAVE_SETTINGS_X;     saveSettingsY     = DEF_SAVE_SETTINGS_Y; }
        if (confirmAfterSaveX == 0 && confirmAfterSaveY == 0) { confirmAfterSaveX = DEF_CONFIRM_AFTER_SAVE_X; confirmAfterSaveY = DEF_CONFIRM_AFTER_SAVE_Y; }
        if (backToGameX       == 0 && backToGameY       == 0) { backToGameX       = DEF_BACK_TO_GAME_X;       backToGameY       = DEF_BACK_TO_GAME_Y; }
        if (newLobbyX         == 0 && newLobbyY         == 0) { newLobbyX         = DEF_NEW_LOBBY_X;         newLobbyY         = DEF_NEW_LOBBY_Y; }
        if (mainLobbyX        == 0 && mainLobbyY        == 0) { mainLobbyX        = DEF_MAIN_LOBBY_X;        mainLobbyY        = DEF_MAIN_LOBBY_Y; }
        if (inventoryX        == 0 && inventoryY        == 0) { inventoryX        = DEF_INVENTORY_X;         inventoryY        = DEF_INVENTORY_Y; }

        if (serverName == null || serverName.isEmpty()) serverName = "Tan Binh";
        // Migration: chuyển tên có dấu / bị lỗi encoding sang không dấu
        serverName = migrateServerName(serverName);
        if (channelNumber < 1 || channelNumber > MAX_CHANNELS) channelNumber = 1;
    }

    /**
     * Chuyển tên máy chủ cũ (có dấu / bị lỗi encoding) sang không dấu.
     * "Tân Binh" / "T?n Binh" → "Tan Binh"
     * "Tự do 2" / "T? do 2"  → "Tu do 2"
     */
    private String migrateServerName(String name) {
        if (name == null) return "Tan Binh";
        String lower = name.toLowerCase();
        if (lower.contains("do 1")) return "Tu do 1";
        if (lower.contains("do 2")) return "Tu do 2";
        if (lower.contains("do 3")) return "Tu do 3";
        if (lower.contains("do 4")) return "Tu do 4";
        if (lower.contains("binh") || lower.contains("tan")) return "Tan Binh";
        return name;
    }

    // ========== TỌA ĐỘ MÁY CHỦ (lấy từ field riêng, cấu hình được) ==========

    /**
     * Trả về tọa độ (x, y) của máy chủ trong bảng Chọn kênh.
     * Tên không dấu để tránh lỗi encoding.
     *
     * <pre>
     *  Tan Binh   (911, 328)   |  Tu do 1  (1248, 332)
     *  Tu do 2    (928, 360)   |  Tu do 3  (1228, 357)
     *  Tu do 4    (911, 385)
     * </pre>
     */
    public int[] getServerCoords(String server) {
        if (server == null) return new int[]{0, 0};
        switch (server.trim()) {
            case "Tan Binh":  return new int[]{serverTanBinhX, serverTanBinhY};
            case "Tu do 1":   return new int[]{serverTuDo1X,   serverTuDo1Y};
            case "Tu do 2":   return new int[]{serverTuDo2X,   serverTuDo2Y};
            case "Tu do 3":   return new int[]{serverTuDo3X,   serverTuDo3Y};
            case "Tu do 4":   return new int[]{serverTuDo4X,   serverTuDo4Y};
            default:           return new int[]{serverTanBinhX, serverTanBinhY};
        }
    }

    // ========== TỌA ĐỘ KÊNH (lưu từng kênh riêng biệt) ==========

    /**
     * Trả về tọa độ (x, y) của kênh trong danh sách kênh.
     * Dữ liệu thực tế từ game Crossfire (đã capture).
     *
     * <pre>
     *  Kênh 1  (1298, 328)   Kênh 2  (1305, 357)
     *  Kênh 3  (1304, 386)   Kênh 4  (1309, 414)
     *  Kênh 5  (1348, 440)   Kênh 6  (1329, 471)
     * </pre>
     *
     * @param channel số kênh 1-6
     */
    public int[] getChannelRowCoords(int channel) {
        if (channel < 1) channel = 1;
        if (channel > MAX_CHANNELS) channel = MAX_CHANNELS;
        switch (channel) {
            case 1: return new int[]{channel1X, channel1Y};
            case 2: return new int[]{channel2X, channel2Y};
            case 3: return new int[]{channel3X, channel3Y};
            case 4: return new int[]{channel4X, channel4Y};
            case 5: return new int[]{channel5X, channel5Y};
            case 6: return new int[]{channel6X, channel6Y};
            default: return new int[]{channel1X, channel1Y};
        }
    }

    /**
     * @deprecated Dùng {@link #getServerCoords(String)} cho máy chủ
     *             và {@link #getChannelRowCoords(int)} cho kênh.
     */
    @Deprecated
    public int[] getChannelCoords(String channelName) {
        return getServerCoords(channelName);
    }

    // ========== NÚT "CHỌN KÊNH" ==========

    public int getChannelBtnX() { return channelBtnX; }
    public int getChannelBtnY() { return channelBtnY; }
    public void setChannelBtnCoords(int x, int y) { channelBtnX = x; channelBtnY = y; }

    // ========== NÚT "VÀO KÊNH" ==========

    public int getEnterChannelBtnX() { return enterChannelBtnX; }
    public int getEnterChannelBtnY() { return enterChannelBtnY; }
    public void setEnterChannelBtnCoords(int x, int y) { enterChannelBtnX = x; enterChannelBtnY = y; }

    // ========== SETTERS TỌA ĐỘ MÁY CHỦ (từng server) ==========

    public void setServerTanBinhCoords(int x, int y)  { serverTanBinhX = x; serverTanBinhY = y; }
    public void setServerTuDo1Coords(int x, int y)    { serverTuDo1X   = x; serverTuDo1Y   = y; }
    public void setServerTuDo2Coords(int x, int y)    { serverTuDo2X   = x; serverTuDo2Y   = y; }
    public void setServerTuDo3Coords(int x, int y)    { serverTuDo3X   = x; serverTuDo3Y   = y; }
    public void setServerTuDo4Coords(int x, int y)    { serverTuDo4X   = x; serverTuDo4Y   = y; }

    public int getServerTanBinhX() { return serverTanBinhX; } public int getServerTanBinhY() { return serverTanBinhY; }
    public int getServerTuDo1X()   { return serverTuDo1X; }   public int getServerTuDo1Y()   { return serverTuDo1Y; }
    public int getServerTuDo2X()   { return serverTuDo2X; }   public int getServerTuDo2Y()   { return serverTuDo2Y; }
    public int getServerTuDo3X()   { return serverTuDo3X; }   public int getServerTuDo3Y()   { return serverTuDo3Y; }
    public int getServerTuDo4X()   { return serverTuDo4X; }   public int getServerTuDo4Y()   { return serverTuDo4Y; }

    // ========== SETTERS TỌA ĐỘ KÊNH (từng kênh) ==========

    public void setChannel1Coords(int x, int y) { channel1X = x; channel1Y = y; }
    public void setChannel2Coords(int x, int y) { channel2X = x; channel2Y = y; }
    public void setChannel3Coords(int x, int y) { channel3X = x; channel3Y = y; }
    public void setChannel4Coords(int x, int y) { channel4X = x; channel4Y = y; }
    public void setChannel5Coords(int x, int y) { channel5X = x; channel5Y = y; }
    public void setChannel6Coords(int x, int y) { channel6X = x; channel6Y = y; }

    public int getChannel1X() { return channel1X; } public int getChannel1Y() { return channel1Y; }
    public int getChannel2X() { return channel2X; } public int getChannel2Y() { return channel2Y; }
    public int getChannel3X() { return channel3X; } public int getChannel3Y() { return channel3Y; }
    public int getChannel4X() { return channel4X; } public int getChannel4Y() { return channel4Y; }
    public int getChannel5X() { return channel5X; } public int getChannel5Y() { return channel5Y; }
    public int getChannel6X() { return channel6X; } public int getChannel6Y() { return channel6Y; }

    // ========== NÚT TẠO PHÒNG / CHẾ ĐỘ AI / XÁC NHẬN / HỦY / BẮT ĐẦU ==========

    public int getCreateRoomX() { return createRoomX; }
    public int getCreateRoomY() { return createRoomY; }
    public void setCreateRoomCoords(int x, int y) { createRoomX = x; createRoomY = y; }

    public int getAiModeX() { return aiModeX; }
    public int getAiModeY() { return aiModeY; }
    public void setAiModeCoords(int x, int y) { aiModeX = x; aiModeY = y; }

    public int getConfirmX() { return confirmX; }
    public int getConfirmY() { return confirmY; }
    public void setConfirmCoords(int x, int y) { confirmX = x; confirmY = y; }

    public int getCancelRoomX() { return cancelRoomX; }
    public int getCancelRoomY() { return cancelRoomY; }
    public void setCancelRoomCoords(int x, int y) { cancelRoomX = x; cancelRoomY = y; }

    public int getStartGameX() { return startGameX; }
    public int getStartGameY() { return startGameY; }
    public void setStartGameCoords(int x, int y) { startGameX = x; startGameY = y; }

    public int getSysConfirmX() { return sysConfirmX; }
    public int getSysConfirmY() { return sysConfirmY; }
    public void setSysConfirmCoords(int x, int y) { sysConfirmX = x; sysConfirmY = y; }

    public int getCloseNotifyX() { return closeNotifyX; }
    public int getCloseNotifyY() { return closeNotifyY; }
    public void setCloseNotifyCoords(int x, int y) { closeNotifyX = x; closeNotifyY = y; }

    // ========== NÚT GAMEPLAY MỚI ==========

    public int getEnterPasswordX()  { return enterPasswordX; }
    public int getEnterPasswordY()  { return enterPasswordY; }
    public void setEnterPasswordCoords(int x, int y) { enterPasswordX = x; enterPasswordY = y; }

    public int getSettingsX()       { return settingsX; }
    public int getSettingsY()       { return settingsY; }
    public void setSettingsCoords(int x, int y) { settingsX = x; settingsY = y; }

    public int getControlsTabX()    { return controlsTabX; }
    public int getControlsTabY()    { return controlsTabY; }
    public void setControlsTabCoords(int x, int y) { controlsTabX = x; controlsTabY = y; }

    public int getDefaultBtnX()     { return defaultBtnX; }
    public int getDefaultBtnY()     { return defaultBtnY; }
    public void setDefaultBtnCoords(int x, int y) { defaultBtnX = x; defaultBtnY = y; }

    public int getConfirmSettingsX(){ return confirmSettingsX; }
    public int getConfirmSettingsY(){ return confirmSettingsY; }
    public void setConfirmSettingsCoords(int x, int y) { confirmSettingsX = x; confirmSettingsY = y; }

    public int getSaveSettingsX()   { return saveSettingsX; }
    public int getSaveSettingsY()   { return saveSettingsY; }
    public void setSaveSettingsCoords(int x, int y) { saveSettingsX = x; saveSettingsY = y; }

    public int getConfirmAfterSaveX(){ return confirmAfterSaveX; }
    public int getConfirmAfterSaveY(){ return confirmAfterSaveY; }
    public void setConfirmAfterSaveCoords(int x, int y) { confirmAfterSaveX = x; confirmAfterSaveY = y; }

    public int getBackToGameX()     { return backToGameX; }
    public int getBackToGameY()     { return backToGameY; }
    public void setBackToGameCoords(int x, int y) { backToGameX = x; backToGameY = y; }

    public int getNewLobbyX()       { return newLobbyX; }
    public int getNewLobbyY()       { return newLobbyY; }
    public void setNewLobbyCoords(int x, int y) { newLobbyX = x; newLobbyY = y; }

    public int getMainLobbyX()      { return mainLobbyX; }
    public int getMainLobbyY()      { return mainLobbyY; }
    public void setMainLobbyCoords(int x, int y) { mainLobbyX = x; mainLobbyY = y; }

    public int getInventoryX()      { return inventoryX; }
    public int getInventoryY()      { return inventoryY; }
    public void setInventoryCoords(int x, int y) { inventoryX = x; inventoryY = y; }

    // ========== GETTERS / SETTERS ==========

    public String getProfileName()  { return profileName; }
    public void setProfileName(String v) { profileName = v; }

    public String getGameName()     { return gameName; }
    public void setGameName(String v) { gameName = v; }

    public String getExePath()      { return exePath; }
    public void setExePath(String v) { exePath = v; }

    public String getUsername()     { return username; }
    public void setUsername(String v) { username = v; }

    public String getPasswordEnc()  { return passwordEnc; }
    public void setPasswordEnc(String v) { passwordEnc = v; }

    public String getServerName()   { return serverName; }
    public void setServerName(String v) { serverName = v; }

    public int getChannelNumber()   { return channelNumber; }
    public void setChannelNumber(int v) { channelNumber = v; }

    public int getUsernameX()       { return usernameX; }
    public int getUsernameY()       { return usernameY; }
    public void setUsernameCoords(int x, int y) { usernameX = x; usernameY = y; }

    public int getPasswordX()       { return passwordX; }
    public int getPasswordY()       { return passwordY; }
    public void setPasswordCoords(int x, int y) { passwordX = x; passwordY = y; }

    public int getLoginBtnX()       { return loginBtnX; }
    public int getLoginBtnY()       { return loginBtnY; }
    public void setLoginBtnCoords(int x, int y) { loginBtnX = x; loginBtnY = y; }

    public int getServerX()         { return serverX; }
    public int getServerY()         { return serverY; }
    public void setServerCoords(int x, int y) { serverX = x; serverY = y; }

    public int getEnterGameX()      { return enterGameX; }
    public int getEnterGameY()      { return enterGameY; }
    public void setEnterGameCoords(int x, int y) { enterGameX = x; enterGameY = y; }

    public int getLoginDetectX()    { return loginDetectX; }
    public int getLoginDetectY()    { return loginDetectY; }
    public String getLoginDetectHex() { return loginDetectHex; }
    public void setLoginDetect(int x, int y, String hex) {
        loginDetectX = x; loginDetectY = y; loginDetectHex = hex;
    }

    public int getServerDetectX()   { return serverDetectX; }
    public int getServerDetectY()   { return serverDetectY; }
    public String getServerDetectHex() { return serverDetectHex; }
    public void setServerDetect(int x, int y, String hex) {
        serverDetectX = x; serverDetectY = y; serverDetectHex = hex;
    }

    public long getStepDelayMs()    { return stepDelayMs; }
    public void setStepDelayMs(long v) { stepDelayMs = v; }

    public long getWaitTimeoutMs()  { return waitTimeoutMs; }
    public void setWaitTimeoutMs(long v) { waitTimeoutMs = v; }

    public long getLaunchWaitMs()   { return launchWaitMs; }
    public void setLaunchWaitMs(long v) { launchWaitMs = v; }

    public boolean isAutoReconnect()  { return autoReconnect; }
    public void setAutoReconnect(boolean v) { autoReconnect = v; }

    public boolean isRunAsAdmin()   { return runAsAdmin; }
    public void setRunAsAdmin(boolean v) { runAsAdmin = v; }

    public int getLoginRetryCount()      { return loginRetryCount; }
    public void setLoginRetryCount(int v) { loginRetryCount = v; }

    public long getLoginRetryDelayMs()   { return loginRetryDelayMs; }
    public void setLoginRetryDelayMs(long v) { loginRetryDelayMs = v; }

    @Override
    public String toString() { return profileName; }
}
