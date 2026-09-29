package ru.sber.transport.trip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import ru.sber.transport.utils.collections.MapUtils;

/**
 * Основной класс приложения.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableWebSocket
@EnableAsync
@EnableScheduling
@Import(MapUtils.class)
public class TripApplication {

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы запуска.
     */
    public static void main(String... args) {
        SpringApplication.run(TripApplication.class, args);
    }

}
