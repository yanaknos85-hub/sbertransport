package ru.sber.transport.qrcodegenerator.service.impl;

import com.google.zxing.WriterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.transport.qrcodegenerator.DoNotStartThis;
import ru.sber.transport.qrcodegenerator.service.QrCodeGeneratorService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = DoNotStartThis.class)
@DisplayName("Тестирование генератора qr-кода")
class QrCodeGeneratorServiceTest {

    private final QrCodeGeneratorService qrCodeGeneratorService;
    
    @Autowired
    QrCodeGeneratorServiceTest(QrCodeGeneratorService qrCodeGeneratorService) {
        this.qrCodeGeneratorService = qrCodeGeneratorService;
    }
    
    @Test
    @DisplayName("Проверка корректности возвращаемого qr-кода size = 300")
    void shouldGetQrCodeText() throws IOException, WriterException {
        byte[] expected = Files.readAllBytes(Paths.get("./src/test/resources/expected_qr_bytes1.png"));
        byte[] actual = qrCodeGeneratorService.getQrCode("Qwerty1234567890", 300);
        assertArrayEquals(expected, actual);
    }

    @Test
    @DisplayName("Проверка корректности возвращаемого qr-кода size = 400")
    void shouldGetQrCodeAnyText() throws IOException, WriterException {
        byte[] expected = Files.readAllBytes(Paths.get("./src/test/resources/expected_qr_bytes2.png"));
        byte[] actual = qrCodeGeneratorService.getQrCode("SimpleQRcode", 400);
        assertArrayEquals(expected, actual);
    }

    @Test
    @DisplayName("Проверка корректности возвращаемого qr-кода c default size")
    void shouldGetQrCodeDefaultSize() throws IOException, WriterException {
        byte[] expected = Files.readAllBytes(Paths.get("./src/test/resources/expected_qr_bytes1.png"));
        byte[] actual = qrCodeGeneratorService.getQrCodeDefaultSize("Qwerty1234567890");
        assertArrayEquals(expected, actual);
    }

    @Test
    @DisplayName("Проверка исключения при получении пустой строки")
    void shouldThrowExceptionEmptyString() {
        assertThrows(IllegalArgumentException.class, () ->
           qrCodeGeneratorService.getQrCode("", 400));
    }

    @Test
    @DisplayName("Проверка исключения при получении пустой строки")
    void shouldThrowExceptionEmptyStringDefaultSize() {
        assertThrows(IllegalArgumentException.class, () ->
            qrCodeGeneratorService.getQrCodeDefaultSize(""));
    }

    @Test
    @DisplayName("Проверка исключения при получении слишком длинной строки")
    void shouldThrowExceptionTooLongString() {
        String text = "ThisStringMoreThan128char".repeat(6);
        assertThrows(IllegalArgumentException.class, () -> qrCodeGeneratorService.getQrCode(text, 400));
    }

    @Test
    @DisplayName("Проверка исключения при получении слишком длинной строки")
    void shouldThrowExceptionTooLongStringDefaultSize() {
        String text = "ThisStringMoreThan128char".repeat(6);
        assertThrows(IllegalArgumentException.class, () -> qrCodeGeneratorService.getQrCodeDefaultSize(text));
    }
}
