package ru.sberbank.ditsib;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Приложение CRM 2.0
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableJpaRepositories
@EnableFeignClients
public class AggregationApplication {

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы.
     */
    public static void main(String... args) {
        SpringApplication.run(AggregationApplication.class, args);
    }

}
