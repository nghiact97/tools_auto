package com.javarpa.task;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for an automated task (sequence of steps).
 * Java 11 compatible (no records, no switch expressions).
 */
public class TaskConfig {

    public interface TaskStep {
        void execute() throws Exception;
        String describe();
    }

    // === Built-in step implementations ===

    public static class ClickStep implements TaskStep {
        private final int x, y;
        public ClickStep(int x, int y) { this.x = x; this.y = y; }
        public void execute() { com.javarpa.core.RobotActions.click(x, y); }
        public String describe() { return "Click(" + x + ", " + y + ")"; }
    }

    public static class RightClickStep implements TaskStep {
        private final int x, y;
        public RightClickStep(int x, int y) { this.x = x; this.y = y; }
        public void execute() { com.javarpa.core.RobotActions.rightClick(x, y); }
        public String describe() { return "RightClick(" + x + ", " + y + ")"; }
    }

    public static class TypeStep implements TaskStep {
        private final String text;
        public TypeStep(String text) { this.text = text; }
        public void execute() { com.javarpa.core.RobotActions.type(text); }
        public String describe() { return "Type(\"" + text + "\")"; }
    }

    public static class DelayStep implements TaskStep {
        private final int ms;
        public DelayStep(int ms) { this.ms = ms; }
        public void execute() throws InterruptedException { Thread.sleep(ms); }
        public String describe() { return "Delay(" + ms + "ms)"; }
    }

    public static class WaitColorStep implements TaskStep {
        private final int x, y;
        private final Color color;
        private final int tolerance;
        private final long timeout;
        public WaitColorStep(int x, int y, Color color, int tolerance, long timeout) {
            this.x = x; this.y = y; this.color = color;
            this.tolerance = tolerance; this.timeout = timeout;
        }
        public void execute() {
            boolean found = com.javarpa.core.PixelDetector.waitForColor(x, y, color, tolerance, timeout);
            if (!found) System.out.println("WaitColor timeout at (" + x + "," + y + ")");
        }
        public String describe() { return "WaitColor(" + x + "," + y + ")"; }
    }

    public static class ScreenshotStep implements TaskStep {
        private final String path;
        public ScreenshotStep(String path) { this.path = path; }
        public void execute() throws Exception {
            BufferedImage img = com.javarpa.core.ScreenCapture.captureFullScreen();
            com.javarpa.util.ImageUtil.save(img, path);
        }
        public String describe() { return "Screenshot(" + path + ")"; }
    }

    public static class KeyPressStep implements TaskStep {
        private final int keyCode;
        public KeyPressStep(int keyCode) { this.keyCode = keyCode; }
        public void execute() { com.javarpa.core.RobotActions.pressKey(keyCode); }
        public String describe() { return "KeyPress(" + keyCode + ")"; }
    }

    // ===

    private String name = "Task";
    private final List<TaskStep> steps = new ArrayList<>();
    private int repeat = 1;
    private long delayBetweenSteps = 200;
    private long delayBetweenLoops = 1000;

    public TaskConfig setName(String name) { this.name = name; return this; }
    public TaskConfig addStep(TaskStep step) { steps.add(step); return this; }
    public TaskConfig setRepeat(int repeat) { this.repeat = repeat; return this; }
    public TaskConfig setDelay(long ms) { this.delayBetweenSteps = ms; return this; }
    public TaskConfig setLoopDelay(long ms) { this.delayBetweenLoops = ms; return this; }

    public String getName() { return name; }
    public List<TaskStep> getSteps() { return steps; }
    public int getRepeat() { return repeat; }
    public long getDelayBetweenSteps() { return delayBetweenSteps; }
    public long getDelayBetweenLoops() { return delayBetweenLoops; }
}
