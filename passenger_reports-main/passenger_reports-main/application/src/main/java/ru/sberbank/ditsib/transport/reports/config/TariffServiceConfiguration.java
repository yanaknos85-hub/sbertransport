package ru.sberbank.ditsib.transport.reports.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.model.tariff.BaseTariff;
import ru.sberbank.ditsib.transport.reports.service.TariffService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class TariffServiceConfiguration {
    @Bean
    <T extends BaseTariff> Map<TransportTypeEnum, TariffService<T>> configServices(List<TariffService<T>> services) {
        Map<TransportTypeEnum, TariffService<T>> tariffServiceMap = new HashMap<>();
        for (TariffService<T> service : services) {
            tariffServiceMap.put(service.getTransportType(), service);
        }
        return tariffServiceMap;
    }
}
