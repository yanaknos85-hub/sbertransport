package ru.sber.transport.request_checks.service;

import ru.sber.transport.request.messaging.RequestMessage;
import ru.sber.transport.request_checks.dto.DurationCheckRequestDto;
import ru.sber.transport.request_checks.dto.MultipointCheckRequestDto;
import ru.sber.transport.request_checks.dto.OverrunCheckRequestDto;
import ru.sber.transport.request_checks.dto.OverrunCheckResponseDto;
import ru.sber.transport.request_checks.messaging.message.ExternalRequestMessage;

/**
 * Сервис работы с заявками на поездки
 */
public interface TripRequestService {

    /**
     * Сохраняет заявку на поездку из ExternalRequestMessage.
     *
     * @param message внешнее сообщение заявки
     */
    void saveFromExternalMessage(ExternalRequestMessage message);

    /**
     * Сохраняет заявку на поездку из RequestMessage.
     *
     * @param message сообщение заявки
     */
    void saveFromRequestMessage(RequestMessage message);

    /**
     * Проверяет лимит многоточечных поездок на заданную дату. Многоточечная поездка - это заявка с количеством адресов
     * (waypoints) > 2. Если таких заявок больше 3, выбрасывается исключение.
     *
     * @param request DTO с данными для проверки
     */
    void checkMultipointLimit(MultipointCheckRequestDto request);

    /**
     * Проверяет лимит длительности поездок на заданную дату. Сумма длительности всех поездок за день + новая поездка
     * не должна превышать 12 часов.
     *
     * @param request DTO с данными для проверки
     */
    void checkDurationLimit(DurationCheckRequestDto request);

    /**
     * Проверяет лимит поездок на суммарный километража. Суммарный пробег за месяц + новая поездка
     * не должна превышать request-checks.maxMonthlyDistance
     *
     * @param request DTO с данными для проверки
     * @return DTO с ответом (comment и totalDistance)
     */
    OverrunCheckResponseDto checkOverrunLimit(OverrunCheckRequestDto request);
}
