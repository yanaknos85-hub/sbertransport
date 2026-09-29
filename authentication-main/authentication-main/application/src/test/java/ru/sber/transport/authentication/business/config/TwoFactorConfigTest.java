package ru.sber.transport.authentication.business.config;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import java.security.*;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@DisplayName("Ключи 2-хфакторки")
class TwoFactorConfigTest {

    private final TwoFactorConfig config = new TwoFactorConfig();

    @DisplayName("Проверка ключей")
    @Test
    void test_compareKeys() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        var keyPair = config.twoFactorKeyPair();
        var privateKey = config.twoFactorJwtPrivate(keyPair);
        var publicKey = config.twoFactorPublic(keyPair);

        assertThat(keyPair).isNotNull();
        assertThat(privateKey).isNotNull();
        assertThat(publicKey).isNotNull();

        var content = UUID.randomUUID().toString().getBytes();

        var signature = Signature.getInstance("SHA256withRSA");
        signature.initSign((PrivateKey) privateKey);
        signature.update(content);
        var signatured = signature.sign();

        signature.initVerify((PublicKey) publicKey);
        signature.update(content);

        assertThat(signature.verify(signatured)).isTrue();
    }

}