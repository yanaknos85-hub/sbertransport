package ru.sber.transport.authsb.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.security.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Map;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.sber.transport.authsb.exceptions.BadResponseException;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class GostJwtDecoder {

    public GostJwtDecoder() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public Map<String, Object> decode(String token)  {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.error("Токен должен содержать 3 части (header.payload.signature), найдено: {}", parts.length);
                throw new BadResponseException("Invalid JWT token format");
            }

            String padded = parts[0] + "==".substring(0, (4 - parts[0].length() % 4) % 4); // Добавляем padding
            String headerJson = new String(java.util.Base64.getUrlDecoder().decode(padded));

            // Декодируем header
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> headerClaims = mapper.readValue(headerJson, Map.class);

            // Проверка алгоритма
            String alg = (String) headerClaims.get("alg");
            if (!"gost34.10-2012".equals(alg)) {
                throw new BadResponseException("Unsupported algorithm: " + alg);
            }

            // Декодируем payload и проверяем exp
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> payloadClaims = mapper.readValue(payloadJson, Map.class);
            if (payloadClaims != null && payloadClaims.get("exp") != null) {
                var exp = (Integer) payloadClaims.get("exp");
                long nowSeconds = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC);
                if (exp != null && nowSeconds >= exp) {
                    throw new BadResponseException("Token expired");
                }
            }
            return payloadClaims;

        } catch (Exception e) {
            log.error("Не удалось декодировать JWT", e);
            throw new BadResponseException("Не удалось декодировать JWT: " + e.getMessage());
        }
    }
}