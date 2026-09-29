package ru.sberbank.ditsib.geo_zones;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.sber.transport.file_works.FileWorksConfiguration;
import ru.sber.transport.utils.collections.MapUtils;

/**
 * Geo application.
 */
@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Геозоны",
                                description = "Операции по работе с геозонами",
                                version = "${spring.application.version}"),
                   security = @SecurityRequirement(name = "access"))
@EnableTransactionManagement
@Import({MapUtils.class, FileWorksConfiguration.class})
public class GeoZoneApplication {
    
    /**
     * Entry point to application.
     *
     * @param args arguments for starting application.
     */
    public static void main(String... args) {
        SpringApplication.run(GeoZoneApplication.class, args);
    }
    
}
