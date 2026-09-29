package ru.sber.transport.authsb.jwt;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.transport.authsb.exceptions.BadResponseException;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class GostJwtDecoderTest {

    private GostJwtDecoder decoder;

    @BeforeEach
    void setUp() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        this.decoder = new GostJwtDecoder();
    }

    @Test
    @DisplayName("Невалидный токен")
    void test2() throws Exception {
        String header = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";
        String payload = "{\"sub\":\"1234567890\",\"iat\":1516239022}";
        String signature = "fake-signature";

        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(header.getBytes(StandardCharsets.UTF_8))
                + "."
                + Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8))
                + "."
                + signature;

        BadResponseException exception = assertThrows(BadResponseException.class, () -> decoder.decode(token));
        assertTrue(exception.getMessage().contains("Не удалось декодировать JWT"));
    }

    @Test
    @DisplayName("Валидный токен")
    void test1() throws Exception {
        String header = "{\"alg\":\"gost34.10-2012\",\"typ\":\"JWT\"}";
        String payload = "{\"exp\":" + (System.currentTimeMillis() / 1000 + 3600) + "}"; // 1 час назад

        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(header.getBytes(StandardCharsets.UTF_8))
                + "."
                + Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8))
                + ".fake-signature";

        assertDoesNotThrow(() -> decoder.decode(token));
    }

    @Test
    @DisplayName("Невалидный токен")
    void test() {
        String invalidToken = "invalid.token.format";

        BadResponseException exception = assertThrows(BadResponseException.class, () -> decoder.decode(invalidToken));
        assertTrue(exception.getMessage().contains("Не удалось декодировать JWT"));
    }

    @Test
    @DisplayName("Нулевой токен")
    void test4() {
        assertThrows(Exception.class, () -> decoder.decode(null));
    }
}