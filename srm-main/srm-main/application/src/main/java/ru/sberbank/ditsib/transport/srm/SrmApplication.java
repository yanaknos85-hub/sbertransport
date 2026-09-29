package ru.sberbank.ditsib.transport.srm;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;
import ru.sberbank.ditsib.transport.Microservice;
import ru.sberbank.ditsib.transport.srm.config.GisProvidersProperties;

import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;

/**
 * Entry point to application.
 */
@Microservice
@EnableTransactionManagement
@OpenAPIDefinition(info = @Info(title = "Совместные поездки",
                                description = "Операции по работе с совместными поездками",
                                version = "${spring.application.version}"),
                   security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
@EnableConfigurationProperties(GisProvidersProperties.class)
@EnableJpaRepositories(basePackages = { "ru.sberbank.ditsib.transport.srm" })
@EntityScan(basePackages = { "ru.sberbank.ditsib.transport.srm" })
@ComponentScan(basePackages = { "ru.sberbank.ditsib.transport.srm" })
@EnableScheduling
@EnableFeignClients
@NoAuthorize("/monitoring")
public class SrmApplication {
    
    /**
     * Start a new application.
     *
     * @param args arguments of application.
     */
    public static void main(String... args) {
        SpringApplication.run(SrmApplication.class, args);
    }
    
 
}
