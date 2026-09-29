package ru.sberbank.transport.oto.cargo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;
import ru.sberbank.ditsib.transport.Microservice;

import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;

/**
 * Entry point to the application.
 */
@Microservice
@EnableTransactionManagement
@ConfigurationPropertiesScan
@EntityScan
@EnableFeignClients
@OpenAPIDefinition(info = @Info(title = "ОТО",
        description = "Микросервис отдела транспортного обеспечения",
        version = "${spring.application.version}"),
        security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
@NoAuthorize("/monitoring")
public class OtoCargoApplication {

    /**
     * Start a new application.
     * @param args arguments of application.
     */
    public static void main(String... args) {
        SpringApplication.run(OtoCargoApplication.class, args);
    }

}
