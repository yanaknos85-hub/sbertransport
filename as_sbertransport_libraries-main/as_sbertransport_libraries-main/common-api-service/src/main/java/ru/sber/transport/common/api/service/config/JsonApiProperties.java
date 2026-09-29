package ru.sber.transport.common.api.service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Источник properties для Json API
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "api")
public class JsonApiProperties {

    private String requestUrl;

}
