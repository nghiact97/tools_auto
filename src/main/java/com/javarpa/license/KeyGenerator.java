package com.javarpa.license;

import com.javarpa.util.CryptoUtil;

import java.time.LocalDate;

/**
 * Admin tool for generating license keys.
 * Usage: KeyGenerator.generate(hwid, days)
 */
public class KeyGenerator {

    private static final String SECRET_SALT = "JAVARPA_SECRET_2024";

    /**
     * Generate a license key for a given HWID and number of days.
     * @param hwid  Hardware ID from the target machine
     * @param days  Number of days the license should be valid
     * @return Encrypted license key string
     */
    public static String generate(String hwid, int days) {
        if (hwid == null || hwid.isBlank()) throw new IllegalArgumentException("HWID required");
        if (days <= 0) throw new IllegalArgumentException("Days must be positive");

        LocalDate expiry = LocalDate.now().plusDays(days);
        String payload = hwid + "|" + expiry.toString();
        String encryptionKey = SECRET_SALT + hwid.substring(0, Math.min(8, hwid.length()));

        return CryptoUtil.encrypt(payload, encryptionKey);
    }

    /**
     * Entry point for standalone key generator tool.
     * Args: [hwid] [days]
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: KeyGenerator <HWID> <days>");
            System.out.println("Example: KeyGenerator ABC-123-DEF-456 365");
            return;
        }
        String hwid = args[0];
        int days = Integer.parseInt(args[1]);
        String key = generate(hwid, days);

        System.out.println("=== JavaRPA Key Generator ===");
        System.out.println("HWID   : " + hwid);
        System.out.println("Days   : " + days);
        System.out.println("Expiry : " + LocalDate.now().plusDays(days));
        System.out.println("KEY    : " + key);
    }
}
