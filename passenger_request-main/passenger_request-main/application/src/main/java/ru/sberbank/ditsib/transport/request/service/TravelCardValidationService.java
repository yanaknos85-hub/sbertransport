package ru.sberbank.ditsib.transport.request.service;

import lombok.NonNull;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPublic;
import ru.sberbank.ditsib.transport.request.exceptions.ActiveTravelPassExistsException;

/**
 * Сервис валидации проездных билетов
 */
public interface TravelCardValidationService {

    /**
     * Проверка существования активного проездного билета у пользователя на выбранную дату поездки
     *
     * @param request заявка
     * @throws ActiveTravelPassExistsException если найден активный проездной
     */
    void checkOneTimeTrip(@NonNull RequestForPublic request) throws ActiveTravelPassExistsException;

    /**
     * Проверка пересечения активных проездных по периоду действия с проездными в заявке для пассажира
     *
     * @param request заявка
     * @throws ActiveTravelPassExistsException если найдено пересечение периодов или превышено количество проездных
     */
    void checkOverlap(@NonNull RequestForPublic request) throws ActiveTravelPassExistsException;
}
