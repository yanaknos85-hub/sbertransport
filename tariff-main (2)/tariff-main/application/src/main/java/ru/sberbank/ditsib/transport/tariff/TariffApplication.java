package ru.sberbank.ditsib.transport.tariff;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.sberbank.ditsib.transport.Microservice;

import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;

/**
 * Application.
 */
@ConfigurationPropertiesScan
@Microservice
@EnableFeignClients
@EnableScheduling
@EnableTransactionManagement
@OpenAPIDefinition(info = @Info(title = "Тарифы",
                                description = "Операции по работе с тарифами",
                                version = "${spring.application.version}"),
                   security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
@EnableJpaRepositories(
        basePackages = { "ru.sberbank.ditsib.transport.tariff", "ru.sber.transport.humanreadableid" })
@EntityScan(basePackages = { "ru.sberbank.ditsib.transport.tariff", "ru.sber.transport.humanreadableid" })
@ComponentScan(
        basePackages = { "ru.sberbank.ditsib.transport.tariff", "ru.sber.transport.humanreadableid" })
public class TariffApplication {
    
    /**
     * Entry point to application.
     *
     * @param args arguments for starting application.
     */
    public static void main(String... args) {
        SpringApplication.run(TariffApplication.class, args);
    }
    
    
}
