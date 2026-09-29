package ru.sber.transport.telemechanic.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "telemedicine-config")
public class TelemedicineProperties {
    
    private String token;
    
    private String contractorApiToken;
}
