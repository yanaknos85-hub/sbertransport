package ru.sber.transport.constants;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;

/**
 * Приложение отображения констант.
 */
@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Константы", description = "Константы сервиса",
                                version = "${spring.application.version}"),
                   security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
public class ConstantsApplication {

    /**
     * Точка входа.
     *
     * @param args аргументы.
     */
    public static void main(String... args) {
        SpringApplication.run(ConstantsApplication.class, args);
    }
    
}
