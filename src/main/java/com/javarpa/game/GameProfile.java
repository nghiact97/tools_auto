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
    private String serverName  = "1";         // tên / index server

    // ========== TOẠ ĐỘ UI ==========
    private int usernameX  = 1163, usernameY  = 735;
    private int passwordX  = 1152, passwordY  = 768;
    private int loginBtnX  = 1376, loginBtnY  = 764;
    private int serverX    = 0, serverY    = 0;
    private int enterGameX = 960, enterGameY = 205;

    // ========== PIXEL DETECT ==========
    // Pixel dùng để detect màn hình đăng nhập đã load xong
    private int loginDetectX     = 0, loginDetectY     = 0;
    private String loginDetectHex = "#FFFFFF";  // màu hex mong đợi

    // Pixel dùng để detect màn hình chọn server đã load xong
    private int serverDetectX     = 0, serverDetectY     = 0;
    private String serverDetectHex = "#FFFFFF";

    // ========== CÀI ĐẶT BOT ==========
    private long stepDelayMs    = 800;    // độ trễ giữa từng bước (ms)
    private long waitTimeoutMs  = 15000;  // thời gian chờ tối đa (ms)
    private long launchWaitMs   = 8000;   // chờ sau khi mở exe (ms) — đủ thời gian launcher load
    private boolean autoReconnect = false; // tự động re-login khi disconnect
    private boolean runAsAdmin    = false; // mở game bằng quyền Admin

    // ========== DEFAULT VALUES (Crossfire) ==========
    private static final int DEF_USERNAME_X = 1163, DEF_USERNAME_Y = 735;
    private static final int DEF_PASSWORD_X = 1152, DEF_PASSWORD_Y = 768;
    private static final int DEF_LOGINBTN_X = 1376, DEF_LOGINBTN_Y = 764;
    private static final int DEF_ENTERGAME_X = 960, DEF_ENTERGAME_Y = 205;

    /**
     * Áp dụng tọa độ mặc định cho các field = 0.
     * Gọi sau khi Gson deserialize từ JSON để fill giá trị thiếu.
     */
    public void applyDefaults() {
        if (usernameX  == 0 && usernameY  == 0) { usernameX  = DEF_USERNAME_X;  usernameY  = DEF_USERNAME_Y; }
        if (passwordX  == 0 && passwordY  == 0) { passwordX  = DEF_PASSWORD_X;  passwordY  = DEF_PASSWORD_Y; }
        if (loginBtnX  == 0 && loginBtnY  == 0) { loginBtnX  = DEF_LOGINBTN_X;  loginBtnY  = DEF_LOGINBTN_Y; }
        if (enterGameX == 0 && enterGameY == 0) { enterGameX = DEF_ENTERGAME_X; enterGameY = DEF_ENTERGAME_Y; }
    }

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

    @Override
    public String toString() { return profileName; }
}
