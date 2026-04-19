package com.javarpa.core;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Provides screen capture utilities using java.awt.Robot.
 */
public class ScreenCapture {

    private static Robot robot;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException("Failed to initialize Robot for screen capture", e);
        }
    }

    /**
     * Captures the entire primary screen.
     */
    public static BufferedImage captureFullScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRect = new Rectangle(screenSize);
        return robot.createScreenCapture(screenRect);
    }

    /**
     * Captures a specific rectangular region of the screen.
     */
    public static BufferedImage captureRegion(int x, int y, int width, int height) {
        Rectangle region = new Rectangle(x, y, width, height);
        return robot.createScreenCapture(region);
    }

    /**
     * Captures a specific rectangular region of the screen.
     */
    public static BufferedImage captureRegion(Rectangle region) {
        return robot.createScreenCapture(region);
    }

    /**
     * Returns the total size of all screens combined.
     */
    public static Dimension getScreenSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    /**
     * Gets the Robot instance (shared).
     */
    public static Robot getRobot() {
        return robot;
    }
}
