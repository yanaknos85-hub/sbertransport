package ru.sber.transport.telemechanic;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.servers.Server;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.sber.transport.files.grpc.annotation.FileExchange;
import ru.sberbank.ditsib.transport.Microservice;

import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;


@Microservice
@ConfigurationPropertiesScan
@EnableDiscoveryClient
@EnableJpaRepositories(
        basePackages = { "ru.sber.transport.telemechanic.database", "ru.sber.transport.humanreadableid" })
@EntityScan(basePackages = { "ru.sber.transport.telemechanic", "ru.sber.transport.humanreadableid" })
@ComponentScan(basePackages = { "ru.sber.transport.telemechanic", "ru.sber.transport.humanreadableid" })
@OpenAPIDefinition(info = @Info(title = "Telemechanic",
                                description = "Микросервис телемеханика",
                                version = "${spring.application.version}"),
                   security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME),
                   servers = {
                           @Server(url = "http://api.fleet.transport.apps.dev-terra000003-ids.ocp.delta.sbrf.ru/api/telemechanic/",
                                   description = "DEV fleet"),
                           @Server(url = "http://api.ift.transport.apps.ift-terra000016-ids.ocp.delta.sbrf.ru/api/telemechanic/",
                                   description = "IFT"),
                           @Server(url = "https://api.sowa-sigma-ift.sbertransport.ru/dev-fleet/api/telemechanic/", description = "Dev SOWA"),
                           @Server(url = "https://api.sowa-sigma-ift.sbertransport.ru/api/telemechanic/", description = "IFT SOWA")
                   })
@FileExchange
@EnableFeignClients
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
public class Application {
    
    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }
}
