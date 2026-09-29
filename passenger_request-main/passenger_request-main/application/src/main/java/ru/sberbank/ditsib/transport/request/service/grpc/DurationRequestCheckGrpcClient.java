package ru.sberbank.ditsib.transport.request.service.grpc;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * gRPC клиент для проверки длительности поездок.
 */
public interface DurationRequestCheckGrpcClient {

    /**
     * Проверяет лимит длительности поездки.
     * Бросает DurationLimitExceededException при превышении лимита.
     *
     * @param passengerId         ID пассажира
     * @param desiredDate         Желаемая дата и время поездки
     * @param expectedDuration    Ожидаемая продолжительность поездки в миллисекундах
     * @param timeZone            Часовой пояс в формате ISO 8601 (например, "+03:00")
     */
    void checkDurationLimit(UUID passengerId, OffsetDateTime desiredDate, Long expectedDuration, String timeZone);

}
