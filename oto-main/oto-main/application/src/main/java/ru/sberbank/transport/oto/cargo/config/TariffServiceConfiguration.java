package ru.sberbank.transport.oto.cargo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.transport.oto.cargo.database.model.tariff.BaseTariff;
import ru.sberbank.transport.oto.cargo.service.TariffService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class TariffServiceConfiguration {
    @Bean
    <T extends BaseTariff> Map<TransportTypeEnum, TariffService<T>> configServices(List<TariffService<T>> services) {
        return services.stream().collect(Collectors.toMap(TariffService::getTransportType, Function.identity()));
    }
}
