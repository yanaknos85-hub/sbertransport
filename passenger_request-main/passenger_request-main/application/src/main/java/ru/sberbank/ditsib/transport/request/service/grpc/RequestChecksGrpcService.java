package ru.sberbank.ditsib.transport.request.service.grpc;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сервис для работы с request-checks по grpc
 */
public interface RequestChecksGrpcService {

    /**
     * Проверка превышения лимита многоточечных поездок.
     * Многоточечная поездка — это заявка с количеством адресов (waypoints) > 2.
     * Если таких заявок больше лимита, выбрасывается исключение.
     *
     * @param passengerId ID пассажира
     * @param desiredDate Дата-время поездки
     * @param timeZone    Часовой пояс в формате ISO 8601l
     * @return true при превышении лимита и false - если проверку прошел успешно
     */
    boolean isMultipointLimitExceeded(UUID passengerId, LocalDateTime desiredDate, String timeZone);
}
