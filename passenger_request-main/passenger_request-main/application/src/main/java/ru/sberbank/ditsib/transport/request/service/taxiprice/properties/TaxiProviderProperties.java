package ru.sberbank.ditsib.transport.request.service.taxiprice.properties;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Источник properties
 */
@Getter
@Setter
@ConfigurationProperties
public class TaxiProviderProperties {
    
    /**
     * URL адрес сервиса
     */
    @NotBlank(message = "Set base url")
    private String baseUrl;
    
    /**
     * Ключ доступа
     */
    @NotBlank(message = "Set auth header")
    private String authHeader;
    
    /**
     * Ключ доступа
     */
    @NotBlank(message = "Set auth token")
    private String authToken;
    
    /**
     * Ключ доступа
     */
    @NotBlank(message = "Set provider")
    private String provider;
    
    /**
     * Ключ доступа
     */
    @NotBlank(message = "Set client id")
    private String clientId;
    
    /**
     * Ключ доступа
     */
    @NotBlank(message = "Set enabled flag")
    private Boolean enabled;
    
}
