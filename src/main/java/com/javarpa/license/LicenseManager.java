package com.javarpa.license;

import com.javarpa.util.CryptoUtil;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;

/**
 * Manages license key verification and activation (offline, HWID-based).
 */
public class LicenseManager {

    private static final String LICENSE_FILE = System.getProperty("user.home") + "/.javarpa/license.dat";
    private static final String SECRET_SALT = "JAVARPA_SECRET_2024";

    private static boolean activated = false;
    private static LocalDate expiryDate = null;

    /**
     * Get Hardware ID of this machine.
     */
    public static String getHWID() {
        return HwidUtil.getHardwareId();
    }

    /**
     * Attempt to activate the app with a given license key.
     * @param key The license key string.
     * @return true if activation was successful.
     */
    public static boolean activate(String key) {
        if (key == null || key.isBlank()) return false;

        try {
            String hwid = getHWID();
            LocalDate expiry = verifyKey(key, hwid);
            if (expiry != null && !expiry.isBefore(LocalDate.now())) {
                activated = true;
                expiryDate = expiry;
                saveLicense(key);
                return true;
            }
        } catch (Exception e) {
            System.err.println("License activation error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Check if the app is currently activated (reads saved license).
     */
    public static boolean isActivated() {
        if (activated) return true;

        // Try to load from saved file
        try {
            File licenseFile = new File(LICENSE_FILE);
            if (!licenseFile.exists()) return false;

            String saved = Files.readString(licenseFile.toPath()).trim();
            return activate(saved);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Get remaining days on the license (returns 0 if expired or not activated).
     */
    public static int getDaysRemaining() {
        if (!isActivated() || expiryDate == null) return 0;
        long days = LocalDate.now().until(expiryDate).getDays();
        return (int) Math.max(0, days);
    }

    /**
     * Get expiry date as string.
     */
    public static String getExpiryDateStr() {
        if (expiryDate == null) return "Chưa kích hoạt";
        return expiryDate.toString();
    }

    // === Internal ===

    /**
     * Verify a license key against the current machine's HWID.
     * Key format (decrypted): HWID|YYYY-MM-DD
     */
    private static LocalDate verifyKey(String encryptedKey, String hwid) {
        try {
            String decoded = CryptoUtil.decrypt(encryptedKey, SECRET_SALT + hwid.substring(0, 8));
            if (decoded == null) return null;

            String[] parts = decoded.split("\\|");
            if (parts.length != 2) return null;

            String keyHwid = parts[0];
            if (!keyHwid.equals(hwid)) return null;

            return LocalDate.parse(parts[1]);
        } catch (Exception e) {
            return null;
        }
    }

    private static void saveLicense(String key) {
        try {
            File file = new File(LICENSE_FILE);
            file.getParentFile().mkdirs();
            Files.writeString(file.toPath(), key);
        } catch (IOException e) {
            System.err.println("Failed to save license: " + e.getMessage());
        }
    }

    public static void deactivate() {
        activated = false;
        expiryDate = null;
        new File(LICENSE_FILE).delete();
    }
}
