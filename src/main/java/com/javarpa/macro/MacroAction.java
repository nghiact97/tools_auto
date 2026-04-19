package com.javarpa.macro;

/**
 * Represents a single recordable/replayable action in a macro.
 */
public class MacroAction {

    public enum Type {
        MOUSE_CLICK,
        MOUSE_RIGHT_CLICK,
        MOUSE_DOUBLE_CLICK,
        MOUSE_MOVE,
        MOUSE_DRAG,
        MOUSE_SCROLL,
        KEY_PRESS,
        KEY_TYPE,
        HOTKEY,
        DELAY,
        SCREENSHOT
    }

    private Type type;
    private int x, y;      // For mouse actions
    private int x2, y2;    // For drag
    private int keyCode;
    private String text;    // For type actions
    private int[] keyCodes; // For hotkey
    private long delayMs;
    private long timestamp; // When recorded

    // === Constructors ===

    public static MacroAction mouseClick(int x, int y) {
        MacroAction a = new MacroAction();
        a.type = Type.MOUSE_CLICK;
        a.x = x; a.y = y;
        a.timestamp = System.currentTimeMillis();
        return a;
    }

    public static MacroAction mouseRightClick(int x, int y) {
        MacroAction a = new MacroAction();
        a.type = Type.MOUSE_RIGHT_CLICK;
        a.x = x; a.y = y;
        a.timestamp = System.currentTimeMillis();
        return a;
    }

    public static MacroAction mouseDoubleClick(int x, int y) {
        MacroAction a = new MacroAction();
        a.type = Type.MOUSE_DOUBLE_CLICK;
        a.x = x; a.y = y;
        a.timestamp = System.currentTimeMillis();
        return a;
    }

    public static MacroAction mouseMove(int x, int y) {
        MacroAction a = new MacroAction();
        a.type = Type.MOUSE_MOVE;
        a.x = x; a.y = y;
        a.timestamp = System.currentTimeMillis();
        return a;
    }

    public static MacroAction mouseDrag(int x1, int y1, int x2, int y2) {
        MacroAction a = new MacroAction();
        a.type = Type.MOUSE_DRAG;
        a.x = x1; a.y = y1; a.x2 = x2; a.y2 = y2;
        a.timestamp = System.currentTimeMillis();
        return a;
    }

    public static MacroAction mouseScroll(int amount) {
        MacroAction a = new MacroAction();
        a.type = Type.MOUSE_SCROLL;
        a.keyCode = amount;
        a.timestamp = System.currentTimeMillis();
        return a;
    }

    public static MacroAction keyPress(int keyCode) {
        MacroAction a = new MacroAction();
        a.type = Type.KEY_PRESS;
        a.keyCode = keyCode;
        a.timestamp = System.currentTimeMillis();
        return a;
    }

    public static MacroAction keyType(String text) {
        MacroAction a = new MacroAction();
        a.type = Type.KEY_TYPE;
        a.text = text;
        a.timestamp = System.currentTimeMillis();
        return a;
    }

    public static MacroAction hotkey(int... keyCodes) {
        MacroAction a = new MacroAction();
        a.type = Type.HOTKEY;
        a.keyCodes = keyCodes;
        a.timestamp = System.currentTimeMillis();
        return a;
    }

    public static MacroAction delay(long ms) {
        MacroAction a = new MacroAction();
        a.type = Type.DELAY;
        a.delayMs = ms;
        a.timestamp = System.currentTimeMillis();
        return a;
    }

    public static MacroAction screenshot(String path) {
        MacroAction a = new MacroAction();
        a.type = Type.SCREENSHOT;
        a.text = path;
        a.timestamp = System.currentTimeMillis();
        return a;
    }

    // === Getters ===
    public Type getType() { return type; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getX2() { return x2; }
    public int getY2() { return y2; }
    public int getKeyCode() { return keyCode; }
    public String getText() { return text; }
    public int[] getKeyCodes() { return keyCodes; }
    public long getDelayMs() { return delayMs; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        if (type == Type.MOUSE_CLICK) return "Click(" + x + "," + y + ")";
        if (type == Type.MOUSE_RIGHT_CLICK) return "RightClick(" + x + "," + y + ")";
        if (type == Type.MOUSE_DOUBLE_CLICK) return "DoubleClick(" + x + "," + y + ")";
        if (type == Type.MOUSE_MOVE) return "Move(" + x + "," + y + ")";
        if (type == Type.MOUSE_DRAG) return "Drag(" + x + "," + y + " -> " + x2 + "," + y2 + ")";
        if (type == Type.MOUSE_SCROLL) return "Scroll(" + keyCode + ")";
        if (type == Type.KEY_PRESS) return "KeyPress(" + keyCode + ")";
        if (type == Type.KEY_TYPE) return "Type(\"" + text + "\")";
        if (type == Type.HOTKEY) return "Hotkey(...)";
        if (type == Type.DELAY) return "Delay(" + delayMs + "ms)";
        if (type == Type.SCREENSHOT) return "Screenshot(" + text + ")";
        return "Unknown";
    }
}
