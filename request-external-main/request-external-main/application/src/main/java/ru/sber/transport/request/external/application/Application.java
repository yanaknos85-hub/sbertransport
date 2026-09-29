package ru.sber.transport.request.external.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.sber.transport.scripting.ScriptUtils;

/**
 * Основной класс приложения
 */
@SpringBootApplication(scanBasePackages = {"ru.sber.transport.request.external"})
@EnableTransactionManagement
@Import(value = ScriptUtils.class)
public class Application {

    /**
     * Запуск приложения
     *
     * @param args аргументы командной строки
     */
    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

}
