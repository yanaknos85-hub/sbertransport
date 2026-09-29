package ru.sber.transport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.sber.transport.address.application.ApplicationStartedListener;
import ru.sber.transport.utils.collections.MapUtils;

@SpringBootApplication
@EnableDiscoveryClient
@EnableTransactionManagement
@Import({MapUtils.class, ApplicationStartedListener.class})
public class AddressApplication {

    /**
     * Точка входа.
     *
     * @param args аргументы.
     */
    public static void main(String... args) {
        SpringApplication.run(AddressApplication.class, args);
    }
    
}