package ru.sber.transport.etrn.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для Feign-клиента DispatcherClient.
 */
@Configuration
public class DispatcherClientConfiguration {

    @Bean
    public Logger.Level dispatcherLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
