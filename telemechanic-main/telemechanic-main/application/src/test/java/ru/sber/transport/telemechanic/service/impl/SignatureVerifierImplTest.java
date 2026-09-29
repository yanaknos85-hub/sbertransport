package ru.sber.transport.telemechanic.service.impl;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.sber.transport.telemechanic.exception.SignatureNotValidException;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Проверка модуля подписания файлов")
class SignatureVerifierImplTest {
    private static final SignatureVerifierImpl verifier = new SignatureVerifierImpl();

    @BeforeAll
    static void init() {
        verifier.initProvider();
    }

    @Test
    @SneakyThrows
    void signatureShouldBeValid() {
        var title = this.getClass().getClassLoader().getResource("ewb/titles/first/title.xml").openStream().readAllBytes();
        var signature = this.getClass().getClassLoader().getResource("ewb/titles/first/signature.bin").openStream().readAllBytes();
        var base64EncodedSignature = new String(signature);
        assertDoesNotThrow(() -> verifier.verify(title, base64EncodedSignature, "Скакун Алексей"));
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource
    void signatureShouldBeNotValid(String signaturePath, String fullName, String errorMessage) {
        var title = this.getClass().getClassLoader().getResource("ewb/titles/first/title.xml").openStream().readAllBytes();
        var signature = this.getClass().getClassLoader().getResource(signaturePath).openStream().readAllBytes();
        var base64EncodedSignature = new String(signature);
        assertThatThrownBy(() -> verifier.verify(title, base64EncodedSignature, fullName))
                .isInstanceOf(SignatureNotValidException.class)
                .hasMessage(errorMessage);
    }

    static Stream<Arguments> signatureShouldBeNotValid() {
        return Stream.of(
                Arguments.of(
                        "ewb/titles/first/wrong_signature.bin",
                        "Скакун Алексей",
                        "При подписании использован не валидный сертификат"
                ),
                Arguments.of(
                        "ewb/titles/first/signature.bin",
                        "Скакун Алексей Анатольевич",
                        "Подпись не принадлежит текущему пользователю. Убедитесь пожалуйста, что подписываете своей подписью"
                ),
                Arguments.of(
                        "ewb/titles/first/damaged_signature.bin",
                        "Скакун Алексей",
                        "При подписании использован не валидный сертификат"
                )
        );
    }
}
