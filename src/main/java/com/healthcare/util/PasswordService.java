package com.healthcare.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Creates and verifies salted PBKDF2 password hashes. */
public final class PasswordService {
    private static final int ITERATIONS = 210_000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Prevents instances of this password utility class. */
    private PasswordService() {
    }

    /** Hashes a password with a fresh random salt and returns its iteration count and encoded values. */
    public static String hash(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] derived = derive(password.toCharArray(), salt);
        // Store the work factor with each hash so it can be increased without invalidating existing passwords.
        return ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt) + ":"
                + Base64.getEncoder().encodeToString(derived);
    }

    /** Verifies a password against its stored encoded hash; malformed hashes are rejected. */
    public static boolean verify(String password, String encoded) {
        try {
            String[] parts = encoded.split(":");
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            // Constant-time comparison avoids leaking how many hash bytes matched.
            return MessageDigest.isEqual(expected, derive(password.toCharArray(), salt, iterations));
        } catch (RuntimeException exception) {
            return false;
        }
    }

    /** Derives a password key using the current configured work factor. */
    private static byte[] derive(char[] password, byte[] salt) {
        return derive(password, salt, ITERATIONS);
    }

    /** Derives a password key with the supplied salt and PBKDF2 iteration count. */
    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Password hashing is unavailable.", exception);
        } finally {
            spec.clearPassword();
        }
    }
}