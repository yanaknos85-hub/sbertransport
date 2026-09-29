package ru.sberbank.ditsib.transport.srm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "gisdata")
public class GisDataProperties {
    private String sowaUrl;
    private int timeout;
    private int sleepTime;

}
