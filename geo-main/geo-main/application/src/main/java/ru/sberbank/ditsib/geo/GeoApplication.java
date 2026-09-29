package ru.sberbank.ditsib.geo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import ru.sber.transport.utils.collections.MapUtils;
import ru.sberbank.ditsib.geo.config.properties.GeoProperties;
import ru.sberbank.ditsib.transport.Microservice;

import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;

/**
 * Entry point to the application.
 */
@Microservice
@EnableConfigurationProperties(GeoProperties.class)
@OpenAPIDefinition(info = @Info(title = "ГЕО",
                                description = "Операции по работе с провайдером гео-данных",
                                version = "${spring.application.version}"),
                   security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
@ComponentScan(basePackages = { "ru.sberbank.ditsib.geo" })
@Import(MapUtils.class)
public class GeoApplication {
    
    /**
     * Start a new application.
     *
     * @param args arguments for application.
     */
    public static void main(String... args) {
        SpringApplication.run(GeoApplication.class, args);
    }
    
}
