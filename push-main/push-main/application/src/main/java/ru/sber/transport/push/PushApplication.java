package ru.sber.transport.push;

import org.springframework.boot.SpringApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.sberbank.ditsib.transport.Microservice;

/**
 * Приложение по отправке push-уведомлений.
 */
@Microservice
@EnableTransactionManagement
public class PushApplication {
    
    /**
     * Точка входа в приложение.
     *
     * @param args аргументы.
     */
    public static void main(String... args) {
        SpringApplication.run(PushApplication.class, args);
    }
    
}
