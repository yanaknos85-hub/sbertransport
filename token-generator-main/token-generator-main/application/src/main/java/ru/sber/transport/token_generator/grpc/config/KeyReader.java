package ru.sber.transport.token_generator.grpc.config;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@RequiredArgsConstructor
@Component
class KeyReader {

    private final KeyFactory factory;

    @SuppressWarnings("unchecked")
    <T> T readKey(String fileName, Class<T> keyType) throws IOException, InvalidKeySpecException {
        var rawKey = Files.readString(Path.of(fileName));
        rawKey = rawKey
            .replace("\n", "")
            .replace("\r", "")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----BEGIN PRIVATE KEY-----", "")
        ;
        var decodedKey = Base64.getDecoder().decode(rawKey);
        if (PublicKey.class.isAssignableFrom(keyType)) {
            return (T) factory.generatePublic(new X509EncodedKeySpec(decodedKey));
        }
        if (PrivateKey.class.isAssignableFrom(keyType)) {
            return (T) factory.generatePrivate(new PKCS8EncodedKeySpec(decodedKey));
        }
        throw new UnsupportedOperationException("Unknown type of key %s".formatted(keyType.getCanonicalName()));
    }

}
