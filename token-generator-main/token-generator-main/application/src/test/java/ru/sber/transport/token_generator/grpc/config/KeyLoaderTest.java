package ru.sber.transport.token_generator.grpc.config;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_token_generator")
@DisplayName("Проверка загрузчика ключей")
class KeyLoaderTest {

    @Test
    @DisplayName("Проверка ключей")
    void test_load() throws IOException, InvalidKeySpecException {
        var reader = mock(KeyReader.class);
        var privateKey = mock(RSAPrivateKey.class);

        when(reader.readKey("private", RSAPrivateKey.class)).thenReturn(privateKey);

        var loader = new KeyLoader("private", reader);

        assertThat(loader.getPrivateKey()).isNotNull().isEqualTo(privateKey);
    }

}