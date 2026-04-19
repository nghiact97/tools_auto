package com.javarpa.core;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global hotkey manager using JNativeHook.
 * Handles system-wide hotkeys even when the app is not focused.
 */
public class GlobalHotkey implements NativeKeyListener {

    private static GlobalHotkey instance;
    private final Map<Integer, Runnable> keyBindings = new HashMap<>();
    private boolean registered = false;

    private GlobalHotkey() {}

    public static GlobalHotkey getInstance() {
        if (instance == null) {
            instance = new GlobalHotkey();
        }
        return instance;
    }

    /**
     * Registers a hotkey with a callback action.
     * @param keyCode JNativeHook keycode (e.g., NativeKeyEvent.VC_F6)
     * @param action  Runnable to execute when key is pressed
     */
    public void register(int keyCode, Runnable action) {
        keyBindings.put(keyCode, action);
        if (!registered) {
            startHook();
        }
    }

    /**
     * Unregisters a hotkey.
     */
    public void unregister(int keyCode) {
        keyBindings.remove(keyCode);
    }

    /**
     * Starts the JNativeHook global listener.
     */
    public void startHook() {
        try {
            // Suppress JNativeHook verbose logging
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.WARNING);
            logger.setUseParentHandlers(false);

            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
            registered = true;
        } catch (NativeHookException e) {
            System.err.println("Failed to register global hotkey hook: " + e.getMessage());
        }
    }

    /**
     * Stops the JNativeHook global listener.
     */
    public void stopHook() {
        if (registered) {
            try {
                GlobalScreen.removeNativeKeyListener(this);
                GlobalScreen.unregisterNativeHook();
                registered = false;
            } catch (NativeHookException e) {
                System.err.println("Failed to unregister native hook: " + e.getMessage());
            }
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        Runnable action = keyBindings.get(e.getKeyCode());
        if (action != null) {
            new Thread(action).start();
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {}

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {}

    public boolean isRegistered() {
        return registered;
    }
}
