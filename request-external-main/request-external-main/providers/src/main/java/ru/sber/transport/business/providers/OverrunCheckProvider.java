package ru.sber.transport.business.providers;

import ru.sber.transport.request.external.model.overrun.OverrunCheckResult;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * gRPC клиент для проверки суммарного километража в request_checks.
 */
public interface OverrunCheckProvider {

    /**
     * Проверяет лимит суммарного километража поездки.
     *
     * @param passengerId      ID пассажира
     * @param desiredDate      Желаемая дата и время поездки
     * @param expectedDistance Ожидаемая дистанция поездки
     * @param timeZone         Часовой пояс в формате ISO 8601 (например, "+03:00")
     * @return результат проверки с комментарием и суммарной дистанцией
     */
    OverrunCheckResult checkOverrunLimit(UUID passengerId, OffsetDateTime desiredDate, int expectedDistance, String timeZone);
}