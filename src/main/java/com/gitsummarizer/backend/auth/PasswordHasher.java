package com.gitsummarizer.backend.auth;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 310_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private final SecureRandom secureRandom = new SecureRandom();

    public String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        byte[] derived = derive(password, salt, ITERATIONS);
        return "pbkdf2-sha256$" + ITERATIONS + "$"
                + Base64.getUrlEncoder().withoutPadding().encodeToString(salt) + "$"
                + Base64.getUrlEncoder().withoutPadding().encodeToString(derived);
    }

    public boolean matches(String password, String encoded) {
        if (password == null || encoded == null) {
            return false;
        }
        try {
            String[] parts = encoded.split("\\$", -1);
            if (parts.length != 4 || !parts[0].equals("pbkdf2-sha256")) {
                return false;
            }
            int iterations = Integer.parseInt(parts[1]);
            if (iterations < 100_000 || iterations > 2_000_000) {
                return false;
            }
            byte[] salt = Base64.getUrlDecoder().decode(parts[2]);
            byte[] expected = Base64.getUrlDecoder().decode(parts[3]);
            return MessageDigest.isEqual(expected, derive(password, salt, iterations));
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (Exception exception) {
            throw new IllegalStateException("Password hashing is unavailable.", exception);
        } finally {
            spec.clearPassword();
        }
    }
}
