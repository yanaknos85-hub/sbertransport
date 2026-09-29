package ru.sberbank.ditsib.transport.role;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.sber.transport.utils.collections.MapUtils;
import ru.sberbank.ditsib.transport.Microservice;

import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;

/**
 * Entry point to the application.
 */
@Microservice
@ConfigurationPropertiesScan
@OpenAPIDefinition(info = @Info(title = "Роли",
                                description = "Операции по работе с ролями",
                                version = "${spring.application.version}"),
                   security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
@ComponentScan(basePackages = { "ru.sberbank.ditsib.transport.role" })
@Import(MapUtils.class)
@EnableTransactionManagement
public class RoleApplication {
    
    /**
     * Start a new application instance.
     *
     * @param args arguments.
     */
    public static void main(String... args) {
        SpringApplication.run(RoleApplication.class);
    }
    
}
