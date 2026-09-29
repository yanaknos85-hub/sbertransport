package ru.sberbank.ditsib.transport.srm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.srm.model.tariff.BaseTariff;
import ru.sberbank.ditsib.transport.srm.service.TariffService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Конфигурация приложения
 */
@Configuration
public class AppConfig {

    /**
     * Создаем карту сервисов тарифов
     * @param tariffs список сервисов тарифов
     * @return карта тарифов
     */
    @SuppressWarnings("java:S1452")
    @Bean
    public Map<TransportTypeEnum, TariffService<? extends BaseTariff>> tariffServiceMap(List<TariffService<? extends BaseTariff>> tariffs) {
        return tariffs.parallelStream().collect(Collectors.toMap(TariffService::transportType, Function.identity()));
    }

}
