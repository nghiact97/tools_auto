package com.javarpa.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * AES encryption/decryption utility.
 */
public class CryptoUtil {

    private static final String ALGORITHM = "AES";

    /**
     * Encrypt a plaintext string using AES with the given password.
     */
    public static String encrypt(String plaintext, String password) {
        try {
            SecretKeySpec key = makeKey(password);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes());
            return Base64.getUrlEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypt a ciphertext string using AES with the given password.
     */
    public static String decrypt(String ciphertext, String password) {
        try {
            SecretKeySpec key = makeKey(password);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decoded = Base64.getUrlDecoder().decode(ciphertext);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            return null; // Invalid key
        }
    }

    /**
     * Generate a 128-bit AES key from a password string.
     */
    private static SecretKeySpec makeKey(String password) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(password.getBytes());
        key = Arrays.copyOf(key, 16); // 128-bit AES
        return new SecretKeySpec(key, ALGORITHM);
    }
}
