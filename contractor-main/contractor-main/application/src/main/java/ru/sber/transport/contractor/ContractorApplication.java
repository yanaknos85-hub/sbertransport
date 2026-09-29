package ru.sber.transport.contractor;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;

/**
 * Entry point to application.
 */
@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Контрагенты", description = "Операции по работе с контрагентами",
        version = "${spring.application.version}"),
        security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
@ConfigurationPropertiesScan
@EnableJpaRepositories(basePackages = {"ru.sber.transport.contractor"})
@EntityScan(basePackages = {"ru.sber.transport.contractor"})
@EnableWebSocket
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@EnableFeignClients
public class ContractorApplication {

    /**
     * Start a new application.
     *
     * @param args arguments of application.
     */
    public static void main(String... args) {
        SpringApplication.run(ContractorApplication.class, args);

    }

}
