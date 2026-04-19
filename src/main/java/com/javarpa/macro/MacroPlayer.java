package com.javarpa.macro;

import com.javarpa.core.RobotActions;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Plays back a MacroScript by executing each action in sequence.
 */
public class MacroPlayer {

    private MacroScript currentScript;
    private volatile boolean playing = false;
    private volatile boolean paused = false;
    private double speedMultiplier = 1.0;
    private Thread playThread;

    /** Load a script for playback. */
    public void load(MacroScript script) {
        this.currentScript = script;
    }

    /** Start playback of the loaded script, once. */
    public void play() {
        playLoop(1);
    }

    /** Play the script n times. Use -1 for infinite loop. */
    public void playLoop(int times) {
        if (currentScript == null) throw new IllegalStateException("No script loaded");
        playing = true;
        paused = false;

        playThread = new Thread(() -> {
            int count = 0;
            while (playing && (times == -1 || count < times)) {
                executeScript(currentScript);
                count++;
            }
            playing = false;
        });
        playThread.setDaemon(true);
        playThread.start();
    }

    /** Stop playback. */
    public void stop() {
        playing = false;
        paused = false;
        if (playThread != null) {
            playThread.interrupt();
        }
    }

    /** Pause/resume playback. */
    public void togglePause() {
        paused = !paused;
    }

    /** Set playback speed (1.0 = normal, 2.0 = 2x speed). */
    public void setSpeed(double multiplier) {
        this.speedMultiplier = multiplier;
    }

    public boolean isPlaying() { return playing; }
    public boolean isPaused() { return paused; }

    /** Execute all actions in the script. */
    private void executeScript(MacroScript script) {
        List<MacroAction> actions = script.getActions();
        System.out.println("[MacroPlayer] ▶ Bat dau phat lai: " + script.getName()
                + " (" + actions.size() + " hanh dong)");

        for (int i = 0; i < actions.size(); i++) {
            if (!playing) break;

            // Wait while paused
            while (paused && playing) {
                try { Thread.sleep(100); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            MacroAction action = actions.get(i);
            System.out.printf("[MacroPlayer] [%3d/%3d] %s%n",
                    i + 1, actions.size(), action.toString());

            executeAction(action);
        }
        System.out.println("[MacroPlayer] ✓ Phat lai hoan thanh.");
    }

    /** Execute a single MacroAction. */
    private void executeAction(MacroAction action) {
        try {
            MacroAction.Type type = action.getType();
            if (type == MacroAction.Type.MOUSE_CLICK) {
                RobotActions.click(action.getX(), action.getY());
            } else if (type == MacroAction.Type.MOUSE_RIGHT_CLICK) {
                RobotActions.rightClick(action.getX(), action.getY());
            } else if (type == MacroAction.Type.MOUSE_DOUBLE_CLICK) {
                RobotActions.doubleClick(action.getX(), action.getY());
            } else if (type == MacroAction.Type.MOUSE_MOVE) {
                RobotActions.mouseMove(action.getX(), action.getY());
            } else if (type == MacroAction.Type.MOUSE_DRAG) {
                RobotActions.dragDrop(action.getX(), action.getY(), action.getX2(), action.getY2());
            } else if (type == MacroAction.Type.MOUSE_SCROLL) {
                RobotActions.scroll(action.getKeyCode());
            } else if (type == MacroAction.Type.KEY_PRESS) {
                RobotActions.pressKey(action.getKeyCode());
            } else if (type == MacroAction.Type.KEY_TYPE) {
                RobotActions.type(action.getText());
            } else if (type == MacroAction.Type.HOTKEY) {
                if (action.getKeyCodes() != null) {
                    RobotActions.hotkey(action.getKeyCodes());
                }
            } else if (type == MacroAction.Type.DELAY) {
                long delay = (long) (action.getDelayMs() / speedMultiplier);
                Thread.sleep(Math.max(delay, 10));
            }
            // SCREENSHOT handled externally
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
