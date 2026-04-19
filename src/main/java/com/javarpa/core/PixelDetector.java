package com.javarpa.core;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Detects and analyzes pixel colors on the screen.
 */
public class PixelDetector {

    /**
     * Gets the color of a pixel at (x, y) on the screen.
     */
    public static Color getColor(int x, int y) {
        Robot robot = ScreenCapture.getRobot();
        return robot.getPixelColor(x, y);
    }

    /**
     * Finds the first occurrence of a color (within tolerance) on the screen.
     * @return Point where color was found, or null if not found.
     */
    public static Point findColor(Color target, int tolerance) {
        BufferedImage screen = ScreenCapture.captureFullScreen();
        return findColorInImage(screen, target, tolerance, 0, 0);
    }

    /**
     * Finds a color within a specific screen region.
     */
    public static Point findColor(Color target, int tolerance, Rectangle region) {
        BufferedImage image = ScreenCapture.captureRegion(region);
        Point found = findColorInImage(image, target, tolerance, 0, 0);
        if (found != null) {
            found.translate(region.x, region.y);
        }
        return found;
    }

    /**
     * Waits until a specific color appears at a point (x, y).
     * @param timeout Maximum wait time in milliseconds.
     * @return true if color appeared within timeout, false otherwise.
     */
    public static boolean waitForColor(int x, int y, Color target, int tolerance, long timeout) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeout) {
            Color current = getColor(x, y);
            if (colorsMatch(current, target, tolerance)) {
                return true;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * Compares two BufferedImages and returns similarity percentage (0.0 to 1.0).
     */
    public static double compareRegion(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            return 0.0;
        }
        int total = img1.getWidth() * img1.getHeight();
        int matching = 0;
        for (int x = 0; x < img1.getWidth(); x++) {
            for (int y = 0; y < img1.getHeight(); y++) {
                Color c1 = new Color(img1.getRGB(x, y));
                Color c2 = new Color(img2.getRGB(x, y));
                if (colorsMatch(c1, c2, 10)) {
                    matching++;
                }
            }
        }
        return (double) matching / total;
    }

    /**
     * Checks if two colors are within a given tolerance.
     */
    public static boolean colorsMatch(Color c1, Color c2, int tolerance) {
        return Math.abs(c1.getRed() - c2.getRed()) <= tolerance &&
               Math.abs(c1.getGreen() - c2.getGreen()) <= tolerance &&
               Math.abs(c1.getBlue() - c2.getBlue()) <= tolerance;
    }

    /**
     * Helper to find a color within a BufferedImage.
     */
    private static Point findColorInImage(BufferedImage image, Color target, int tolerance, int offsetX, int offsetY) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixel = new Color(image.getRGB(x, y));
                if (colorsMatch(pixel, target, tolerance)) {
                    return new Point(x + offsetX, y + offsetY);
                }
            }
        }
        return null;
    }
}
