package ru.sberbank.ditsib.transport;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.lang.annotation.*;

/**
 * Main annotation of microservice.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EnableDiscoveryClient
@ru.sberbank.ditsib.Microservice
@SecurityScheme(type = SecuritySchemeType.HTTP,
             name = Microservice.MAIN_SECURITY_SCHEME,
             description = "Доступ к ресурсам сервиса. Токен можно получить сделав запрос" +
                           " к сервису `/api/auth/login`", scheme = "bearer",
             in = SecuritySchemeIn.HEADER, bearerFormat = "JWT",
             paramName = "Authorization")
public @interface Microservice {
    
    /**
     * Name of main security scheme.
     */
    String MAIN_SECURITY_SCHEME = "access";
    
}
