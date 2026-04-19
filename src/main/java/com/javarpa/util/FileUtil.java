package com.javarpa.util;

import java.io.*;
import java.nio.file.*;

/**
 * File I/O utility methods.
 */
public class FileUtil {

    /**
     * Read text content from a file.
     */
    public static String readText(File file) throws IOException {
        return Files.readString(file.toPath());
    }

    /**
     * Write text content to a file (creates parent dirs as needed).
     */
    public static void writeText(File file, String content) throws IOException {
        file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), content);
    }

    /**
     * Ensure a directory exists, creating it if not.
     */
    public static void ensureDir(String path) {
        new File(path).mkdirs();
    }

    /**
     * Get the application data directory (OS-specific).
     */
    public static String getAppDataDir() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return System.getenv("APPDATA") + File.separator + "JavaRPA";
        } else if (os.contains("mac")) {
            return System.getProperty("user.home") + "/Library/Application Support/JavaRPA";
        } else {
            return System.getProperty("user.home") + "/.javarpa";
        }
    }
}
