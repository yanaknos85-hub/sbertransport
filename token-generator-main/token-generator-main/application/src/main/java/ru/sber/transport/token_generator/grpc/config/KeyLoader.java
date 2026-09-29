package ru.sber.transport.token_generator.grpc.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;

@Component
@Getter
class KeyLoader {

    private final RSAPrivateKey privateKey;

    KeyLoader(
            @Value("${jwk.private.path}") String privatePath,
            KeyReader keyReader
    ) throws IOException, InvalidKeySpecException {
        privateKey = keyReader.readKey(privatePath, RSAPrivateKey.class);
    }

}
