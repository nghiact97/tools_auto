package com.javarpa.macro;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelListener;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Records mouse and keyboard actions into a MacroScript.
 */
public class MacroRecorder implements NativeKeyListener, NativeMouseInputListener, NativeMouseWheelListener {

    private MacroScript currentScript;
    private boolean recording = false;
    private long lastEventTime = 0;
    private boolean recordDelays = true;

    public MacroRecorder() {}

    /**
     * Start recording a new macro.
     */
    public void start(String macroName) {
        currentScript = new MacroScript(macroName);
        recording = true;
        lastEventTime = System.currentTimeMillis();

        try {
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.WARNING);
            logger.setUseParentHandlers(false);

            if (!GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.registerNativeHook();
            }
            GlobalScreen.addNativeKeyListener(this);
            GlobalScreen.addNativeMouseListener(this);
            GlobalScreen.addNativeMouseMotionListener(this);
            GlobalScreen.addNativeMouseWheelListener(this);
        } catch (NativeHookException e) {
            System.err.println("MacroRecorder hook failed: " + e.getMessage());
        }
    }

    /**
     * Stop recording and return the completed MacroScript.
     */
    public MacroScript stop() {
        recording = false;
        try {
            GlobalScreen.removeNativeKeyListener(this);
            GlobalScreen.removeNativeMouseListener(this);
            GlobalScreen.removeNativeMouseMotionListener(this);
            GlobalScreen.removeNativeMouseWheelListener(this);
        } catch (Exception e) {
            System.err.println("Error removing listeners: " + e.getMessage());
        }
        return currentScript;
    }

    public boolean isRecording() {
        return recording;
    }

    public MacroScript getCurrentScript() {
        return currentScript;
    }

    // === Private helpers ===

    private void addDelay() {
        if (recordDelays) {
            long now = System.currentTimeMillis();
            long elapsed = now - lastEventTime;
            if (elapsed > 50 && elapsed < 10000) {
                currentScript.addAction(MacroAction.delay(elapsed));
            }
            lastEventTime = now;
        }
    }

    // === NativeMouseInputListener ===

    @Override
    public void nativeMouseClicked(NativeMouseEvent e) {
        if (!recording) return;
        addDelay();
        if (e.getButton() == NativeMouseEvent.BUTTON1) {
            if (e.getClickCount() == 2) {
                currentScript.addAction(MacroAction.mouseDoubleClick(e.getX(), e.getY()));
            } else {
                currentScript.addAction(MacroAction.mouseClick(e.getX(), e.getY()));
            }
        } else if (e.getButton() == NativeMouseEvent.BUTTON2) {
            currentScript.addAction(MacroAction.mouseRightClick(e.getX(), e.getY()));
        }
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {}

    @Override
    public void nativeMouseReleased(NativeMouseEvent e) {}

    @Override
    public void nativeMouseMoved(NativeMouseEvent e) {}

    @Override
    public void nativeMouseDragged(NativeMouseEvent e) {}

    // === NativeMouseWheelListener ===

    @Override
    public void nativeMouseWheelMoved(NativeMouseWheelEvent e) {
        if (!recording) return;
        addDelay();
        currentScript.addAction(MacroAction.mouseScroll(e.getWheelRotation()));
    }

    // === NativeKeyListener ===

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (!recording) return;
        addDelay();
        currentScript.addAction(MacroAction.keyPress(e.getKeyCode()));
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {}

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {}

    public void setRecordDelays(boolean recordDelays) {
        this.recordDelays = recordDelays;
    }
}
