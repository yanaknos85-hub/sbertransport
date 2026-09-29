package ru.sber.transport.dispatcher;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import ru.sber.transport.utils.collections.MapUtils;

import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;

/**
 * Entry point to application.
 */
@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Диспетчерская", description = "Операции по ведению сущностей Диспетчерской",
        version = "${spring.application.version}"),
        security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
@ConfigurationPropertiesScan
@EnableJpaRepositories(basePackages = {"ru.sber.transport.dispatcher"})
@EntityScan(basePackages = {"ru.sber.transport.dispatcher"})
@EnableWebSocket
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@Import(MapUtils.class)
@EnableFeignClients
@EnableDiscoveryClient
public class DispatcherApplication {

    /**
     * Start a new application.
     *
     * @param args arguments of application.
     */
    public static void main(String... args) {
        SpringApplication.run(DispatcherApplication.class, args);

    }

}
