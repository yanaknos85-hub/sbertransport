package ru.sber.transport.request_checks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * Основной класс приложения.
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = "ru.sber.transport")
public class RequestChecksApplication {

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы запуска.
     */
    public static void main(String... args) {
        SpringApplication.run(RequestChecksApplication.class, args);
    }

}
