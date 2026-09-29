package ru.sberbank.ditsib.transport.request.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки проверки превышения суммарного времени ожидания на всех точках маршрута.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "request.validation.all-points-max-wait-time.taxi")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AllPointsMaxWaitTimeProperties {

    /**
     * Лимит суммарного времени ожидания в минутах.
     */
    private int limitInMinutes;

    /**
     * Порог расстояния (м). Если расстояние ниже — fraud создаётся всегда.
     * Если выше — только при одном уникальном городе.
     */
    private int distanceThreshold;
}
