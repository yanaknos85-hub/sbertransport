package ru.sberbank.ditsib.transport.srm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки для диспетчерской.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "gisdata.providers")
public class GisProvidersProperties {
    GisProviderProperties twogis = new GisProviderProperties();
    GisProviderProperties twogisasync = new GisProviderProperties();
    GisProviderProperties twogisasyncresult = new GisProviderProperties();
}