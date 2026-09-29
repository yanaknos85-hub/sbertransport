package ru.sber.transport.authentication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.sberbank.ditsib.transport.Microservice;

import java.time.Clock;
import java.time.ZoneOffset;

import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;

/**
 * Application.
 */
@Microservice
@ConfigurationPropertiesScan
@OpenAPIDefinition(info = @Info(title = "Аутентификация", description = "Операции по входу/выходу из системы",
                                version = "${spring.application.version}"))
@SecurityScheme(type = SecuritySchemeType.HTTP, name = "login", description = "Вход",
                scheme = "basic")
@SecurityScheme(type = SecuritySchemeType.HTTP, name = MAIN_SECURITY_SCHEME,
                description = "Доступ",
                scheme = "bearer",
                in = SecuritySchemeIn.HEADER, bearerFormat = "JWT",
                paramName = "Authorization")
@EnableScheduling
@EnableTransactionManagement
public class AuthenticationApplication {
    
    /**
     * Entry point to application.
     *
     * @param args arguments for starting application.
     */
    public static void main(String... args) {
        SpringApplication.run(AuthenticationApplication.class, args);
    }
    
    @Bean
    Clock clock() {
        return Clock.system(ZoneOffset.UTC);
    }
    
}
