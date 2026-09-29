package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.request.TripRequest;

import java.util.Set;
import java.util.UUID;

/**
 * Сервис по работе с заявками на поездки.
 */
public interface TripRequestService {
    
    /**
     * Получение.
     *
     * @param id идентификатор заявки.
     * @return заявка.
     */
    TripRequest get(UUID id);
    
    /**
     * Удалить заявку.
     *
     * @param requestId идентификатор заявки.
     */
    void delete(UUID requestId);
    
    /**
     * Сохранить заявку.
     *
     * @param request заявка на сохранение.
     */
    TripRequest save(TripRequest request);
    
    /**
     * Поиск заявок по id совместных поездок
     * @param id id совместной поездки
     * @return список заявок
     */
    Set<TripRequest> getAllBySharedRideId(UUID id);
    
    /**
     * Сохранить заявку принудительно.
     *
     * @param request заявка на сохранение.
     */
    TripRequest saveAndFlush(TripRequest request);
}
