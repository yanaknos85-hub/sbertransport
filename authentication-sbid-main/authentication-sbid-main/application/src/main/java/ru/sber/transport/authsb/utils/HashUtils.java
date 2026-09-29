package ru.sber.transport.authsb.utils;

import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.authsb.exceptions.HashException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@Slf4j
public class HashUtils {

    private HashUtils() {
        throw new IllegalStateException("Utility class");
    }

    // Для тестов — можно переопределить
    public static String generateRandomString() {
        return UUID.randomUUID().toString();
    }

    public static String hashSha256(String input) {
        try {
            log.debug("Хеширование строки: {}", input);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (Exception e) {
            throw new HashException(e.getMessage());
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
