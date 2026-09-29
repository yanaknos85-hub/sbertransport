package ru.sber.transport.qrcodegenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import ru.sber.transport.qrcodegenerator.service.impl.QrCodeGeneratorServiceImpl;

/**
 * Тестовый апп. Библиотека не должна иметь контекста, она должна встраиваться существующий.
 * Этот апп призван создать тестовый контекст для проверки библиотеки.
 */
@SpringBootApplication
@Import({ QrCodeGeneratorServiceImpl.class })
@TestPropertySource("classpath:/application.yml")
public class DoNotStartThis {
    
    public static void main(String[] args) {
        SpringApplication.run(DoNotStartThis.class, args);
    }
    
}
