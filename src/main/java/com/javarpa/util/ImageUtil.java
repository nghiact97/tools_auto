package com.javarpa.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Image processing utilities.
 */
public class ImageUtil {

    /**
     * Save a BufferedImage to file as PNG.
     */
    public static void save(BufferedImage image, File file) throws IOException {
        file.getParentFile().mkdirs();
        ImageIO.write(image, "PNG", file);
    }

    /**
     * Save to a path string.
     */
    public static void save(BufferedImage image, String path) throws IOException {
        save(image, new File(path));
    }

    /**
     * Load an image from file.
     */
    public static BufferedImage load(File file) throws IOException {
        return ImageIO.read(file);
    }

    /**
     * Scale a BufferedImage to the given dimensions.
     */
    public static BufferedImage scale(BufferedImage original, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, original.getType());
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();
        return scaled;
    }

    /**
     * Convert a Color to its hex string (#RRGGBB).
     */
    public static String colorToHex(Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Parse a hex color string to Color.
     */
    public static Color hexToColor(String hex) {
        return Color.decode(hex);
    }

    /**
     * Create a timestamp-based filename for screenshots.
     */
    public static String screenshotFilename() {
        return "screenshot_" + System.currentTimeMillis() + ".png";
    }
}
