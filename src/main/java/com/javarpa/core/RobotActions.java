package com.javarpa.core;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Provides mouse and keyboard automation actions using java.awt.Robot.
 */
public class RobotActions {

    private static final Robot robot = ScreenCapture.getRobot();
    private static int defaultDelay = 50; // ms between actions

    // ===================== MOUSE ACTIONS =====================

    /** Left-click at (x, y). */
    public static void click(int x, int y) {
        mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        delay(defaultDelay);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        delay(defaultDelay);
    }

    /** Right-click at (x, y). */
    public static void rightClick(int x, int y) {
        mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        delay(defaultDelay);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        delay(defaultDelay);
    }

    /** Double left-click at (x, y). */
    public static void doubleClick(int x, int y) {
        click(x, y);
        delay(50);
        click(x, y);
    }

    /** Move mouse to (x, y). */
    public static void mouseMove(int x, int y) {
        robot.mouseMove(x, y);
        delay(20);
    }

    /** Smooth mouse move (glide animation). */
    public static void mouseMoveSmooth(int startX, int startY, int endX, int endY, int steps) {
        for (int i = 0; i <= steps; i++) {
            int x = startX + (endX - startX) * i / steps;
            int y = startY + (endY - startY) * i / steps;
            robot.mouseMove(x, y);
            delay(10);
        }
    }

    /** Click and drag from (x1, y1) to (x2, y2). */
    public static void dragDrop(int x1, int y1, int x2, int y2) {
        mouseMove(x1, y1);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        delay(100);
        mouseMoveSmooth(x1, y1, x2, y2, 20);
        delay(100);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        delay(defaultDelay);
    }

    /** Scroll the mouse wheel. Positive = down, negative = up. */
    public static void scroll(int amount) {
        robot.mouseWheel(amount);
        delay(defaultDelay);
    }

    // ===================== KEYBOARD ACTIONS =====================

    /** Type a string of text character by character. */
    public static void type(String text) {
        for (char c : text.toCharArray()) {
            typeChar(c);
        }
    }

    /** Press and release a single key by KeyEvent code. */
    public static void pressKey(int keyCode) {
        robot.keyPress(keyCode);
        delay(defaultDelay);
        robot.keyRelease(keyCode);
        delay(defaultDelay);
    }

    /** Press a hotkey combination (e.g., CTRL+C). */
    public static void hotkey(int... keyCodes) {
        for (int key : keyCodes) {
            robot.keyPress(key);
            delay(20);
        }
        delay(defaultDelay);
        for (int i = keyCodes.length - 1; i >= 0; i--) {
            robot.keyRelease(keyCodes[i]);
            delay(20);
        }
        delay(defaultDelay);
    }

    /** Press CTRL+C (copy). */
    public static void copy() {
        hotkey(KeyEvent.VK_CONTROL, KeyEvent.VK_C);
    }

    /** Press CTRL+V (paste). */
    public static void paste() {
        hotkey(KeyEvent.VK_CONTROL, KeyEvent.VK_V);
    }

    /** Press CTRL+A (select all). */
    public static void selectAll() {
        hotkey(KeyEvent.VK_CONTROL, KeyEvent.VK_A);
    }

    /** Press ENTER. */
    public static void enter() {
        pressKey(KeyEvent.VK_ENTER);
    }

    /** Press BACKSPACE n times. */
    public static void backspace(int times) {
        for (int i = 0; i < times; i++) {
            pressKey(KeyEvent.VK_BACK_SPACE);
            delay(20);
        }
    }

    // ===================== UTILITIES =====================

    /** Wait for a given number of milliseconds. */
    public static void delay(int ms) {
        robot.delay(ms);
    }

    /** Set default delay between actions (ms). */
    public static void setDefaultDelay(int ms) {
        defaultDelay = ms;
    }

    /** Type a single character using Robot key events. */
    private static void typeChar(char c) {
        boolean needsShift = Character.isUpperCase(c) || "!@#$%^&*()_+{}|:\"<>?~".indexOf(c) >= 0;
        int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
        if (keyCode == KeyEvent.VK_UNDEFINED) return;

        if (needsShift) {
            robot.keyPress(KeyEvent.VK_SHIFT);
            delay(10);
        }
        robot.keyPress(keyCode);
        delay(20);
        robot.keyRelease(keyCode);
        delay(10);
        if (needsShift) {
            robot.keyRelease(KeyEvent.VK_SHIFT);
            delay(10);
        }
    }
}
