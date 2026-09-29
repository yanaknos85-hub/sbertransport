package ru.sberbank.ditsib.transport.request.service.grpc;

import java.time.LocalDateTime;
import java.util.UUID;
import ru.sber.transport.request_checks.grpc.CheckOverrunLimitResponse;

/**
 * gRPC клиент для проверки превышения лимита суммарного километража.
 */
public interface OverrunRequestCheckGrpcClient {

    /**
     * Проверяет превышение лимита суммарного километража за месяц.
     * Если лимит превышен, возвращает ответ с комментарием и суммарной дистанцией.
     *
     * @param passengerId      ID пассажира
     * @param desiredDate      Желаемая дата и время поездки
     * @param expectedDistance Ожидаемая дистанция поездки в метрах
     * @param timeZone         Часовой пояс (например, "+03:00")
     * @return Ответ с комментарием о нарушении и суммарной дистанцией, либо null если лимит не превышен
     */
    CheckOverrunLimitResponse checkOverrunLimit(UUID passengerId, LocalDateTime desiredDate, int expectedDistance, String timeZone);

}