package ru.sber.transport.notifications;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.sber.transport.utils.collections.MapUtils;
import ru.sberbank.ditsib.transport.Microservice;

import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;

/**
 * Entrypoint to the application.
 */
@Microservice
@EntityScan
@OpenAPIDefinition(info = @Info(title = "Уведомления",
                                description = "Операции по работе с уведомлениями",
                                version = "${spring.application.version}"),
                   security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
@EnableTransactionManagement
@EnableAsync
public class NotificationsApplication {
    
    /**
     * Start a new application.
     *
     * @param args arguments to start an application.
     */
    public static void main(String... args) {
        SpringApplication.run(NotificationsApplication.class, args);
    }
    
}
