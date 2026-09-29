package ru.sber.transport.etrn;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.sberbank.ditsib.transport.Microservice;

/**
 * Entry point to the application.
 */
@Microservice
@OpenAPIDefinition(info = @Info(title = "etrn-cargo",
        description = "Сервис для работы с ЭТрН",
        version = "${spring.application.version}"),
        security = @SecurityRequirement(name = Microservice.MAIN_SECURITY_SCHEME))
@ConfigurationPropertiesScan
@EnableFeignClients
@SpringBootApplication(scanBasePackages = {"ru.sber.transport"})
public class EtrnApplication {

    public static void main(String[] args) {
        SpringApplication.run(EtrnApplication.class, args);
    }
}