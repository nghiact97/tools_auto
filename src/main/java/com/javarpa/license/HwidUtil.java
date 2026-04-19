package com.javarpa.license;

import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Enumeration;

/**
 * Utility to generate a unique Hardware ID (HWID) for this machine.
 * Based on MAC address + OS info.
 */
public class HwidUtil {

    /**
     * Generates a stable Hardware ID for this machine.
     * Format: XXXX-XXXX-XXXX-XXXX
     */
    public static String getHardwareId() {
        try {
            StringBuilder raw = new StringBuilder();

            // Collect MAC addresses
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            while (networks.hasMoreElements()) {
                NetworkInterface ni = networks.nextElement();
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    for (byte b : mac) {
                        raw.append(String.format("%02X", b));
                    }
                }
            }

            // Add OS info
            raw.append(System.getProperty("os.name"));
            raw.append(System.getProperty("user.name"));

            // Hash to get fixed-length HWID
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.toString().getBytes());

            // Format as XXXX-XXXX-XXXX-XXXX
            StringBuilder hwid = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                hwid.append(String.format("%02X", hash[i]));
                if (i == 3 || i == 7 || i == 11) {
                    hwid.append("-");
                }
            }
            return hwid.toString();

        } catch (Exception e) {
            // Fallback to simpler ID
            return "DEMO-0000-0000-0000";
        }
    }
}
