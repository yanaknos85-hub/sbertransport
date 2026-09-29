package ru.sberbank.ditsib.transport.request.service.taxiprice.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки для диспетчерской.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "external-prices.taxi-providers")
public class TaxiProvidersProperties {
    
    TaxiProviderProperties citymobil = new TaxiProviderProperties();
    
    TaxiProviderProperties yandex = new TaxiProviderProperties();
    
    TaxiProviderProperties uber = new TaxiProviderProperties();
}
