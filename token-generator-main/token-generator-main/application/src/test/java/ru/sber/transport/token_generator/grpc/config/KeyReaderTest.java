package ru.sber.transport.token_generator.grpc.config;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_token_generator")
@DisplayName("Проверка чтения ключей")
class KeyReaderTest {

    private final KeyFactory keyFactory = KeyFactory.getInstance("RSA");

    private final KeyReader keyReader = new KeyReader(keyFactory);

    KeyReaderTest() throws NoSuchAlgorithmException {
    }

    @Test
    @DisplayName("Приватный ключ из файла")
    void test_private_file() throws IOException, InvalidKeySpecException {
        var privateKey = keyReader.readKey("src/test/resources/key/jwt.key", RSAPrivateKey.class);

        assertThat(privateKey).isNotNull();
    }
}