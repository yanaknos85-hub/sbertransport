package ru.sberbank.ditsib.transport.srm.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Источник properties для Мадженты
 */
@Getter
@Setter
public class GisProviderProperties {
    
    /**
     * URL адрес сервиса
     */
    @NotBlank(message = "Set root url")
    private String rootUrl;
    
    /**
     * Ключ доступа
     */
    @NotBlank(message = "Set auth protocol")
    private String apiKey;
    
}
