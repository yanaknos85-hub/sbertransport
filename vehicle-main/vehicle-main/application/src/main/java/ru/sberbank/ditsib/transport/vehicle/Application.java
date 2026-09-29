package ru.sberbank.ditsib.transport.vehicle;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.sberbank.ditsib.transport.Microservice;

import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;

@Microservice
@EntityScan(basePackages = { "ru.sberbank.ditsib.transport.vehicle" })
@ComponentScan(basePackages = { "ru.sberbank.ditsib.transport.vehicle" })
@EnableJpaRepositories(basePackages = { "ru.sberbank.ditsib.transport.vehicle.database" })
@OpenAPIDefinition(info = @Info(title = "Vehicle",
                                description = "Микросервис по транспортным средствам",
                                version = "${spring.application.version}"),
                   security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME),
                   servers = {
                           @Server(url = "http://api.fleet.transport.apps.dev-terra000003-ids.ocp.delta.sbrf.ru/api/vehicle/",
                                   description = "DEV fleet"),
                           @Server(url = "http://api.ift.transport.apps.ift-terra000016-ids.ocp.delta.sbrf.ru/api/vehicle/",
                                   description = "IFT"),
                           @Server(url = "https://api.sowa-sigma-ift.sbertransport.ru/dev-fleet/api/vehicle/",
                                   description = "Dev SOWA"),
                           @Server(url = "https://api.sowa-sigma-ift.sbertransport.ru/api/vehicle/",
                                   description = "IFT SOWA")
                   })
public class Application {
    
    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
        
    }
}