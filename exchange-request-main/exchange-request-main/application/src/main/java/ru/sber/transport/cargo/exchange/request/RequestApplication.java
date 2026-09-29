package ru.sber.transport.cargo.exchange.request;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.integration.config.EnableIntegration;

/**
 * Entry point to the application.
 */
@SpringBootApplication
@EnableIntegration
@OpenAPIDefinition(info = @Info(title = "exchange-cargo",
        description = "Работа с заявками в логистической бирже",
        version = "${spring.application.version}"))
@ConfigurationPropertiesScan
public class RequestApplication {

    /**
     * Start a new application instance.
     *
     * @param args arguments.
     */
    public static void main(String... args) {
        SpringApplication.run(RequestApplication.class, args);
    }

}