package ru.sberbank.ditsib.transport.request.config.personal;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.util.List;

/**
 * Конфигурация для проверки заявки на личный транспорт.
 */
@ConfigurationProperties(prefix = "split-check")
@Configuration
@Getter
@Setter
public class TransportRequestSplitCheckProperties {
    private Personal personal = new Personal();

    /**
     * Класс для конфигурации для проверки заявки на личный транспорт на дробление поездки.
     */
    @Getter
    @Setter
    public static class Personal {
        /**
         * Пороговая стоимость (в копейках)
         */
        private Double costThreshold;

        /**
         * Пороговая продолжительность (в минутах)
         */
        private Integer durationThreshold;

        /**
         * Пороговое количество заявок с минимальной стоимостью и меньшей пороговой продолжительности в сутки
         */
        private Integer requestCountThreshold;

        /**
         * Список статусов, для которых не нужно проверять заявку на дробление
         */
        private List<TripRequestStatus> ignoreStatuses;
    }
}
