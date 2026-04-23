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
    private String serverName    = "Tân Binh";  // tên máy chủ (Tân Binh, Tự do 1, ...)
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

    // Nút "Chọn kênh" trên home screen
    private int channelBtnX = 0, channelBtnY = 0;
    private static final int DEF_CHANNELBTN_X = 640, DEF_CHANNELBTN_Y = 202;

    // Nút "Vào kênh" sau khi chọn kênh
    private int enterChannelBtnX = 0, enterChannelBtnY = 0;
    private static final int DEF_ENTERCHANNELBTN_X = 1406, DEF_ENTERCHANNELBTN_Y = 909;

    // ========== DANH SÁCH MÁY CHỦ ==========
    /** Tên các máy chủ có trong game. */
    public static final String[] SERVER_NAMES = {
        "Tân Binh", "Tự do 1", "Tự do 2", "Tự do 3", "Tự do 4"
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
        if (channelBtnX == 0 && channelBtnY == 0) { channelBtnX = DEF_CHANNELBTN_X; channelBtnY = DEF_CHANNELBTN_Y; }
        if (enterChannelBtnX == 0 && enterChannelBtnY == 0) {
            enterChannelBtnX = DEF_ENTERCHANNELBTN_X;
            enterChannelBtnY = DEF_ENTERCHANNELBTN_Y;
        }
        if (serverName == null || serverName.isEmpty()) serverName = "Tân Binh";
        if (channelNumber < 1 || channelNumber > MAX_CHANNELS) channelNumber = 1;
    }

    // ========== TỌA ĐỘ MÁY CHỦ (bảng 2 cột) ==========

    /**
     * Trả về tọa độ (x, y) của máy chủ trong bảng Chọn kênh.
     * Layout Crossfire: bảng 2 cột, mỗi cột có danh sách máy chủ.
     *
     * <pre>
     *  Cột trái (x≈900)     | Cột phải (x≈1130)
     *  ─────────────────────|───────────────────
     *  Tân Binh   (y≈286)   | Tự do 1   (y≈286)
     *  Tự do 2    (y≈315)   | Tự do 3   (y≈315)
     *  Tự do 4    (y≈344)   | ...
     * </pre>
     */
    public int[] getServerCoords(String server) {
        if (server == null) return new int[]{0, 0};
        switch (server.trim()) {
            case "Tân Binh":  return new int[]{900, 286};
            case "Tự do 1":   return new int[]{1130, 286};
            case "Tự do 2":   return new int[]{900, 315};
            case "Tự do 3":   return new int[]{1130, 315};
            case "Tự do 4":   return new int[]{900, 344};
            default:           return new int[]{900, 286}; // mặc định Tân Binh
        }
    }

    // ========== TỌA ĐỘ KÊNH (danh sách 6 kênh dọc) ==========

    /**
     * Trả về tọa độ (x, y) của kênh trong danh sách kênh.
     * Layout Crossfire: 6 kênh xếp dọc sau khi chọn máy chủ.
     *
     * <pre>
     *  Máy chủ phổ thông - kênh 1  (y≈290)
     *  Máy chủ phổ thông - kênh 2  (y≈319)
     *  Máy chủ phổ thông - kênh 3  (y≈348)
     *  Máy chủ phổ thông - kênh 4  (y≈377)
     *  Máy chủ phổ thông - kênh 5  (y≈406)
     *  Máy chủ phổ thông - kênh 6  (y≈435)
     * </pre>
     *
     * @param channel số kênh 1-6
     */
    public int[] getChannelRowCoords(int channel) {
        if (channel < 1) channel = 1;
        if (channel > MAX_CHANNELS) channel = MAX_CHANNELS;

        int x = 1088;                    // giữa dòng kênh
        int baseY = 290;                 // kênh 1
        int rowHeight = 29;              // khoảng cách giữa mỗi kênh
        int y = baseY + (channel - 1) * rowHeight;

        return new int[]{x, y};
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
