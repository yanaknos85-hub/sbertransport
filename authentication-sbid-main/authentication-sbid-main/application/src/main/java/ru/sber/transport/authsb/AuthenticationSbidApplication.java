package ru.sber.transport.authsb;

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
@OpenAPIDefinition(info = @Info(title = "Авторизация по Сбер Бизнес ИД",
        description = "Взаимодействие с Сбер Бизнес ИД",
        version = "${spring.application.version}"))
@ConfigurationPropertiesScan
public class AuthenticationSbidApplication {

    static {
        System.setProperty("javax.net.debug", "ssl,handshake");
        System.setProperty("reactor.netty.http.client.HttpClient", "DEBUG");
    }
    /**
     * Start a new application instance.
     *
     * @param args arguments.
     */
    public static void main(String... args) {
        SpringApplication.run(AuthenticationSbidApplication.class, args);
    }

}

