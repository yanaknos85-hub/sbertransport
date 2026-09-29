package ru.sber.transport.business.providers;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * gRPC клиент для проверки длительности поездок.
 */
public interface DurationRequestCheckProvider {

    /**
     * Проверяет лимит длительности поездки.
     *
     * @param passengerId         ID пассажира
     * @param desiredDate         Желаемая дата и время поездки
     * @param expectedDuration    Ожидаемая продолжительность поездки в миллисекундах
     * @param timeZone            Часовой пояс в формате ISO 8601 (например, "+03:00")
     */
    void checkDurationLimit(UUID passengerId, OffsetDateTime desiredDate, Long expectedDuration, String timeZone);

}