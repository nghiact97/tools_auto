package com.javarpa.core;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * OCR Reader using Tess4J (Tesseract) to extract text from screen regions.
 */
public class OcrReader {

    private static ITesseract tesseract;
    private static String tessDataPath = "tessdata";
    private static String language = "eng+vie";

    static {
        initTesseract();
    }

    private static void initTesseract() {
        tesseract = new Tesseract();
        tesseract.setDatapath(tessDataPath);
        tesseract.setLanguage(language);
        // Page segmentation mode 6: Assume a single uniform block of text
        tesseract.setPageSegMode(6);
    }

    /**
     * Reads text from a screen region.
     */
    public static String readText(Rectangle region) {
        BufferedImage image = ScreenCapture.captureRegion(region);
        return readTextFromImage(image);
    }

    /**
     * Reads text from a screen region defined by coordinates.
     */
    public static String readText(int x, int y, int width, int height) {
        return readText(new Rectangle(x, y, width, height));
    }

    /**
     * Reads text from a BufferedImage.
     */
    public static String readTextFromImage(BufferedImage image) {
        try {
            String result = tesseract.doOCR(image);
            return result != null ? result.trim() : "";
        } catch (TesseractException e) {
            System.err.println("OCR error: " + e.getMessage());
            return "";
        }
    }

    /**
     * Reads text from a file.
     */
    public static String readTextFromFile(File imageFile) {
        try {
            return tesseract.doOCR(imageFile).trim();
        } catch (TesseractException e) {
            System.err.println("OCR error: " + e.getMessage());
            return "";
        }
    }

    /**
     * Reads and parses a number from a screen region.
     */
    public static int readNumber(Rectangle region) {
        String text = readText(region).replaceAll("[^0-9]", "");
        try {
            return text.isEmpty() ? -1 : Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Sets the Tesseract data path.
     */
    public static void setTessDataPath(String path) {
        tessDataPath = path;
        initTesseract();
    }

    /**
     * Sets OCR language (e.g., "eng", "vie", "eng+vie").
     */
    public static void setLanguage(String lang) {
        language = lang;
        initTesseract();
    }

    /**
     * Returns true if Tesseract data is available.
     */
    public static boolean isAvailable() {
        File tessDir = new File(tessDataPath);
        return tessDir.exists() && tessDir.isDirectory();
    }
}
