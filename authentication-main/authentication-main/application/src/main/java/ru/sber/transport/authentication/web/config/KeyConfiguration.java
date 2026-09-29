package ru.sber.transport.authentication.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

@Slf4j
@Configuration
public class KeyConfiguration {

    @Bean
    KeyFactory keyFactory() throws NoSuchAlgorithmException {
        return KeyFactory.getInstance("RSA");
    }

    @Bean("jwtPrivateKey")
    Key jwtPrivateKey(KeyFactory keyFactory, @Value("${jwt.private.path}") String path) throws InvalidKeySpecException, IOException {
        if (path.contains("classpath:")) {
            path = path.replace("classpath:", "");
            var resource = getClass().getClassLoader().getResource(".");
            path = "%s/%s".formatted(Objects.requireNonNull(resource).getFile(), path);
        }
        var decoded = Files.readString(Path.of(path))
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replaceAll(System.lineSeparator(), "")
            .replace("-----END PRIVATE KEY-----", "");
        var privateKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(decoded));
        return keyFactory.generatePrivate(privateKeySpec);
    }
}
